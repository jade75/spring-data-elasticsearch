package ie.d00216118.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "ie.d00216118.repositories")

//extends AbstractElasticsearchConfiguration{
public class ElasticsearchClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchClientConfig.class);

    public RestHighLevelClient client() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .build();
//        System.out.println("ClientConfiguration is starting ");
        logger.info("ClientConfiguration is starting ");
//        logger.trace("A TRACE Message");
//        logger.debug("A DEBUG Message");
//        logger.info("An INFO Message");
//        logger.warn("A WARN Message");
//        logger.error("An ERROR Message");
        return RestClients.create(clientConfiguration).rest();
    }




//    @Bean
//    public RestHighLevelClient client() {
//        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//                .connectedTo("localhost:9200")
//                .build();
//        System.out.println("ClientConfiguration is starting ");
//        logger.debug("ClientConfiguration is starting ");
//        return RestClients.create(clientConfiguration).rest();
//    }

//    @Bean
//    public ElasticsearchOperations elasticsearchTemplate() {
//        return new ElasticsearchRestTemplate(elasticsearchClient());
//    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        logger.debug("my elasticsearchTemplate is starting ");
        return new ElasticsearchRestTemplate(client());
    }
}
