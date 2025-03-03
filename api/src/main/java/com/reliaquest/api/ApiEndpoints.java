package com.reliaquest.api;

public enum ApiEndpoints {
    BASE_URL("http://localhost:8111/api/v1/employee/");

    private final String url;

    ApiEndpoints(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
