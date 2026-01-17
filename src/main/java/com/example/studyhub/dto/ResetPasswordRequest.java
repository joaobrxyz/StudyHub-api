package com.example.studyhub.dto;

public record ResetPasswordRequest(String token, String newPassword) {
}
