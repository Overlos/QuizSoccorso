package com.example.quizsoccorso

import com.google.gson.annotations.SerializedName

data class QuizQuestion(
    @SerializedName("id") val id: Int,
    @SerializedName("question") val question: String,
    @SerializedName("category") val category: String,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("difficulty") val difficulty: Int,
    @SerializedName("answers") val answers: List<String>,
    @SerializedName("correct") val correct: String,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("source") val source: String
)