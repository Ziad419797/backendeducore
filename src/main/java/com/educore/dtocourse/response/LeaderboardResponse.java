package com.educore.dtocourse.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {
    private String title;
    private String type; // QUIZ, COURSE, GLOBAL
    private Long entityId;
    private List<LeaderboardEntryResponse> entries;
    private Integer totalParticipants;
    private Double averageScore;
    private Integer highestScore;
    private Integer lowestScore;
}