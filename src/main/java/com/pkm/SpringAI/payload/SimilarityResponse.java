package com.pkm.SpringAI.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimilarityResponse {

    private String similarity;
    private String score;
}
