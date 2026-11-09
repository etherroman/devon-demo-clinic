package dev.devon.clinic.api;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant timestamp, int status, String code, String message, String path, Map<String, String> fields) {}
