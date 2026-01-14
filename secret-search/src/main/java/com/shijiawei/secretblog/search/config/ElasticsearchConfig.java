package com.shijiawei.secretblog.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    //從配置檔案讀取Elasticsearch連線地址
    @Value("${spring.elasticsearch.uris}")
    private String uris;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        //使用配置的URI動態建立HttpHost
        HttpHost httpHost = HttpHost.create(uris);

        RestClient build = RestClient.builder(httpHost).build();

        var transport = new RestClientTransport(build, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
