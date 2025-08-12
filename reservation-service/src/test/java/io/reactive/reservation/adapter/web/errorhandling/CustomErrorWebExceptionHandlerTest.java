package io.reactive.reservation.adapter.web.errorhandling;

import io.reactive.reservation.adapter.web.errorhandling.CustomErrorWebExceptionHandlerTest.Config.TestController;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@WebFluxTest(controllers = TestController.class)
@Import(CustomErrorWebExceptionHandler.class)
class CustomErrorWebExceptionHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void decodingError() {
        webTestClient.get().uri("/decode-error")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errorCode").isEqualTo("BAD_REQUEST")
                .jsonPath("$.message").isEqualTo("Malformed JSON request");
    }

    @Test
    void httpMessageNotReadable() {
        webTestClient.get().uri("/http-message-not-readable")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errorCode").isEqualTo("BAD_REQUEST")
                .jsonPath("$.message").isEqualTo("Malformed JSON request");
    }

    @Test
    void serverWebInputException() {
        webTestClient.get().uri("/server-web-input")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errorCode").isEqualTo("BAD_REQUEST")
                .jsonPath("$.message").isEqualTo("Invalid request format or query parameter type")
                .jsonPath("$.debugMessage").value(msg -> assertTrue(((String) msg).contains("Missing required query parameter")));
    }

    @Test
    void unsupportedMediaTypeWithNullContentType() {
        webTestClient.get().uri("/unsupported-media-null")
                .exchange()
                .expectStatus().isEqualTo(415)
                .expectBody()
                .jsonPath("$.status").isEqualTo(415)
                .jsonPath("$.errorCode").isEqualTo("UNSUPPORTED_MEDIA_TYPE")
                .jsonPath("$.message").value(msg -> assertTrue(((String) msg).contains("null")));
    }

    @Test
    void responseStatusExceptionWithNullReason() {
        webTestClient.get().uri("/status-exception-no-reason")
                .exchange()
                .expectStatus().isEqualTo(403)
                .expectBody()
                .jsonPath("$.status").isEqualTo(403)
                .jsonPath("$.errorCode").isEqualTo("FORBIDDEN")
                .jsonPath("$.message").isEqualTo("Forbidden");
    }

    @Test
    void bindError() {
        webTestClient.get().uri("/bind-error")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.status").isEqualTo(422)
                .jsonPath("$.errorCode").isEqualTo("UNPROCESSABLE_ENTITY")
                .jsonPath("$.message").isEqualTo("Schema validation failed")
                .jsonPath("$.errors[0].field").isEqualTo("field");
    }

    @Test
    void unsupportedMediaType() {
        webTestClient.get().uri("/media-type-error")
                .exchange()
                .expectStatus().isEqualTo(415)
                .expectBody()
                .jsonPath("$.status").isEqualTo(415)
                .jsonPath("$.errorCode").isEqualTo("UNSUPPORTED_MEDIA_TYPE")
                .jsonPath("$.message").value(msg -> assertTrue(((String) msg).contains("not supported")));
    }

    @Test
    void methodNotAllowed() {
        webTestClient.get().uri("/method-not-allowed")
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectBody()
                .jsonPath("$.status").isEqualTo(405)
                .jsonPath("$.errorCode").isEqualTo("METHOD_NOT_ALLOWED")
                .jsonPath("$.message").value(msg -> assertTrue(((String) msg).contains("Request method")));
    }

    @Test
    void notAcceptable() {
        webTestClient.get().uri("/not-acceptable")
                .exchange()
                .expectStatus().isEqualTo(406)
                .expectBody()
                .jsonPath("$.status").isEqualTo(406)
                .jsonPath("$.errorCode").isEqualTo("NOT_ACCEPTABLE")
                .jsonPath("$.message").isEqualTo("Could not find acceptable representation");
    }

    @Test
    void statusException() {
        webTestClient.get().uri("/status-exception")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.errorCode").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Not found");
    }

    @Test
    void unexpectedError() {
        webTestClient.get().uri("/unexpected")
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.errorCode").isEqualTo("INTERNAL_SERVER_ERROR")
                .jsonPath("$.message").isEqualTo("Unexpected server error")
                .jsonPath("$.debugMessage").value(msg -> assertTrue(((String) msg).contains("Something went wrong")));
    }

    @TestConfiguration
    static class Config {

        @RestController
        static class TestController {

            @GetMapping("/decode-error")
            Mono<Void> decodingError() {
                return Mono.error(new DecodingException("Invalid JSON"));
            }

            @GetMapping("/http-message-not-readable")
            Mono<Void> httpMessageNotReadable() {
                return Mono.error(new HttpMessageNotReadableException("Unreadable message", new IOException()));
            }

            @GetMapping("/server-web-input")
            Mono<Void> serverWebInputException() {
                return Mono.error(new ServerWebInputException("Missing required query parameter"));
            }

            @GetMapping("/unsupported-media-null")
            Mono<Void> unsupportedMediaTypeNull() {
                return Mono.error(new UnsupportedMediaTypeStatusException(null));
            }

            @GetMapping("/status-exception-no-reason")
            Mono<Void> statusExceptionNoReason() {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
            }

            @GetMapping("/bind-error")
            Mono<Void> bindError() {
                var error = new WebExchangeBindException(
                        new MethodParameter(
                                ReflectionUtils.findMethod(TestController.class, "dummy", Dummy.class),
                                -1
                        ),
                        new BeanPropertyBindingResult(new Dummy(), "dummy")
                );
                error.addError(new FieldError("dummy", "field", "must not be null"));
                return Mono.error(error);
            }

            @GetMapping("/media-type-error")
            Mono<Void> mediaTypeError() {
                return Mono.error(new UnsupportedMediaTypeStatusException(MediaType.APPLICATION_PDF_VALUE));
            }

            @GetMapping("/method-not-allowed")
            Mono<Void> methodNotAllowed() {
                return Mono.error(new MethodNotAllowedException("POST", List.of(HttpMethod.GET)));
            }

            @GetMapping("/not-acceptable")
            Mono<Void> notAcceptable() {
                return Mono.error(new NotAcceptableStatusException(""));
            }

            @GetMapping("/status-exception")
            Mono<Void> statusException() {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
            }

            @GetMapping("/unexpected")
            Mono<Void> unexpected() {
                return Mono.error(new IllegalStateException("Something went wrong"));
            }

            public static class Dummy {
            }

            public void dummy(Dummy dummy) {
            }
        }
    }
}
