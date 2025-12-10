package com.shijiawei.secretblog.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        var httpHost = new HttpHost("localhost", 9200);

        RestClient build = RestClient.builder(httpHost).build();

        var transport = new RestClientTransport(build, new JacksonJsonpMapper());


        return new ElasticsearchClient(transport);
    }
}