package com.ciny.studynote.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class RestApiException {
    private String errorMessage;
    private HttpStatus status;
}
