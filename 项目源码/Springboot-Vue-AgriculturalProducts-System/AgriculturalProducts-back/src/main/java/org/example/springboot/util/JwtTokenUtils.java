package org.example.springboot.util;


import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.springboot.entity.User;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class JwtTokenUtils {
    private static UserService staticUserService;
    @Resource
    private  UserService userService;
    public static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtils.class);
@PostConstruct
    public void setUserService() {
        staticUserService=userService;
    }
    public static String genToken(String userId,String sign){
    return JWT.create().withAudience(userId).withExpiresAt(DateUtil.offsetHour(new Date(),2)).sign(Algorithm.HMAC256(sign));
    }
    public static User getCurrentUser(){
        String token=null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            token = request.getHeader("token");
            if (StringUtils.isNotBlank(token)) {
                token = request.getParameter("token");
            }
            if (StringUtils.isBlank(token)) {
                LOGGER.error("获取当前登录的token失败，token{}", token);
            }

            String userId = JWT.decode(token).getAudience().get(0);
            return staticUserService.getUserById(Integer.parseInt(userId));
        }catch (Exception e){
            LOGGER.error("获取当前用户信息失败，token{}",token,e);
            return null;
        }
    }
}
