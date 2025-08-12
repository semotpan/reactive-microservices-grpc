package io.reactive.reservation.adapter.web.errorhandling;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(-2)
@Slf4j
@RequiredArgsConstructor
final class CustomErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var response = exchange.getResponse();
        var bufferFactory = response.bufferFactory();

        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        var message = "Unexpected server error";
        var debugMessage = "";
        List<ApiErrorResponse.ApiErrorField> errors = List.of();

        // --- Malformed JSON
        if (ex instanceof DecodingException || ex instanceof JsonParseException || ex instanceof HttpMessageNotReadableException) {
            status = HttpStatus.BAD_REQUEST;
            message = "Malformed JSON request";
        }

        // --- Input binding / validation
        else if (ex instanceof ServerWebInputException inputEx) {
            status = HttpStatus.BAD_REQUEST;
            message = "Invalid request format or query parameter type";

            if (inputEx instanceof WebExchangeBindException bindEx) {
                status = HttpStatus.UNPROCESSABLE_ENTITY;
                message = "Schema validation failed";
                errors = bindEx.getFieldErrors().stream()
                        .map(this::toApiErrorField)
                        .collect(Collectors.toList());
            } else {
                debugMessage = inputEx.getMessage();
            }
        }

        // --- Unsupported Media Type
        else if (ex instanceof UnsupportedMediaTypeStatusException mediaEx) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            message = "Content type '" + mediaEx.getContentType() + "' is not supported";
        }

        // --- Method Not Allowed
        else if (ex instanceof MethodNotAllowedException methodEx) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            message = "Request method '" + methodEx.getHttpMethod() + "' is not supported";
            response.getHeaders().setAllow(methodEx.getSupportedMethods());
        }

        // --- Not Acceptable
        else if (ex instanceof NotAcceptableStatusException) {
            status = HttpStatus.NOT_ACCEPTABLE;
            message = "Could not find acceptable representation";
        }

        // --- Status exception (e.g. 404, 403)
        else if (ex instanceof ResponseStatusException statusEx) {
            status = HttpStatus.resolve(statusEx.getStatusCode().value());
            message = statusEx.getReason() != null ? statusEx.getReason() : status.getReasonPhrase();
        }

        // --- Default debug fallback
        if (debugMessage.isEmpty() && status == HttpStatus.INTERNAL_SERVER_ERROR) {
            debugMessage = ex.getMessage();
        }

        // --- Build and write API error
        var apiError = ApiErrorResponse.builder()
                .httpStatus(status)
                .message(message)
                .debugMessage(debugMessage)
                .errors(errors)
                .build();

        try {
            byte[] json = objectMapper.writeValueAsBytes(apiError);
            response.setStatusCode(status);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(bufferFactory.wrap(json)));
        } catch (Exception encodingEx) {
            log.error("Failed to write error response", encodingEx);
            return Mono.error(encodingEx);
        }
    }

    private ApiErrorResponse.ApiErrorField toApiErrorField(FieldError error) {
        return new ApiErrorResponse.ApiErrorField(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
        );
    }
}
