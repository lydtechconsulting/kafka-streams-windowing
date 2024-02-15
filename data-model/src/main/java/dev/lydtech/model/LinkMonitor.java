package dev.lydtech.model;

import dev.lydtech.model.LinkStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkMonitor {
    private String name;
    private String ip;
    private Long downCount=0L;
    private String codes;
    private LinkStatusEnum status;
}
