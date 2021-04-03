package kafka.streams.product.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.streams.product.tracker.model.Tick;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TickProducer {

    Logger log = LoggerFactory.getLogger(TickProducer.class);

    ObjectMapper mapper = new ObjectMapper();
    Serde<Tick> tickSerde = new JsonSerde<>(Tick.class, mapper);

    @Value("${spring.cloud.stream.function.bindings.processticks-in-0.destination}")
    private String topic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, tickSerde.serializer().getClass());

        return props;
    }

    @Bean
    public ProducerFactory<String, Tick> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, Tick> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    public void sendDataToKafka(@RequestBody Tick data) {

        ListenableFuture<SendResult<String, Tick>> listenableFuture = kafkaTemplate().send(topic, data);


        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, Tick>>() {

            @Override
            public void onSuccess(SendResult<String, Tick> result) {
                log.info(String.format("Successfully sent data into topic [%s]", result.getProducerRecord().value()));
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Failed to send data to topic!", ex);
            }
        });
    }
}

