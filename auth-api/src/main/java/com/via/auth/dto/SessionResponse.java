package com.via.auth.dto;

import java.util.List;

public record SessionResponse(String email, List<String> authorities) {}
