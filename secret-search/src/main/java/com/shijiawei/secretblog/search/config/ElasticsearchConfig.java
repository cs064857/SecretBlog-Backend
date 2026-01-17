package com.shijiawei.secretblog.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class ElasticsearchConfig {

    //從配置檔案讀取Elasticsearch連線地址
    @Value("${spring.elasticsearch.uris}")
    private String uris;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        //使用配置的URI動態建立HttpHost
        HttpHost httpHost = HttpHost.create(uris);

        RestClientBuilder builder = RestClient.builder(httpHost);

        //配置底層HttpClient 以處理閒置連接回收，防止Connection reset
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setKeepAliveStrategy((response, context) -> Duration.ofMinutes(5).toMillis()); //設置Keep-Alive
            return httpClientBuilder;
        });
        //設置超時時間
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(8000);
            requestConfigBuilder.setSocketTimeout(35000);
            return requestConfigBuilder;
        });

        RestClient restClient = builder.build();
        var transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
