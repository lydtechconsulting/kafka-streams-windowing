package dev.lydtech.streams.windows.hopping.processor;


import dev.lydtech.model.Link;
import dev.lydtech.model.LinkSerde;
import dev.lydtech.model.LinkStatusEnum;
import dev.lydtech.model.LinkSummary;
import dev.lydtech.model.LinkSummarySerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class Hopping {

    private final Long windowDuration;
    private final Long windowAdvance;

    public Hopping (@Value("${hopping.processor.window.duration:10000}")Long windowDuration, @Value("${hopping.processor.window.advance:5000}") Long windowAdvance){
        this.windowDuration = windowDuration;
        this.windowAdvance = windowAdvance;
    }

    @Autowired
    public void process(StreamsBuilder streamsBuilder) {

        Serde<Link> linkSerde = new LinkSerde();

        Serde<LinkSummary> linkSummarySerde = new LinkSummarySerde();
        Serde<String> stringSerde = new Serdes.StringSerde();

        streamsBuilder.stream("link.status", Consumed.with(stringSerde, linkSerde))
                .peek((k, v) -> log.info("Mapped event: {} : {}", k, v))
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMillis(windowDuration)).advanceBy(Duration.ofMillis(windowAdvance)))
                .aggregate(() -> new LinkSummary(),
                        this::aggregate,
                        Materialized.<String, LinkSummary, WindowStore<Bytes, byte[]>>as("hopping-window-link-store")
                                .withKeySerde(stringSerde)
                                .withValueSerde(linkSummarySerde)
                )
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), value))
                .peek((k, v) -> log.info("hopping peeky: {} : {}", k, v))
                .to("link.hopping", Produced.with(stringSerde, linkSummarySerde));
    }


    private LinkSummary aggregate(String key, Link link, LinkSummary linkSummary) {

        Long upCount = linkSummary.getUpCount();
        Long downCount = linkSummary.getDownCount();
        Long toggleCount = linkSummary.getToggleCount();
        String codes = linkSummary.getCodes();
        LinkStatusEnum status = linkSummary.getStatus();

        if (codes == null) {
            codes = "";
        }
        codes=codes.concat(link.getCode());

        if (link.getStatus() == LinkStatusEnum.DOWN) {
            downCount++;
        } else {
            upCount++;
        }

        // first window status will be null so do not increment toggleCount
        // if the status has changed increment toggleCount
        if (status != null && link.getStatus() != status) {
            toggleCount++;
        }

        LinkSummary newLinkSummary = LinkSummary.builder()
                .name(link.getName())
                .downCount(downCount)
                .upCount(upCount)
                .codes(codes)
                .toggleCount(toggleCount)
                .status(link.getStatus())
                .build();

        return newLinkSummary;
    }
}
