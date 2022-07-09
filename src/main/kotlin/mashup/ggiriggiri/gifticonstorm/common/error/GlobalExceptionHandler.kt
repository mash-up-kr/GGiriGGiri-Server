package mashup.ggiriggiri.gifticonstorm.common.error

import mashup.ggiriggiri.gifticonstorm.common.dto.BaseResponse
import mashup.ggiriggiri.gifticonstorm.common.dto.ResponseCode
import mashup.ggiriggiri.gifticonstorm.common.error.exception.BaseException
import mashup.ggiriggiri.gifticonstorm.infrastructure.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object : Logger

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): BaseResponse<List<FieldError>> {
        val fieldErrors = FieldError.of(e.bindingResult)
        log.warn(e.message, e)
        return BaseResponse.error(ResponseCode.INVALID_INPUT_VALUE, fieldErrors)
    }

    @ExceptionHandler(BaseException::class)
    private fun handleBaseException(e: BaseException): ResponseEntity<BaseResponse<Unit>> {
        log.warn(e.message, e)
        return ResponseEntity.status(e.responseCode.status)
            .body(BaseResponse.error(e.responseCode))
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private fun handleException(e: Exception): BaseResponse<Unit> {
        log.error(e.message, e)
        return BaseResponse.error(ResponseCode.INTERNAL_SERVER_ERROR)
    }
}