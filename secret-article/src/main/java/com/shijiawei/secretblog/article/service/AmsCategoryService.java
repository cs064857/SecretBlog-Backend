package com.shijiawei.secretblog.article.service;

import com.shijiawei.secretblog.article.entity.AmsCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.vo.AmsCategoryTreeVo;

import java.util.List;

/**
* @author User
* @description 针对表【ams_category】的数据库操作Service
* @createDate 2024-08-28 17:47:50
*/
public interface AmsCategoryService extends IService<AmsCategory> {

    List<AmsCategoryTreeVo> getTreeCategoryVo();

    void saveCategory(AmsCategory amsCategory);

    void deleteById(Long id);

    void updateCategory(Long beforeId, Long afterParentId,Integer afterLevel);

    void updateTreeCategoryName(Long id, String categoryName);
}
