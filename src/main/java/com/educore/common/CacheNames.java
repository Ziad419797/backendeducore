package com.educore.common;

public interface CacheNames {
    // ================= COURSE RELATED =================
    String COURSES = "courses";
    String COURSES_PAGES = "courses_pages";
    String COURSES_BY_CATEGORY = "courses_by_category";

    String LESSONS = "lessons";
    String LESSONS_PAGES = "lessons_pages";
    String LESSONS_BY_SESSION = "lessons_by_session";

    String MATERIALS = "materials";
    String MATERIALS_PAGES = "materials_pages";

    String SESSIONS = "sessions";
    String SESSIONS_PAGES = "sessions_pages";

    String CATEGORIES = "categories";
    String CATEGORIES_PAGES = "categories_pages";
    String SESSIONS_BY_COURSE = "sessions_by_course"; // إذا كانت موجودة
    // ================= QUIZ RELATED =================
    String QUIZZES = "quizzes";
    String QUIZZES_BY_WEEK = "quizzes_by_week";

    String QUESTIONS = "questions";
    String QUESTIONS_BY_QUIZ = "questions_by_quiz";
    // ================= ASSIGNMENT RELATED (NEW) =================
    String ASSIGNMENTS = "assignments";
    String ASSIGNMENTS_BY_WEEK = "assignments_by_week";

    String ASSIGNMENT_QUESTIONS = "assignment_questions";
    String ASSIGNMENT_QUESTIONS_BY_ASSIGNMENT = "assignment_questions_by_assignment";

    // ================= LEADERBOARD RELATED (NEW) =================
    String LEADERBOARD_QUIZ = "leaderboard_quiz";
    String LEADERBOARD_COURSE = "leaderboard_course";
    String LEADERBOARD_GLOBAL = "leaderboard_global";
}