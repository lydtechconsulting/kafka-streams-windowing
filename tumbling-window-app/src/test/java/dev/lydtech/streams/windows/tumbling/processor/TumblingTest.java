package dev.lydtech.streams.windows.tumbling.processor;

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

class TumblingTest {

    StreamsBuilder streamsBuilder;

    Serde<Link> linkSerde = new LinkSerde();
    Serde<LinkSummary> linkSummarySerde = new LinkSummarySerde();
    Serde<String> stringSerde = new Serdes.StringSerde();

    TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Link> linkStatusInput;
    private TestOutputTopic<String, LinkSummary> tumblingOutput;

    @BeforeEach
    public void setup(){

        streamsBuilder = new StreamsBuilder();

        Tumbling tumbling = new Tumbling(10L, 0L);
        tumbling.process(streamsBuilder);
        final Topology topology = streamsBuilder.build();

        topologyTestDriver = new TopologyTestDriver(topology);
        linkStatusInput = topologyTestDriver.createInputTopic("link.status", stringSerde.serializer(),
                linkSerde.serializer());
        tumblingOutput = topologyTestDriver.createOutputTopic("link.tumbling", stringSerde.deserializer(),
                linkSummarySerde.deserializer());
    }

    @AfterEach
    public void tearDown() {
         if (topologyTestDriver != null) {
             topologyTestDriver.close();
         }
    }

    @Test
    public void demonstrateTumblingWindowSingleKey(){

        linkStatusInput.pipeInput("Link 1", createLink("Link1", "a"), 2L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "b"), 6L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "c"), 10L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "d"), 16L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "e"), 32L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "f"), 35L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "z"), 40);

        List<KeyValue<String, LinkSummary>> list = tumblingOutput.readKeyValuesToList();

        assert list.size() == 3;

        // The following assertions demonstrate the window grouping
        assert list.get(0).value.getCodes().equals("ab");
        assert list.get(1).value.getCodes().equals("cd");
        assert list.get(2).value.getCodes().equals("ef");


        // The following assertions demonstrate the window aggregation
        assert list.get(0).value.getUpCount() == 2;
        assert list.get(0).value.getDownCount() == 0;
    }



    @Test
    public void demonstrateTumblingWindowMultipleKeys(){

        linkStatusInput.pipeInput("Link 1", createLink("Link1", "a"), 0L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "b"), 6L);
        linkStatusInput.pipeInput("Link 2", createLink("Link2", "a"), 4L);
        linkStatusInput.pipeInput("Link 2", createLink("Link2", "b"), 6L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "c"), 12L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "d"), 16L);
        linkStatusInput.pipeInput("Link 1", createLink("Link1", "e"), 32L);

        List<KeyValue<String, LinkSummary>> list = tumblingOutput.readKeyValuesToList();

        assert list.size() == 3;

        // The following assertions demonstrate the window grouping
        assert list.get(0).value.getCodes().equals("ab");
        assert list.get(0).value.getName().equals("Link1");
        assert list.get(1).value.getCodes().equals("ab");
        assert list.get(1).value.getName().equals("Link2");
        assert list.get(2).value.getCodes().equals("cd");


        // The following assertions demonstrate the window aggregation
        assert list.get(0).value.getUpCount() == 2;
        assert list.get(0).value.getDownCount() == 0;
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
