package com.xyrfs.framework.exception.handler;

import com.xyrfs.bean.result.utils.v1.ResultUtil;
import com.xyrfs.common.enums.ResultEnum;
import com.xyrfs.common.exception.UpdateErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zxh
 */
@ControllerAdvice
@Slf4j
public class GlobalDefaultExceptionHandler {

    /**
     * 其他的错误
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<Object> defaultExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e){
        log.error("错误信息：",e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                 ResultUtil.NG(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ResultEnum.SYSTEM_ERROR,
                        e,
                        e.getMessage(),
                        request)
        );
    }

    /**
     * 更新出错时，设置返回的head，body
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = UpdateErrorException.class)
    @ResponseBody
    public ResponseEntity<Object> updateErrorExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e){
        log.error("错误信息：",e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ResultUtil.NG(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ResultEnum.SYSTEM_ERROR,
                e,
                e.getMessage(),
                request)
        );
    }
}
