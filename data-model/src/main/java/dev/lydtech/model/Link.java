package dev.lydtech.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Link {

        private String name;
        private String ip;
        private String code;
        private LinkStatusEnum status=LinkStatusEnum.UP;
        private double bias;


    public static Link.LinkBuilder builder(Link copy){
        Link.LinkBuilder linkBuilder = new Link.LinkBuilder();
        linkBuilder.name = copy.name;
        linkBuilder.ip = copy.ip;
        linkBuilder.code = copy.code;
        linkBuilder.status = copy.status;
        linkBuilder.bias = copy.bias;
        return linkBuilder;
    }

    public static Link.LinkBuilder builder(){
        return new Link.LinkBuilder();
    }

}
