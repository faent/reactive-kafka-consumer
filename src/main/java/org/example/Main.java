package org.example;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final String TEST_TOPIC = "test-topic";

  public static void main(String[] args) {

    var props = new Properties();
    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
    props.put(CommonClientConfigs.GROUP_ID_CONFIG, "test-proj");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    ReceiverOptions<String, String> options = ReceiverOptions.<String, String>create(props)
        .subscription(List.of(TEST_TOPIC));
    SenderOptions<String, String> senderOptions = SenderOptions.create(props);

    KafkaReceiver<String, String> receiver = KafkaReceiver.create(options);
    KafkaSender<String, String> sender = KafkaSender.create(senderOptions);

    sender.send(
            Flux.just(
                SenderRecord.create(
                    TEST_TOPIC, 0, Instant.now()
                        .toEpochMilli(),
                    "key-1", "value-1", null
                ),
                SenderRecord.create(
                    TEST_TOPIC, 0, Instant.now()
                        .toEpochMilli(),
                    "key-2", "value-2", null
                )
            )
        )
        .blockLast();

    receiver.receive()
        .doOnNext(record -> logger.info("Received: {}", record.value()))
        .concatMap(record -> Mono.just(record)
            .doOnNext(r -> logger.info(
                "Acking record with trace_id: {}",
                new String(r.headers()
                               .lastHeader("x-datadog-trace-id")
                               .value())
            ))
        )
        .doOnNext(record -> record.receiverOffset()
            .acknowledge())
        .blockLast();
  }
}
