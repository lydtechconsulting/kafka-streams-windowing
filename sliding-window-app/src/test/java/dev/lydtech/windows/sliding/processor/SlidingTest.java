package dev.lydtech.windows.sliding.processor;

import dev.lydtech.model.Link;
import dev.lydtech.model.LinkMonitor;
import dev.lydtech.model.LinkMonitorSerde;
import dev.lydtech.model.LinkSerde;
import dev.lydtech.model.LinkStatusEnum;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;


class SlidingTest {
    StreamsBuilder streamsBuilder;

    Serde<Link> linkSerde = new LinkSerde();
    Serde<LinkMonitor> linkMonitorSerde = new LinkMonitorSerde();
    Serde<String> stringSerde = new Serdes.StringSerde();

    TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Link> linkStatusInput;
    private TestOutputTopic<String, LinkMonitor> slidingOutput;

    @BeforeEach
    public void setup(){

        streamsBuilder = new StreamsBuilder();

        Sliding sliding = new Sliding(30L, 0L, 0L);
        sliding.process(streamsBuilder);
        final Topology topology = streamsBuilder.build();

        topologyTestDriver = new TopologyTestDriver(topology);
        linkStatusInput = topologyTestDriver.createInputTopic("link.status", stringSerde.serializer(),
                linkSerde.serializer());
        slidingOutput = topologyTestDriver.createOutputTopic("link.sliding", stringSerde.deserializer(),
                linkMonitorSerde.deserializer());
    }

    @AfterEach
    public void tearDown() {
        if (topologyTestDriver != null) {
            topologyTestDriver.close();
        }
    }


    @Test
    public void demonstrateFullWindowCapture(){

        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "a", LinkStatusEnum.DOWN), 31L);
        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "b", LinkStatusEnum.DOWN), 40L);

        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "c", LinkStatusEnum.DOWN), 65L);
        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "d", LinkStatusEnum.DOWN), 75L);

        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "z", LinkStatusEnum.DOWN), 300L);

        List<KeyValue<String, LinkMonitor>> list = slidingOutput.readKeyValuesToList();

        assert list.size() == 8;

        // The following assertions demonstrate the window grouping
        assert list.get(0).value.getCodes().equals("a");
        assert list.get(1).value.getCodes().equals("ab");
        assert list.get(2).value.getCodes().equals("b");
        assert list.get(3).value.getCodes().equals("bc");
        assert list.get(4).value.getCodes().equals("c");
        assert list.get(5).value.getCodes().equals("cd");
        assert list.get(6).value.getCodes().equals("d");
        assert list.get(7).value.getCodes().equals("z");
    }



    //
    // Link Test Helpers
    //
    private Link createLink(String name, String code) {
        return createLink(name, code, LinkStatusEnum.UP);
    }

    private Link createLink(String name, String code, LinkStatusEnum status) {
        Link link = Link.builder()
                .name(name)
                .ip("127.0.0.1")
                .code(code)
                .status(status)
                .bias(1.0)
                .build();
        return link;
    }
}
