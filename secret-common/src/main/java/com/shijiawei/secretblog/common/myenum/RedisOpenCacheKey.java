package com.shijiawei.secretblog.common.myenum;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ClassName: RedisOpenCacheKey
 * Description:
 *
 * @Create 2025/11/1 下午11:37
 */

public class RedisOpenCacheKey {

    private RedisOpenCacheKey() {}

    /**
     * 文章相關常數
     */
    private static final String ARTICLE = "ams:article:";


    /*
     * 文章評論相關常數(評論區快取)
     */
    public static class ArticleComments{
        private ArticleComments() {}

        public static final String COMMENT_DETAILS_PREFIX = ARTICLE + "comment";
        public static final String COMMENT_DETAILS_KEY = "#{#articleId}:comment_details";

    }



//    /*
//     * 文章評論資訊相關常數(點讚數、書簽署)
//     */
//    public static class ArticleCommentsStatus{
//        private ArticleCommentsStatus() {}
//
//        public static final String LIKES_COUNT_PREFIX = ARTICLE + "comments";
//        public static final String LIKES_COUNT_KEY = "#{#articleId}:likes_count";
//
//        public static final String BOOKMARKS_COUNT_PREFIX = ARTICLE + "comments";
//        public static final String BOOKMARKS_COUNT_KEY = "#{#articleId}:bookmarks_count";
//    }



//    /*
//     * 文章評論資訊相關常數(點讚數、)
//     */
//    public static class ARTICLE_COMMENTS_STATUS{
//        //        private ARTICLE_COMMENTS_STATUS() {}
//        public static class LIKE_COUNTS{
//
//
//            public static final String PREFIX = ARTICLE + "comments";
//            public static final String KEY = "#{#articleId}:likes_count";
//
//        }
//        public static class BOOKMARK_COUNTS{
//
//
//            public static final String PREFIX = ARTICLE + "comments";
//            public static final String KEY = "#{#articleId}:bookmarks_count";
//
//        }
//
//    }
}
