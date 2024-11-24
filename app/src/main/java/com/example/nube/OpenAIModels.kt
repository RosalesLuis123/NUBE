package com.example.nube

data class OpenAIRequest(
    val model: String,
    val prompt: String,
    val max_tokens: Int
)

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val text: String
)
