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
}
