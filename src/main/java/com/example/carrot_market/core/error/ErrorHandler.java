package com.example.carrot_market.core.error;

import com.example.carrot_market.core.base.BaseResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Log4j2
public class ErrorHandler {
    @ExceptionHandler(CommonError.class)
    private ResponseEntity<BaseResponseEntity<?>> handleMethodArgumentNotValidException(
            CommonError ex,
            ServletWebRequest request
    ) {
        if (ex instanceof CommonError.Unexpected) {
            ex.printStackTrace();
            log.error(formatException(ex, request, ex.httpStatus));
        }
        return ex.convertResponse();
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<BaseResponseEntity<?>> handleAllUnexpectedExceptions(
            Exception ex,
            ServletWebRequest request
    ) {
        ex.printStackTrace();
        log.error(formatException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR));
        return BaseResponseEntity.fail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    public String formatException(Exception ex, ServletWebRequest request, HttpStatus httpStatus) {
        String url = request.getDescription(false);
        String httpMethod = request.getHttpMethod().toString();
        String parameters = requestParameterToString(request.getRequest());  // 요청 파라미터를 문자열로 변환
        return String.format("""
                        Time: %s,
                        Message: %s,
                        Status: %s,
                        Method: %s,
                        URL: %s,
                        Params: %s
                        """,
                LocalDateTime.now(),
                ex.getMessage(),
                httpStatus,
                httpMethod,
                url,
                parameters);
    }

    private String requestParameterToString(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        return parameterMap.entrySet().stream().map(entry ->
                        entry.getKey() + "=" + String.join(",", entry.getValue()))
                .collect(Collectors.joining("&"));
    }
}
