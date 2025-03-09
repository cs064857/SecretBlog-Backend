package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.article.annotation.OpenLog;
import com.shijiawei.secretblog.article.entity.AmsCategory;
import com.shijiawei.secretblog.article.service.AmsCategoryService;
import com.shijiawei.secretblog.article.mapper.AmsCategoryMapper;
import com.shijiawei.secretblog.article.vo.AmsCategoryTreeVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author User
 * @description 针对表【ams_category】的数据库操作Service实现
 * @createDate 2024-08-28 17:47:50
 */
@Slf4j
@Service
public class AmsCategoryServiceImpl extends ServiceImpl<AmsCategoryMapper, AmsCategory> implements AmsCategoryService {
    /**
     * 封裝全部數據並返回給前端展示樹形分類結構
     * @return
     */
    @OpenCache(prefix = "AmsCategory",key = "treeCategoryVos",time = 30,chronoUnit = ChronoUnit.MINUTES)
    @OpenLog
    @Override
    public List<AmsCategoryTreeVo> getTreeCategoryVo() {
        List<AmsCategory> amsCategoryList = this.baseMapper.selectList(new LambdaQueryWrapper<>());
//        log.info("amsCategoryList={}", amsCategoryList);
        List<AmsCategoryTreeVo> categoryTreeVos = amsCategoryList.stream()
                .filter(amsCategory -> amsCategory.getParentId() == 0)
                .map(amsCategory -> {
                    AmsCategoryTreeVo amsCategoryTreeVo = new AmsCategoryTreeVo();
                    amsCategoryTreeVo.setId(amsCategory.getId());
                    amsCategoryTreeVo.setLabel(amsCategory.getCategoryName());
                    amsCategoryTreeVo.setChildren(getChildren(amsCategoryList, amsCategory));
                    return amsCategoryTreeVo;
                }).collect(Collectors.toList());
//        log.info("categoryTreeVos={}", categoryTreeVos);
        return categoryTreeVos;
    }

    /**
     * 封裝全部數據並返回給前端展示樹形分類結構,遞規處理該分類子類數據
     * @param amsCategoryList
     * @param amsCategory
     * @return
     */
    public List<AmsCategoryTreeVo> getChildren(List<AmsCategory> amsCategoryList, AmsCategory amsCategory) {
        List<AmsCategoryTreeVo> childrens = amsCategoryList.stream()
                .filter(item -> Objects.equals(item.getParentId(), amsCategory.getId()))
                .map(amsCategoryChildren -> {
                    AmsCategoryTreeVo amsCategoryTreeVo = new AmsCategoryTreeVo();
                    amsCategoryTreeVo.setId(amsCategoryChildren.getId());
                    amsCategoryTreeVo.setLabel(amsCategoryChildren.getCategoryName());

                    amsCategoryTreeVo.setChildren(getChildren(amsCategoryList, amsCategoryChildren));

                    return amsCategoryTreeVo;
                }).collect(Collectors.toList());
        return childrens;
    }
    /**
     * 儲存該分類數據
     * @param amsCategory
     */
//    @DelayDoubleDelete(prefix = "AmsCategory",key = "'treeCategoryVos_'+ #amsCategory.id",delay = 60)
    @DelayDoubleDelete(prefix = "AmsCategory",key = "treeCategoryVos")
    @OpenLog
    @Override
    public void saveCategory(AmsCategory amsCategory) {
        if(amsCategory!=null){
            if(amsCategory.getParentId()==0){//新增一級分類
                this.baseMapper.insert(amsCategory);
            }else {//新增非一級分類
                AmsCategory parentInfo = this.baseMapper.selectById(amsCategory.getParentId());
//            log.info("parentInfo={}", parentInfo);
                if(parentInfo!=null){
                    amsCategory.setCategoryLevel(parentInfo.getCategoryLevel() + 1);
                    this.baseMapper.insert(amsCategory);
                }
            }

        }
    }

    /**
     * 刪除該分類數據
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        this.baseMapper.deleteById(id);
    }

    /**
     * 拖曳後修改該分類數據
     * @param beforeId
     * @param afterParentId
     * @param afterLevel
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCategory(Long beforeId, Long afterParentId, Integer afterLevel) {
        //寫一個冒泡排序

        //根據Id先查出欲修改之分類數據
        AmsCategory amsCategory = this.baseMapper.selectById(beforeId);
        if (amsCategory != null) {
            amsCategory.setParentId(afterParentId);
            if (afterLevel != -1) {
                amsCategory.setCategoryLevel(afterLevel);
            }
            this.baseMapper.updateById(amsCategory);
        }

        //遞規修改該分類下的子類數據,直到沒有找到子類為止
        if (afterLevel != -1) {
            updateCategoryChildren(beforeId, afterLevel);
        }


    }

    @Override
    public void updateTreeCategoryName(Long id, String categoryName) {
        // 根據 ID 從資料庫中獲取 AmsCategory 對象
        AmsCategory amsCategory = this.baseMapper.selectById(id);

        // 更新 AmsCategory 對象的類別名稱
        amsCategory.setCategoryName(categoryName);

        // 將更新後的 AmsCategory 對象保存回資料庫
        this.baseMapper.updateById(amsCategory);
    }


    /**
     * 遞規修改拖曳後該分類下的子類數據
     * @param beforeId
     * @param afterLevel
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategoryChildren(Long beforeId,Integer afterLevel){
        //找出欲修改之分類數據的子類
        List<AmsCategory> amsCategoryList = this.baseMapper.selectList(new LambdaQueryWrapper<AmsCategory>().eq(AmsCategory::getParentId, beforeId));
        if(!CollectionUtils.isEmpty(amsCategoryList)){
            amsCategoryList = amsCategoryList.stream().map(item -> {
                //將子類層級設定為父類新層級+1
                Integer NewLevel = afterLevel + 1;
                item.setCategoryLevel(NewLevel);
                updateCategoryChildren(item.getId(),NewLevel);
                return item;
            }).toList();
            this.baseMapper.updateById(amsCategoryList);
        }
    }




}




