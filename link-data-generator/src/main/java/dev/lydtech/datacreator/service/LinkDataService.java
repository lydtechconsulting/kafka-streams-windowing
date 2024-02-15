package dev.lydtech.datacreator.service;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.lydtech.model.Link;
import dev.lydtech.model.LinkStatusEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class LinkDataService {

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private Faker faker;
    private Random random;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public LinkDataService() {
        faker = new Faker();
        random = new Random();
    }

    @PostConstruct
    public void process() throws Exception {
        log.info("Initializing the producer - Link gubbins to follow...");

        Integer numberOfLinks = 5;

        List<Link> links = generateLink(numberOfLinks);

        while (true) {
            for (Link link : links) {
                LinkStatusEnum newStatus = upBias(link.getBias());

                // status is still down or transitioning to up
                if (newStatus == LinkStatusEnum.DOWN ||
                        (newStatus == LinkStatusEnum.UP && link.getStatus() == LinkStatusEnum.DOWN)) {
                    link.setStatus(newStatus);

                    String jsonLinkStatus = convertToJson(link);

                    String key = link.getName();
                    log.info("Sending link: {}: {} ", key, jsonLinkStatus);
                    kafkaTemplate.send("link.status", key, jsonLinkStatus).get();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing.  swallow the exception
            }
        }
    }

    private LinkStatusEnum upBias(double bias) {
        if (random.nextDouble() < bias) {
            return LinkStatusEnum.UP;
        }

        return LinkStatusEnum.DOWN;
    }


    private List<Link> generateLink(Integer number) {
        List<Link> links = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            Link link = new Link().builder()
                    .name(faker.animal().name())
                    .ip(faker.internet().ipV4Address())
                    .status(LinkStatusEnum.UP)
                    .bias(random.nextDouble())
                    .code("-")
                    .build();
            log.info(link.toString());
            links.add(link);
        }
        return links;
    }



    private static <T> String convertToJson(T generatedDataItem) {
        return gson.toJson(generatedDataItem);
    }

}
