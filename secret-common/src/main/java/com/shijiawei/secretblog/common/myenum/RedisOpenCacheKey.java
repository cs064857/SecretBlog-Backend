package com.shijiawei.secretblog.common.myenum;

import com.shijiawei.secretblog.common.annotation.OpenCache;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.temporal.ChronoUnit;

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
     * 文章留言相關常數(留言區快取)
     */
    public static class ArticleComments{
        private ArticleComments() {}

        public static final String COMMENT_DETAILS_PREFIX = ARTICLE + "comment";
        public static final String COMMENT_DETAILS_KEY = "#{#articleId}:comment_details";

    }

    /*
     * 文章相關常數
     */
    public static class ArticleDetails{
        private ArticleDetails() {}

        public static final String ARTICLE_DETAILS_PREFIX = ARTICLE + "articleDetails";
        public static final String ARTICLE_DETAILS_KEY = "#{#articleId}";

    }
    /*
     * 文章標籤相關常數
     */
    public static class ArticleTags{
        private ArticleTags() {}

        public static final String ARTICLE_Tags_PREFIX = ARTICLE + "articleTags";
        public static final String ARTICLE_Tags_KEY = "list";


    }

    /*
     * 文章預覽列表相關常數
     */
    public static class ArticlePreviews{
        private ArticlePreviews() {}

        public static final String ARTICLE_PREVIEWS_PREFIX = ARTICLE + "previews";
        public static final String ARTICLE_PREVIEWS_KEY = "categoryId_#{#categoryId}:routerPage_#{#routePage}";
        public static final String ARTICLE_PREVIEWS_BY_VO_KEY = "categoryId_#{#amsSaveArticleVo.categoryId}:routerPage_#{#routePage}";

    }

    /*
     * 文章分類相關常數
     */
    public static class ArticleCategories {
        private ArticleCategories() {}

        public static final String CATEGORY_TREE_PREFIX = "AmsCategory";
        public static final String CATEGORY_TREE_KEY = "treeCategoryVos";
        public static final String CATEGORY_TREE_BY_AMS_CATEGORY_ID_KEY = "treeCategoryVos_#{#amsCategory.id}";
    }

    /*
     * 文章列表相關常數
     */
    public static class ArticleList {
        private ArticleList() {}

        public static final String ARTICLE_LIST_PREFIX = "AmsArticle";
        public static final String ARTICLE_LIST_KEY = "articles";
    }
//    /*
//     * 文章留言資訊相關常數(點讚數、書簽署)
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
//     * 文章留言資訊相關常數(點讚數、)
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
