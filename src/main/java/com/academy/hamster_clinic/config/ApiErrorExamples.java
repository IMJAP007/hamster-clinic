package com.academy.hamster_clinic.config;

public final class ApiErrorExamples {

    private ApiErrorExamples() {}

    public static final String UNAUTHORIZED = """
            {
              "error": "Unauthorized"
            }
            """;

    public static final String TOKEN_EXPIRED = """
            {
              "error": "Token Expired"
            }
            """;

    public static final String INVALID_TOKEN = """
            {
              "error": "Invalid token"
            }
            """;

    public static final String FORBIDDEN = """
            {
              "error": "Forbidden"
            }
            """;

    public static final String INTERNAL_ERROR = """
            {
              "timestamp": "2026-10-23T12:00:00+03:00",
              "status": 500,
              "error": "Internal Server Error",
              "message": "Внутренняя ошибка",
              "path": "/api/resource",
              "fieldErrors": null
            }
            """;
}
