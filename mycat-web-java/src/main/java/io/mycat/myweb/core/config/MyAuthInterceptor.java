package io.mycat.myweb.core.config;

import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import io.mycat.myweb.core.service.TokenService;
import io.mycat.myweb.core.service.UserService;

/**
 * 权限验证，基于Token
 */
@Component
public class MyAuthInterceptor extends HandlerInterceptorAdapter {
    Logger log = LoggerFactory.getLogger(MyAuthInterceptor.class);
    @Autowired
    UserService userService;
    @Autowired
    TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            Object object) throws Exception {
    	 // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
//        HandlerMethod handlerMethod = (HandlerMethod) object;
//        Method method = handlerMethod.getMethod();

        // OPTIONS请求类型直接返回不处理
        if ("OPTIONS".equals(httpServletRequest.getMethod())) {
            return false;
        }
        String token = httpServletRequest.getHeader("token");// 从 http 请求头中取出 token
        System.out.println(token);
        // 检查有没有需要用户权限的注解
        if (needToken(httpServletRequest)) {
            // 执行认证
            if (token == null) {
            	httpServletResponse.sendError(401,"无token，请重新登录");
//                throw new RuntimeException("无token，请重新登录");
                return false;
            }
            // 验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(tokenService.getTokenSecret())).build();
            try {
                jwtVerifier.verify(token);
            } catch (JWTVerificationException e) {
            	httpServletResponse.sendError(403,"无token，请重新登录");
//                throw new RuntimeException("401");
                return false;
            }
            // 获取 token 中的 user id
            Long userId;
            try {
                userId = Long.valueOf(JWT.decode(token).getAudience().get(0));
            } catch (JWTDecodeException j) {
              	httpServletResponse.sendError(403,"无token，请重新登录");
//              throw new RuntimeException("401");
              return false;
            }
            log.info("success checked token for {} ",userId);
            return true;
        }
        return true;

    }

    private boolean needToken(HttpServletRequest httpServletRequest) {
       String uri =  httpServletRequest.getRequestURI();
       if ("/login".equals(uri)) {
           return false;
       }else{
           return true;
       }

    }
}