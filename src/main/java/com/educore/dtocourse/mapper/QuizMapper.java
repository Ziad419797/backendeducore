package com.educore.dtocourse.mapper;

import com.educore.dtocourse.response.QuizResponse;
import com.educore.dtocourse.response.QuestionResponse;
import com.educore.quiz.Quiz;
import com.educore.question.Question;
import com.educore.unit.Session;
import org.mapstruct.Mapper;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    default QuizResponse toResponse(Quiz quiz) {
        if (quiz == null) return null;

        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
//                .description(quiz.getDescription())
                .active(quiz.getActive())
                .deleted(quiz.getDeleted())
                .timeRestricted(quiz.getTimeRestricted())
                .durationMinutes(quiz.getDurationMinutes())
                .weekId(quiz.getWeek() != null ? quiz.getWeek().getId() : null)
                .courseId(getCourseIdFromQuiz(quiz))
//                .courseId(null)
                .questions(mapQuestions(quiz.getQuestions()))
                .totalMarks(calculateTotalMarks(quiz.getQuestions()))
                .questionsCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    default List<QuestionResponse> mapQuestions(java.util.Collection<Question> questions) {
        if (questions == null) return null;

        return questions.stream()
                .map(this::mapQuestion)
                .collect(Collectors.toList());
    }

    default QuestionResponse mapQuestion(Question question) {
        if (question == null) return null;

        return QuestionResponse.builder()
                .id(question.getId())
                .description(question.getDescription())
                .imageUrl(question.getImageUrl())
                .mark(question.getMark())
                .options(question.getOptions())
                .optionsCount(question.getOptionsCount())
                // ملاحظة: لا نرسل correctAnswer للطالب
                .build();
    }
    // ميثود مساعدة لتجنب الـ NullPointerException والـ Errors
    default Long getCourseIdFromQuiz(Quiz quiz) {
        try {
            // 1. التأكد إن الـ Quiz مربوط بـ Week
            if (quiz.getWeek() == null) return null;

            // 2. الوصول لأول Session مربوطة بالـ Week ده
            Session firstSession = quiz.getWeek().getSessions().iterator().next();

            // 3. الوصول لأول Course مربوط بالـ Session دي
            return firstSession.getCourses().iterator().next().getId();
        } catch (Exception e) {
            // لو مفيش جلسات أو مفيش كورسات هيرجع null بهدوء من غير ما يوقع البرنامج
            return null;
        }
    }

    default Integer calculateTotalMarks(java.util.Collection<Question> questions) {
        if (questions == null) return 0;
        return questions.stream()
                .mapToInt(q -> q.getMark() != null ? q.getMark() : 0)
                .sum();
    }

    default List<QuizResponse> toResponseList(List<Quiz> quizzes) {
        if (quizzes == null) return null;
        return quizzes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}