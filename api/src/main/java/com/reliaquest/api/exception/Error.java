package com.reliaquest.api.exception;

import java.time.LocalDateTime;
import java.util.List;

public record Error(String path, List<String> message, int statusCode, LocalDateTime localDateTime) {}
