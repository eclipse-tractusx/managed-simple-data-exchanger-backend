package com.catenax.dft.model.legalEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalEntityData {
    public int totalElements;
    public int totalPages;
    public int page;
    public ArrayList<Content> content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {
        public double score;
        public LegalEntity legalEntity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LegalEntity {
        public String bpn;
        public ArrayList<Name> names;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Name {
        public String value;
        public Object shortName;
    }
}




