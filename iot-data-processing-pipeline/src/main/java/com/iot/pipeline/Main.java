package com.iot.pipeline;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try (RestClient restClient = RestClient.builder(new HttpHost(Config.getElasticsearchHost(), Config.getElasticsearchPort(), Config.getElasticsearchScheme())).build()) {
            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            ElasticsearchClient esClient = new ElasticsearchClient(transport);
            var response = getSearchResponse(esClient);
            System.out.println("Event count: " + response.hits().total().value());

            // Print all documents in the index to debug
            printAllDocuments(esClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SearchResponse getSearchResponse(ElasticsearchClient esClient) throws IOException {
        IoTEventProducer producer = new IoTEventProducer();
        producer.sendEvent(Config.getKafkaTopic(), "device1", "{\"deviceId\":\"device1\",\"temperature\":25.5,\"timestamp\":1234567890}");
        producer.close();

        IoTEventConsumer consumer = new IoTEventConsumer(esClient);
        consumer.consumeAndIndex(Config.getKafkaTopic());
        consumer.close();

        RealTimeAnalytics analytics = new RealTimeAnalytics(esClient);
        var response = analytics.getEventCount("device1");
        return response;
    }

    private static void printAllDocuments(ElasticsearchClient esClient) throws IOException {
        System.out.println("--- Documents in index: " + Config.getElasticsearchIndex() + " ---");
        SearchResponse<Map> all = esClient.search(s -> s
            .index(Config.getElasticsearchIndex())
            .size(10), Map.class);
        if (all.hits().hits().isEmpty()) {
            System.out.println("(no documents)");
        } else {
            all.hits().hits().forEach(h -> System.out.println(h.source()));
        }
    }
}
