package com.educore.dtocourse.mapper;

import com.educore.dtocourse.request.CreateQuizRequest;
import com.educore.dtocourse.request.CreateQuestionRequest;
import com.educore.quiz.Quiz;
import com.educore.question.Question;
import org.mapstruct.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {WeekMapper.class})
public interface QuizCreateMapper {

    default Quiz toEntity(CreateQuizRequest request) {
        if (request == null) return null;

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .durationMinutes(request.getDurationMinutes())
                .active(true)
                .deleted(false)
                .timeRestricted(false)
                .build();

        // تحويل الأسئلة
        if (request.getQuestions() != null) {
            quiz.setQuestions(
                    request.getQuestions().stream()
                            .map(q -> toQuestionEntity(q, quiz))
                            .collect(Collectors.toSet())
            );
        }

        return quiz;
    }

    default Question toQuestionEntity(CreateQuestionRequest request, Quiz quiz) {
        if (request == null) return null;

        return Question.builder()
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .correctAnswer(request.getCorrectAnswer())
                .mark(request.getMark())
                .quiz(quiz)
                .build();
    }
}