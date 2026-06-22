package com.pangeranvalerensco.orchestria.auth_service.service;

import com.pangeranvalerensco.orchestria.auth_service.payload.request.SessionDemoLoginRequest;
import com.pangeranvalerensco.orchestria.auth_service.payload.response.SessionDemoResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface SessionDemoService {
    SessionDemoResponse login(SessionDemoLoginRequest request, HttpServletRequest httpRequest);
    SessionDemoResponse getProfile(HttpServletRequest httpRequest);
    SessionDemoResponse getStatus(HttpServletRequest httpRequest);
    SessionDemoResponse logout(HttpServletRequest httpRequest);
}
