package dev.lydtech.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkSummary {
        private String name;
        private String codes;
        @Builder.Default
        private Long upCount=0L;
        @Builder.Default
        private Long downCount=0L;
        @Builder.Default
        private Long toggleCount=0L;
        private LinkStatusEnum status;
}
