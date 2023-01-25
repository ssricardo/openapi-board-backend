package io.rss.apicenter.server.web.provider

data class AppValidationError(val cause: String, val code: Int = 0) {
    val errorOrigin = "OaBoard"

    constructor(cause: String, code: ErrorCodeType) : this(cause, code.code) {
    }
}

enum class ErrorCodeType (val code: Int) {
    GENERAL_ERROR(0),
    CONSTRAINT_VIOLATION(1),
    VALIDATION_FAIL(2),
    ;
}