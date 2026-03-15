package com.educore.assignment.assignmentQuestion;

import com.educore.common.CacheNames;
import com.educore.common.SortValidator;
import com.educore.dtocourse.request.CreateAssignmentQuestionRequest;
import com.educore.dtocourse.response.AssignmentQuestionResponse;
import com.educore.exception.ResourceNotFoundException;
import com.educore.assignment.Assignment;
import com.educore.assignment.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentQuestionService {

    private final AssignmentQuestionRepository questionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SortValidator sortValidator;

    private static final List<String> ALLOWED_SORT = List.of("id", "mark");

    // ================= ADD QUESTION TO ASSIGNMENT =================

    @CacheEvict(value = {CacheNames.ASSIGNMENT_QUESTIONS_BY_ASSIGNMENT}, allEntries = true)
    @Transactional
    public AssignmentQuestionResponse addQuestion(Long assignmentId, CreateAssignmentQuestionRequest request) {

        log.info("Adding question to assignment: {}", assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("الواجب غير موجود بالرقم: " + assignmentId));

        AssignmentQuestion question = AssignmentQuestion.builder()
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .mark(request.getMark())
                .options(request.getOptions()) // إضافة الخيارات
                .correctAnswer(request.getCorrectAnswer())
                .assignment(assignment)
                .build();

        questionRepository.save(question);

        log.info("Question added successfully to assignment {} with id: {}", assignmentId, question.getId());

        return AssignmentQuestionResponse.builder()
                .id(question.getId())
                .imageUrl(question.getImageUrl())
                .mark(question.getMark())
                .description(question.getDescription())
                .options(question.getOptions())
                .optionsCount(question.getOptions() != null ? question.getOptions().size() : 0)
                .build();
    }

    // ================= GET ALL BY ASSIGNMENT =================

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.ASSIGNMENT_QUESTIONS_BY_ASSIGNMENT,
            key = "#assignmentId + '-' + #pageable.pageNumber")
    public Page<AssignmentQuestionResponse> getQuestions(Long assignmentId, Pageable pageable) {

        log.info("Fetching questions for assignment: {}, page: {}", assignmentId, pageable.getPageNumber());

        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("الواجب غير موجود بالرقم: " + assignmentId);
        }

        sortValidator.validate(pageable, ALLOWED_SORT);

        Page<AssignmentQuestion> page = questionRepository.findByAssignmentId(assignmentId, pageable);

        return page.map(q -> AssignmentQuestionResponse.builder()
                .id(q.getId())
                .imageUrl(q.getImageUrl())
                .mark(q.getMark())
                .description(q.getDescription())
                .options(q.getOptions())
                .optionsCount(q.getOptions() != null ? q.getOptions().size() : 0)
                .build()
        );
    }

    // ================= GET SINGLE QUESTION =================

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.ASSIGNMENT_QUESTIONS, key = "#questionId")
    public AssignmentQuestionResponse getQuestion(Long questionId) {

        log.info("Fetching assignment question with id: {}", questionId);

        AssignmentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("السؤال غير موجود بالرقم: " + questionId));

        return AssignmentQuestionResponse.builder()
                .id(question.getId())
                .imageUrl(question.getImageUrl())
                .mark(question.getMark())
                .description(question.getDescription())
                .options(question.getOptions())
                .optionsCount(question.getOptions() != null ? question.getOptions().size() : 0)
                .build();
    }

    // ================= UPDATE QUESTION =================

    @CacheEvict(value = {CacheNames.ASSIGNMENT_QUESTIONS, CacheNames.ASSIGNMENT_QUESTIONS_BY_ASSIGNMENT}, allEntries = true)
    @Transactional
    public AssignmentQuestionResponse updateQuestion(Long questionId, CreateAssignmentQuestionRequest request) {

        log.info("Updating assignment question with id: {}", questionId);

        AssignmentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("السؤال غير موجود بالرقم: " + questionId));

        if (request.getImageUrl() != null) question.setImageUrl(request.getImageUrl());
        if (request.getDescription() != null) question.setDescription(request.getDescription());
        if (request.getMark() != null) question.setMark(request.getMark());
        if (request.getOptions() != null) question.setOptions(request.getOptions());
        if (request.getCorrectAnswer() != null) question.setCorrectAnswer(request.getCorrectAnswer());

        AssignmentQuestion updated = questionRepository.save(question);

        return AssignmentQuestionResponse.builder()
                .id(updated.getId())
                .imageUrl(updated.getImageUrl())
                .mark(updated.getMark())
                .description(updated.getDescription())
                .options(updated.getOptions())
                .build();
    }

    // ================= DELETE QUESTION =================

    @CacheEvict(value = {CacheNames.ASSIGNMENT_QUESTIONS, CacheNames.ASSIGNMENT_QUESTIONS_BY_ASSIGNMENT}, allEntries = true)
    @Transactional
    public void deleteQuestion(Long questionId) {
        log.info("Deleting assignment question with id: {}", questionId);
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("السؤال غير موجود بالرقم: " + questionId);
        }
        questionRepository.deleteById(questionId);
    }

    // ================= DELETE ALL BY ASSIGNMENT =================

    @CacheEvict(value = {CacheNames.ASSIGNMENT_QUESTIONS, CacheNames.ASSIGNMENT_QUESTIONS_BY_ASSIGNMENT}, allEntries = true)
    @Transactional
    public void deleteAllByAssignment(Long assignmentId) {
        log.info("Deleting all questions for assignment: {}", assignmentId);
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("الواجب غير موجود بالرقم: " + assignmentId);
        }
        questionRepository.deleteByAssignmentId(assignmentId);
    }
}