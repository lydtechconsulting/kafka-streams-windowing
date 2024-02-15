package dev.lydtech.streams.windows.hopping.processor;

import dev.lydtech.model.Link;
import dev.lydtech.model.LinkSerde;
import dev.lydtech.model.LinkStatusEnum;
import dev.lydtech.model.LinkSummary;
import dev.lydtech.model.LinkSummarySerde;
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

class HoppingTest {

    StreamsBuilder streamsBuilder;

    Serde<Link> linkSerde = new LinkSerde();
    Serde<LinkSummary> linkSummarySerde = new LinkSummarySerde();
    Serde<String> stringSerde = new Serdes.StringSerde();

    TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Link> linkStatusInput;
    private TestOutputTopic<String, LinkSummary> hoppingOutput;

    @BeforeEach
    public void setup(){

        streamsBuilder = new StreamsBuilder();

        Hopping hopping = new Hopping(10L, 5L);
        hopping.process(streamsBuilder);
        final Topology topology = streamsBuilder.build();

        topologyTestDriver = new TopologyTestDriver(topology);
        linkStatusInput = topologyTestDriver.createInputTopic("link.status", stringSerde.serializer(),
                linkSerde.serializer());
        hoppingOutput = topologyTestDriver.createOutputTopic("link.hopping", stringSerde.deserializer(),
                linkSummarySerde.deserializer());
    }

    @AfterEach
    public void tearDown() {
         if (topologyTestDriver != null) {
             topologyTestDriver.close();
         }
    }

    @Test
    public void demonstrateFullWindowCapture(){

        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "a"), 0L);
        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "b"), 7L);
        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "c"), 10L);
        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "d"), 15L);
        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "z"), 30L);

        List<KeyValue<String, LinkSummary>> list = hoppingOutput.readKeyValuesToList();

        assert list.size() == 4;

        // The following assertions demonstrate the window grouping
        assert list.get(0).value.getCodes().equals("ab");
        assert list.get(1).value.getCodes().equals("bc");
        assert list.get(2).value.getCodes().equals("cd");
        assert list.get(3).value.getCodes().equals("d");

        // validation of the aggregation
        assert list.get(0).value.getUpCount() == 2L;
        assert list.get(1).value.getUpCount() == 2L;
    }

    @Test
    public void demonstrateFullWindowWithGapCapture(){

        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "a"), 7L);
        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "b"), 23L);

        linkStatusInput.pipeInput("testLink 1", createLink("testLink1", "z"), 100L);

        List<KeyValue<String, LinkSummary>> list = hoppingOutput.readKeyValuesToList();

        // The following assertions demonstrate the window grouping
        assert list.size() == 4;
        assert list.get(0).value.getCodes().equals("a");
        assert list.get(1).value.getCodes().equals("a");
        assert list.get(2).value.getCodes().equals("b");
        assert list.get(3).value.getCodes().equals("b");

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
