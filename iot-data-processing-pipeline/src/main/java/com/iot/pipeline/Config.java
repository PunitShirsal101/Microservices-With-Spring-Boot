package com.iot.pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getKafkaBootstrapServers() {
        return properties.getProperty("kafka.bootstrap.servers", "localhost:9092");
    }

    public static String getKafkaTopic() {
        return properties.getProperty("kafka.topic", "iot-events");
    }

    public static String getElasticsearchHost() {
        return properties.getProperty("elasticsearch.host", "localhost");
    }

    public static int getElasticsearchPort() {
        return Integer.parseInt(properties.getProperty("elasticsearch.port", "9200"));
    }

    public static String getElasticsearchScheme() {
        return properties.getProperty("elasticsearch.scheme", "http");
    }

    public static String getElasticsearchIndex() {
        return properties.getProperty("elasticsearch.index", "iot-events");
    }
}
