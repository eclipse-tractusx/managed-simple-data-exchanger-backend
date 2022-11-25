package org.eclipse.tractusx.sde.portal.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalEntityData {
    private int totalElements;
    private int totalPages;
    private int page;
    private List<Content> content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {
        private double score;
        private LegalEntity legalEntity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LegalEntity {
        private String bpn;
        private List<Name> names;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Name {
        private String value;
        private Object shortName;
    }
}