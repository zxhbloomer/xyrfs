package com.xyrfs.bean.result.utils.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyrfs.common.constant.FsConstant;
import com.xyrfs.common.enums.ResultEnum;
import com.xyrfs.common.exception.ValidateCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * response返回值工具类
 * @author zxh
 */
public class ResponseResultUtil {

//    public static void responseWriteError(ObjectMapper objectMapper,
//                                          HttpServletRequest request,
//                                          HttpServletResponse response,
//                                          AuthenticationException exception,
//                                          int httpStatus
//                                          ) throws IOException, ServletException {
//        responseWriteError(objectMapper,request,response,exception);
//        response.setStatus(httpStatus);
//
//    }

    public static void responseWriteOK(Object data, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        response.getWriter().write(objectMapper.writeValueAsString(
            ResultUtil.OK(data)
        ));
    }

    /**
     *  response 错误时返回
     * @param request
     * @param response
     * @param exception
     * @param httpStatus       http的status
     * @param system_code      系统错误code
     * @param errorMessage
     * @throws IOException
     */
    public static void responseWriteError(
                                            HttpServletRequest request,
                                            HttpServletResponse response,
                                            Exception exception,
                                            int httpStatus,
                                            ResultEnum system_code,
                                            String errorMessage
                                        ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType(FsConstant.JSON_UTF8);
        if(exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException){
            response.getWriter().write(objectMapper.writeValueAsString(
                    ResultUtil.NG(HttpStatus.UNAUTHORIZED.value(),
                                    system_code,
                                    exception,
                                    errorMessage,
                                    request)
                            )
            );
        }else if(exception instanceof ValidateCodeException){
            response.getWriter().write(objectMapper.writeValueAsString(
                    ResultUtil.NG(httpStatus,
                                    system_code,
                                    exception,
                                    errorMessage,
                                    request)
            ));
        }else{
            response.getWriter().write(objectMapper.writeValueAsString(
                    ResultUtil.NG(httpStatus,
                                    system_code,
                                    exception,
                                    errorMessage,
                                    request)
            ));
        }

        response.setStatus(httpStatus);
    }
}
