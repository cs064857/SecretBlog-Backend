package com.shijiawei.secretblog.common.codeEnum;

import com.shijiawei.secretblog.common.message.ArticleLikeChangedMessage;

/**
 * ClassName: RabbitMqConsts
 * Description:
 *
 * @Create 2025/12/4 下午3:24
 */
public final class RabbitMqConsts {

    private RabbitMqConsts() {}

    /**
     * 使用者服務（secret-user）相關常數
     */
    public static final class User {

        private static final String PREFIX = "user";

        private User() {}

        public static final String DIRECT_EXCHANGE = PREFIX + ".directExchange";
        public static final String FANOUT_EXCHANGE = PREFIX + ".fanoutExchange";
        public static final String TOPIC_EXCHANGE = PREFIX + ".topicExchange";

        /**
         * 作者頭像更新（用於同步文章模組作者資訊）
         */
        public static final class UserAvatarUpdate {
            private UserAvatarUpdate() {}

            public static final String QUEUE = "user.avatar.update.queue";
            public static final String ROUTING_KEY = "user.avatar.updated";
        }

        /**
         * 文章被點讚後，以 Email 通知作者
         */
        public static final class ArticleLikedEmailNotify {
            private ArticleLikedEmailNotify() {}

            public static final String QUEUE = "user.article.liked.email.notify.queue";
            public static final String ROUTING_KEY = "user.article.liked.email.notify";
        }

    }

    /**
     * 文章服務（secret-article / ams）相關常數
     */
    public static final class Ams {
        private Ams() {}

        private static final String PREFIX = "ams";

        public static final String TOPIC_EXCHANGE = PREFIX + ".topicExchange";

        /**
         * 1、用戶對文章互動行為更新（點讚/取消點讚狀態）
         * 2、用戶對文章互動行為更新（點讚/取消點讚狀態）
         * 共用一把RoutingKey
         */
        public static final class ArticleLikeChanged {
            private ArticleLikeChanged() {}

            public static final String ROUTING_KEY = "ams.liked.updated";
        }

        /**
         * 更新文章點讚數
         */
        public static final class UpdateArticleLiked {
            private UpdateArticleLiked() {}

            public static final String QUEUE = "ams.liked.updated.queue";
//            public static final String ROUTING_KEY = "ams.liked.updated";
        }

        /**
         * 用戶對文章互動行為更新（點讚/取消點讚狀態）
         */
        public static final class UpdateArticleAction {
            private UpdateArticleAction() {}

            public static final String QUEUE = "ams.action.updated.queue";
//            public static final String ROUTING_KEY = "ams.action.updated";
        }






        /**
         * 文章書籤數更新（書籤數同步到 AmsArtStatus）
         */
        public static final class UpdateArticleBookmark {
            private UpdateArticleBookmark() {}

            public static final String QUEUE = "ams.bookmark.updated.queue";
            public static final String ROUTING_KEY = "ams.bookmark.updated";
        }

        /**
         * 用戶對文章書籤行為更新（書籤狀態同步到 AmsArtAction）
         */
        public static final class UpdateArticleBookmarkAction {
            private UpdateArticleBookmarkAction() {}

            public static final String QUEUE = "ams.bookmark.action.updated.queue";
            public static final String ROUTING_KEY = "ams.bookmark.action.updated";
        }

        /**
         * 留言讚數更新（點讚數同步到 AmsCommentStatistics）
         */
        public static final class UpdateCommentLiked {
            private UpdateCommentLiked() {}

            public static final String QUEUE = "ams.comment.liked.updated.queue";
            public static final String ROUTING_KEY = "ams.comment.liked.updated";
        }

        /**
         * 用戶對留言互動行為更新（點讚/取消點讚狀態同步到 AmsCommentAction）
         */
        public static final class UpdateCommentAction {
            private UpdateCommentAction() {}

            public static final String QUEUE = "ams.comment.action.updated.queue";
            public static final String ROUTING_KEY = "ams.comment.action.updated";
        }

    }

    /**
     * 搜索服務（secret-search）相關常數
     */
    public static final class Search {
        private Search() {}

        private static final String PREFIX = "search";

        public static final String TOPIC_EXCHANGE = PREFIX + ".topicExchange";

        /**
         * 文章同步至 Elasticsearch
         */
        public static final class SyncArticleToES {
            private SyncArticleToES() {}

            public static final String QUEUE = "search.article.sync.queue";
            public static final String ROUTING_KEY = "search.article.sync";
        }
    }

}
