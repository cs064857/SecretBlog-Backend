package com.shijiawei.secretblog.common.codeEnum;

/**
 * ClassName: RabbitMqConsts
 * Description:
 *
 * @Create 2025/12/4 下午3:24
 */
public class RabbitMqConsts {

    private RabbitMqConsts() {};

    public static class user{

        private static final String user = "user";

        private user() {}

        public static final String directExchange = user+".directExchange";
        public static final String fanoutExchange = user+".fanoutExchange";
        public static final String topicExchange = user+".topicExchange";

        public static class userAvatarUpdate{
            private userAvatarUpdate() {}
            public static final String queue = "user.avatar.update.queue";
            public static final String routingKey = "user.avatar.updated";

        }

    }

    public static class ams{
        private ams(){}
        private static final String ams = "ams";
        
        public static final String topicExchange = ams + ".topicExchange";

        public static class updateArticleLiked{
            private updateArticleLiked(){}
            public static final String queue = "ams.liked.updated.queue";
            public static final String routingKey = "ams.liked.updated";

        }

        /**
         * 用戶對文章互動行為更新（點讚/取消點讚狀態）
         */
        public static class updateArticleAction{
            private updateArticleAction(){}
            public static final String queue = "ams.action.updated.queue";
            public static final String routingKey = "ams.action.updated";
        }

        /**
         * 文章書籤數更新（書籤數同步到 AmsArtStatus）
         */
        public static class updateArticleBookmark{
            private updateArticleBookmark(){}
            public static final String queue = "ams.bookmark.updated.queue";
            public static final String routingKey = "ams.bookmark.updated";
        }

        /**
         * 用戶對文章書籤行為更新（書籤狀態同步到 AmsArtAction）
         */
        public static class updateArticleBookmarkAction{
            private updateArticleBookmarkAction(){}
            public static final String queue = "ams.bookmark.action.updated.queue";
            public static final String routingKey = "ams.bookmark.action.updated";
        }

        /**
         * 留言讚數更新（點讚數同步到 AmsCommentStatistics）
         */
        public static class updateCommentLiked{
            private updateCommentLiked(){}
            public static final String queue = "ams.comment.liked.updated.queue";
            public static final String routingKey = "ams.comment.liked.updated";
        }

        /**
         * 用戶對留言互動行為更新（點讚/取消點讚狀態同步到 AmsCommentAction）
         */
        public static class updateCommentAction{
            private updateCommentAction(){}
            public static final String queue = "ams.comment.action.updated.queue";
            public static final String routingKey = "ams.comment.action.updated";
        }

    }
}
