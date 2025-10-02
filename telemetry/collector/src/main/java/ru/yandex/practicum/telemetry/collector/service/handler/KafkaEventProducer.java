package ru.yandex.practicum.telemetry.collector.service.handler;

import jakarta.annotation.PreDestroy;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;

import java.util.Properties;

@Component
public class KafkaEventProducer {

    private Producer<String, SpecificRecordBase> producer;

    public Producer<String, SpecificRecordBase> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Ждем подтверждения от всех реплик
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // Количество повторных попыток
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Для строгой ordering

        config.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Ждем до 10ms для батчинга
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Размер батча 16KB



        producer = new KafkaProducer<>(config);
    }

    @PreDestroy
    public void destroy() {
        stop();  // Закрываем при остановке приложения
    }

    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }
}
