package com.iot.pipeline;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import java.io.IOException;

public class RealTimeAnalytics {
    private final ElasticsearchClient esClient;

    public RealTimeAnalytics(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public SearchResponse<Void> getEventCount(String deviceId) throws IOException {
        return esClient.search(s -> s
            .index(Config.getElasticsearchIndex())
            .query(q -> q
                .match(m -> m
                    .field("deviceId")
                    .query(deviceId)
                )
            ), Void.class);
    }
}
