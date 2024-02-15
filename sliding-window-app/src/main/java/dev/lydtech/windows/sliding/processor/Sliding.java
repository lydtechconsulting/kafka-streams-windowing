package dev.lydtech.windows.sliding.processor;

import dev.lydtech.model.Link;
import dev.lydtech.model.LinkMonitor;
import dev.lydtech.model.LinkMonitorSerde;
import dev.lydtech.model.LinkSerde;
import dev.lydtech.model.LinkStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.SlidingWindows;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class Sliding {


    private final Long windowDuration;
    private final Long windowGrace;
    private final Long linkThreshold;

    public Sliding(@Value("${sliding.processor.window.duration:10000}")Long windowDuration, @Value("${sliding.processor.window.grace:1000}")Long windowGrace, @Value("${sliding.processor.link.threshold:25}")Long linkThreshold){
        this.windowDuration = windowDuration;
        this.windowGrace = windowGrace;
        this.linkThreshold = linkThreshold;
    }
    @Autowired
    public void process(StreamsBuilder streamsBuilder) {

        Serde<Link> linkSerde = new LinkSerde();

        Serde<LinkMonitor> linkMonitorSerde = new LinkMonitorSerde();
        Serde<String> stringSerde = new Serdes.StringSerde();

        streamsBuilder.stream("link.status", Consumed.with(stringSerde, linkSerde))
                .peek((k, v) -> log.info("Mapped event: {} : {}", k, v))
                .groupByKey()
                .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofMillis(windowDuration), Duration.ofMillis(windowGrace)))
                .aggregate(() -> new LinkMonitor(),
                        this::aggregate,
                        Materialized.<String, LinkMonitor, WindowStore<Bytes, byte[]>>as("sliding-window-link-store")
                                .withKeySerde(stringSerde)
                                .withValueSerde(linkMonitorSerde)
                )
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), value))
                .filter((key, linkMonitor) -> linkMonitor.getDownCount() >= linkThreshold )
                .peek((k, v) -> log.info("sliding peeky: {} : {}", k, v))
                .to("link.sliding", Produced.with(stringSerde, linkMonitorSerde));
    }

    private LinkMonitor aggregate(String key, Link link, LinkMonitor linkMonitor) {
        Long downCount = linkMonitor.getDownCount();
        String codes = linkMonitor.getCodes();

        if (codes == null) {
            codes = "";
        }
        codes=codes.concat(link.getCode());

        if (link.getStatus() == LinkStatusEnum.DOWN) {
            downCount++;
        } else {
            downCount=0L;
        }

        LinkMonitor newLinkMonitor = LinkMonitor.builder()
                .name(link.getName())
                .ip(link.getIp())
                .downCount(downCount)
                .status(link.getStatus())
                .codes(codes)
                .build();

        log.info("Aggregated link monitor: {}", newLinkMonitor);
        return newLinkMonitor;
    }
}
