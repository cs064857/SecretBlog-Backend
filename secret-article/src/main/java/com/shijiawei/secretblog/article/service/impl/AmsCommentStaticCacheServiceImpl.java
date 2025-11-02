//package com.shijiawei.secretblog.article.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.shijiawei.secretblog.article.entity.AmsComment;
//import com.shijiawei.secretblog.article.entity.AmsCommentInfo;
//import com.shijiawei.secretblog.article.feign.UserFeignClient;
//import com.shijiawei.secretblog.article.mapper.AmsCommentStaticCacheMapper;
//import com.shijiawei.secretblog.article.service.AmsArticleService;
//import com.shijiawei.secretblog.article.service.AmsCommentInfoService;
//import com.shijiawei.secretblog.article.service.AmsCommentStaticCacheService;
//import com.shijiawei.secretblog.article.vo.AmsArtCommentStaticVo;
//import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
//import com.shijiawei.secretblog.common.annotation.OpenCache;
//import com.shijiawei.secretblog.common.dto.UserBasicDTO;
//import com.shijiawei.secretblog.common.exception.CustomBaseException;
//import com.shijiawei.secretblog.common.utils.R;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Service;
//
//import java.time.temporal.ChronoUnit;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
///**
// * ClassName: AmsCommentStaticCacheServiceImpl
// * Description:
// *
// * @Create 2025/11/1 下午7:45
// */
//@Slf4j
//@Service
//public class AmsCommentStaticCacheServiceImpl extends ServiceImpl<AmsCommentStaticCacheMapper, AmsComment> implements AmsCommentStaticCacheService {
//
//    private final AmsArticleService amsArticleService;
//    private final AmsCommentInfoService amsCommentInfoService;
//    private final UserFeignClient userFeignClient;
//
//    public AmsCommentStaticCacheServiceImpl(AmsArticleService amsArticleService , AmsCommentInfoService amsCommentInfoService , UserFeignClient userFeignClient){
//        this.amsArticleService = amsArticleService;
//        this.amsCommentInfoService = amsCommentInfoService;
//        this.userFeignClient = userFeignClient;
//    }
//
//
//    @OpenCache(prefix = "AmsCommentsStatic", key = "articleId:#{#articleId}", time = 30, chronoUnit = ChronoUnit.MINUTES)
//    @Override
//    public List<AmsArtCommentStaticVo> getStaticCommentDetails(Long articleId) {
//        //透過布隆過濾器判斷文章id是否不存在, 若不存在則拋出異常
//        if(amsArticleService.isArticleNotExists(articleId)){
//            log.info("文章不存在，articleId={}",articleId);
//            throw new CustomBaseException("文章不存在");
//        }
//
//        //根據該文章的ArticleId查找所有關聯的CommentInfo
//        List<AmsCommentInfo> amsCommentInfoList = amsCommentInfoService.list(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, articleId));
//        if(amsCommentInfoList.isEmpty()){
//            //假設文章無評論, 則直接回傳空陣列
//            log.info("文章無評論, articleId:{}", articleId);
//            return Collections.emptyList();
//        }
//        List<Long> amsCommentIds = amsCommentInfoList.stream()
//                .map(AmsCommentInfo::getCommentId)
//                .collect(Collectors.toList());
//
//
//
//
//        log.info("所有留言的Ids:{}",amsCommentIds);
//        //根據CommentInfo中的CommentId查找該文章所有的留言
//        List<AmsComment> amsCommentList = this.baseMapper.selectList(new LambdaQueryWrapper<AmsComment>().in(AmsComment::getId, amsCommentIds));
//        log.info("所有留言:{}",amsCommentList);
//        //將commentList轉為MAP屬性,KEY為ID、VALUE為對象,方便與CommentInfo的內容對應上
//        Map<Long, AmsComment> amsCommentCollect = amsCommentList.stream().collect(Collectors.toMap(AmsComment::getId, Function.identity()));
//        Map<Long, AmsCommentInfo> amsCommentInfoCollect = amsCommentInfoList.stream().collect(Collectors.toMap(AmsCommentInfo::getId, Function.identity()));
////        List<AmsArtCommentStaticVo> amsArtCommentsFromParentCommentId = getAmsArtCommentsFromParentCommentId(amsCommentInfo.getParentCommentId());
//        //該文章下所有是父留言的留言ID
////        List<Long> amsParentCommentIds = amsCommentInfoList.stream()
////                .map(AmsCommentInfo::getParentCommentId)
////                .collect(Collectors.toList());
//
//        //TODO 利用用戶的ID來獲取用戶名稱
//        List<Long> userIds = amsCommentInfoList.stream().map(AmsCommentInfo::getUserId).toList();
//        R<List<UserBasicDTO>> usersByIds = userFeignClient.selectUserBasicInfoByIds(userIds);
//
//        Map<Long,UserBasicDTO> userBasicDTOLongMap = Optional.ofNullable(usersByIds)
//                .map(R::getData)//若usersByIds不為空,則取出Data
//                .orElse(Collections.emptyList())//若Data為空,則給一個空的List
//                .stream()
//                .collect(Collectors.toMap(UserBasicDTO::getUserId, Function.identity()));//將List轉為Map,KEY為UserBasicDTO對象、VALUE為UserId
//
//
//        List<AmsArtCommentStaticVo> amsArtCommentStaticVos = amsCommentInfoList.stream().map(amsCommentInfo -> {
//            AmsArtCommentStaticVo amsArtCommentsStaticVo = new AmsArtCommentStaticVo();
//            AmsComment amsComment = amsCommentCollect.get(amsCommentInfo.getCommentId());
//            amsArtCommentsStaticVo.setCommentContent(amsComment.getCommentContent());
//
//
////            if(!userBasicDTOLongMap.isEmpty())
//
//            if(!userBasicDTOLongMap.isEmpty()){
//                UserBasicDTO userBasicDTO = userBasicDTOLongMap.get(amsCommentInfo.getUserId());
//                amsArtCommentsStaticVo.setUsername(userBasicDTO.getNickName());
//                amsArtCommentsStaticVo.setAvatar(userBasicDTO.getAvatar());
//            }
//
//            amsArtCommentsStaticVo.setCreateAt(amsCommentInfo.getCreateAt());
//            amsArtCommentsStaticVo.setUpdateAt(amsCommentInfo.getUpdateAt());
//
//            amsArtCommentsStaticVo.setCommentId(amsCommentInfo.getCommentId());
//            if (amsCommentInfo.getParentCommentId() != null) {
//                //TODO 查詢父留言中的資料
//                //拿parentCommentIds去這篇文章中的所有留言來搜尋到父留言
//
//                AmsComment parentComment = amsCommentCollect.get(amsCommentInfo.getParentCommentId());
//                AmsCommentInfo parentCommentInfo = amsCommentInfoCollect.get(parentComment.getCommentInfoId());
//
//                log.info("留言ID:{},父留言ID:{}", amsCommentInfo.getCommentId(), parentComment.getId());
//                log.info("留言ID:{},父留言InfoID:{}", amsCommentInfo.getCommentId(), parentCommentInfo.getId());
//                log.info("留言ID:{},父留言對象:{}", amsCommentInfo.getCommentId(), parentComment);
//                log.info("留言ID:{},父留言Info對象:{}", amsCommentInfo.getCommentId(), parentCommentInfo);
//                //包裝父留言
//                AmsArtCommentsVo artParentCommentsVo = new AmsArtCommentsVo();
//                BeanUtils.copyProperties(parentComment,artParentCommentsVo);
//                BeanUtils.copyProperties(parentCommentInfo,artParentCommentsVo);
//                amsArtCommentsStaticVo.setParentCommentId(artParentCommentsVo.getCommentId());
//
//                amsArtCommentsStaticVo.setUsername("test");
//                log.info("留言ID:{},父留言包裝後Vo對象:{}", amsCommentInfo.getCommentId(), artParentCommentsVo);
//                return amsArtCommentsStaticVo;
//            }
//            return amsArtCommentsStaticVo;
//        }).collect(Collectors.toList());
//
//        log.info("amsArtCommentsVos:{}",amsArtCommentStaticVos);
//        return amsArtCommentStaticVos;
//    }
//}
