package com.example.quizsoccorso

data class QuizQuestion(
    val id: Int,
    val question: String,
    val category: String,
    val tags: List<String>,
    val difficulty: Int,
    val answers: List<String>,
    val correct: String,
    val explanation: String,
    val source: String
)