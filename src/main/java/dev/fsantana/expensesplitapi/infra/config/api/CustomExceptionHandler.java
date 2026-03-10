package dev.fsantana.expensesplitapi.infra.config.api;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import dev.fsantana.expensesplitapi.domain.exceptions.AppEntityNotFound;
import dev.fsantana.expensesplitapi.domain.exceptions.AppException;
import dev.fsantana.expensesplitapi.domain.exceptions.AppRuleException;
import dev.fsantana.expensesplitapi.domain.exceptions.AppSecurityException;
import dev.fsantana.expensesplitapi.infra.config.api.dto.FieldProblem;
import dev.fsantana.expensesplitapi.infra.config.api.dto.ProblemType;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@ControllerAdvice
@RequiredArgsConstructor
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(AppEntityNotFound.class)
    public ResponseEntity<Object> handleAppEntityNotFound(AppEntityNotFound ex, WebRequest request) {
        HttpStatusCode statusCode = HttpStatus.NOT_FOUND;
        ProblemDetail problemDetail = this.createProblemDetail(statusCode,
            ProblemType.RECURSO_NAO_ENCONTRADO.getTitle(), ex.getMessage(), null);
        return this.handleExceptionInternal(ex, problemDetail, new HttpHeaders(), statusCode, request);
    }

    @ExceptionHandler(AppRuleException.class)
    public ResponseEntity<Object> handleAppRuleException(AppRuleException ex, WebRequest request) {
        HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;
        ProblemDetail problemDetail = this.createProblemDetail(statusCode,
                ProblemType.ERRO_NEGOCIO.getTitle(), ex.getMessage(), null);
        return this.handleExceptionInternal(ex, problemDetail, new HttpHeaders(), statusCode, request);
    }

    /**
    * AppSecurityException handle.
    */
    @ExceptionHandler({AppSecurityException.class })
    public ResponseEntity<Object> handleBusinessRuleException(AppSecurityException ex,
                                                       WebRequest request) {
        HttpStatusCode status = HttpStatus.UNAUTHORIZED;
        ProblemDetail problemDetail = this.createProblemDetail(status,
            ProblemType.ACESSO_NEGADO.getTitle(),
            ex.getMessage(), null);
        return this.handleExceptionInternal(ex, problemDetail, new HttpHeaders(), status, request);
    }

    @ExceptionHandler({ BadCredentialsException.class})
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex,
                                                              WebRequest request) {
        HttpStatusCode status = HttpStatus.UNAUTHORIZED;
        ProblemDetail problemDetail = this.createProblemDetail(status,
                ProblemType.ACESSO_NEGADO.getTitle(),
                ex.getMessage(), null);
        return this.handleExceptionInternal(ex, problemDetail, new HttpHeaders(), status, request);
    }

    @ExceptionHandler({  InternalAuthenticationServiceException.class })
    public ResponseEntity<Object> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex, WebRequest request) {
        HttpStatusCode status;
        String detail ;
        if (ex.getCause() instanceof AppEntityNotFound) {
            status = HttpStatus.UNAUTHORIZED;
            detail = "Bad credentials";
        } else {
            status = HttpStatus.FORBIDDEN;
            detail = ex.getMessage();
        }
        ProblemDetail problem = this.createProblemDetail(status, ProblemType.ACESSO_NEGADO.getTitle(),
                detail, null);
        return this.handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    /**
    * Custom method.
    */
    @ExceptionHandler({Exception.class, AppException.class})
    public ResponseEntity<Object> handlerUncaught(Exception ex, WebRequest request) {
        HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;
        String detail = "Ocorreu um erro interno inesperado no sistema. Tente novamente e se o "
            + "problema persistir, entre em contato com o administrador do sistema.";
        ProblemDetail problem = this.createProblemDetail(status, ProblemType.ERRO_DE_SISTEMA.getTitle(),
            detail, null);
        return this.handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
      HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.catching(ex);
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);
        String detail = "O corpo da requisição esta invalido. Verifique erro na sintaxe";
        if (rootCause instanceof InvalidFormatException invalidCause) {
          return handleInvalidFormatException( invalidCause, headers, status,
              request);
        } else if (rootCause instanceof PropertyBindingException bindCause) {
          return handlePropertyBindingException(bindCause, headers, status,
              request);
        }
        ProblemDetail problem = this.createProblemDetail(status,
            ProblemType.MENSAGEM_INCOMPREENSIVEL.getTitle(), detail, null);
        return this.handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return this.handlerInternalValidation(ex, ex.getBindingResult(), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String detail = "";
        if(!ex.getParameterValidationResults().isEmpty()) {
            detail = ex.getParameterValidationResults().getFirst().getResolvableErrors().getFirst().getDefaultMessage();
        } else {
            detail = "Um ou mais campos estão invalidos."
                    + " Faça o preenchimento correto e tente novamente";
        }

        ProblemDetail problem = this.createProblemDetail(status, ProblemType.DADOS_INVALIDOS.getTitle(),
                detail, null);

        return this.handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        String detail = "O metodo requisitado não e suportado";
        String title = "Metodo não permitido";
        ProblemDetail problemDetail = this.createProblemDetail(status, title, detail, null);
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    private ResponseEntity<Object> handlerInternalValidation(Exception ex,
                                                             BindingResult bindingResult, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detail = "Um ou mais campos estão invalidos."
            + " Faça o preenchimento correto e tente novamente";
        List<FieldProblem> fields = this.buildFields(bindingResult.getAllErrors());

        ProblemDetail problem = this.createProblemDetail(status, ProblemType.DADOS_INVALIDOS.getTitle(),
            detail, fields);

        return this.handleExceptionInternal(ex, problem, headers, status, request);
    }

    private ResponseEntity<Object> handlePropertyBindingException(PropertyBindingException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String path = joinPath(ex.getPath());
        String detail = String.format(
            "A propriedade '%s' não existe. " + "Corrija ou remova essa propriedade e tente novamente.",
            path);
        ProblemDetail problem = this.createProblemDetail(status,
        ProblemType.MENSAGEM_INCOMPREENSIVEL.getTitle(), detail, null);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    private ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex,
          HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String path = joinPath(ex.getPath());
        String detail = String.format("A propriedate '%s' recebeu o valor '%s'"
                + " que é de um tipo invalido. Corrija e informe um valor compativel com o tipo '%s'.",
            path, ex.getValue(), ex.getTargetType().getSimpleName());
        ProblemDetail problemdetail = this.createProblemDetail(status,
            ProblemType.MENSAGEM_INCOMPREENSIVEL.getTitle(), detail, null);
        return handleExceptionInternal(ex, problemdetail, headers, status, request);
    }

    private String joinPath(List<JsonMappingException.Reference> list) {
        return list.stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.joining("."));
    }


    private List<FieldProblem> buildFields(List<ObjectError> list) {
        return list.stream().map(objectError -> {
          String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
          String nome = objectError.getObjectName();
          if (objectError instanceof FieldError fieldError) {
            nome = fieldError.getField();
          }
          return FieldProblem.builder().name(nome).detail(message).build();
        }).toList();
    }

    private ProblemDetail createProblemDetail(HttpStatusCode statusCode, String title, String detail,
      List<FieldProblem> objects) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setProperty("timestamp", OffsetDateTime.now().toInstant().getEpochSecond());
        if (objects != null) {
          problemDetail.setProperty("objects", objects);
        }
        return problemDetail;
    }
}