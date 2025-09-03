package org.example.springboot.controller.email;



import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.User;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;
import java.util.Random;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/email")
public class SendEmailController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendEmailController.class);

    @Resource
    JavaMailSender javaMailSender;
    @Value("${user.fromEmail}")
    private String FROM_EMAIL;
    @Resource
    UserService userService;


    @GetMapping("/sendEmail/{email}")
    public Result<?> emailRegister(@PathVariable String email) throws GeneralSecurityException {

      if (userService.getByEmail(email) != null) return Result.error("-1", "邮箱已被注册");


        Random random = new Random();
        int code = random.nextInt(899999) + 100000;

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
//        MimeMailMessage mimeMailMessage = new MimeMailMessage(sess);

        simpleMailMessage.setFrom(FROM_EMAIL);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("注册验证码");
        simpleMailMessage.setText("邮箱验证码为：" + code + "，请勿转发给他人");
        try {

            javaMailSender.send(simpleMailMessage);
            LOGGER.info("邮件已发送：" + simpleMailMessage.getText());
    return Result.success(code);

        } catch (Exception e) {
            LOGGER.error("邮件发送异常：" + e.getMessage());
            return Result.error("-1", "验证码发送异常，请联系管理员");
        }
//        return Result.success("123456");

    }

    @GetMapping("/findByEmail/{email}")
    public Result<?> findByEmail(@PathVariable String email) {
        LOGGER.info("FIND BY EMAIL:" + email );


                User user = userService.getByEmail(email);
                if (user == null) return Result.error("-1", "邮箱不存在");



        // 生成随机验证码
        Random random = new Random();
        int code = random.nextInt(899999) + 100000;

        // 构建邮件消息
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(FROM_EMAIL);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("找回密码验证码");
        simpleMailMessage.setText("您的找回密码验证码为：" + code + "，有效期5分钟，请勿泄露给他人");

        try {
            // 发送邮件
            javaMailSender.send(simpleMailMessage);
            LOGGER.info("找回密码邮件已发送：" + simpleMailMessage.getText());
            // 这里可以将验证码和用户id存储在redis等缓存中，设置5分钟过期时间
            // 例如：cache.put(user.getId(), code, 300);
            return Result.success(code);
        } catch (Exception e) {
            LOGGER.error("找回密码邮件发送异常：" + e.getMessage());
            return Result.error("-1", "邮件发送异常，请联系管理员");
        }
    }

}
