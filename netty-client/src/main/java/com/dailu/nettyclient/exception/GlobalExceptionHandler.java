package com.dailu.nettyclient.exception;

import com.dailu.nettyclient.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


/**
 * 捕获异常
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String FORBIDDEN = "403";
    private static final String LOGIN = "login";
    private static final String ERROR = "500";
    private static final MediaType PLAIN = MediaType.parseMediaType("text/plain;charset=utf-8");


    /**
     * CustomerException 异常捕获
     */
    @ExceptionHandler({CustomException.class})
    @ResponseBody
    public Object handle(Exception ex, HttpServletRequest req) {
        log.error("业务异常", ex);
        String reason = ex.getMessage();
        if (isAjax(req)) {
            return R.fail(reason);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(PLAIN)
                .body(reason);
    }

    @ExceptionHandler({AcquireResultTimeoutException.class})
    @ResponseBody
    public Object handle(AcquireResultTimeoutException ex, HttpServletRequest req) {
        log.error("获取结果超时异常:", ex);
        String reason = ex.getMessage();
        if (isAjax(req)) {
            return R.fail(reason);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(PLAIN)
                .body(reason);
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ResponseEntity<String> handleBindException(BindException ex) {
        log.trace("参数异常", ex);
        FieldError fieldError = ex.getFieldError();
        String reason = fieldError == null ? ex.getBindingResult().toString() : fieldError.getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(PLAIN)
                .body(reason);
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object commonHandler(Exception ex, HttpServletRequest req) {
        log.debug("接口异常:{}", ex.getMessage(), ex);
        String reason = "接口异常,请联系管理员";
        if (isAjax(req)) {
            return R.fail(reason);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(PLAIN)
                .body(reason);
    }

    private boolean isAjax(HttpServletRequest req) {
        return "XMLHttpRequest".equalsIgnoreCase(req.getHeader("x-requested-with"));
    }
}
