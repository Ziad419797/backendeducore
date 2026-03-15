package com.educore.dtocourse.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekSummaryResponse {

    private Long id;
    private String title;
    private Integer orderNumber;
    private boolean active;
    private boolean hasQuiz;

}
