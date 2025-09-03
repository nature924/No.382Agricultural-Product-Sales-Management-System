package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.User;
import org.example.springboot.entity.UserPasswordUpdate;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }
    @GetMapping("/username/{username}")
    public Result<?> getUserByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) {
        return userService.login(user);

    }
    @PutMapping("/password/{id}")
    public Result<?> updatePassword(@PathVariable int id, @RequestBody UserPasswordUpdate userPasswordUpdate) {
        boolean success = userService.updatePassword(id, userPasswordUpdate);
        if (success) {
            return Result.success();
        } else {
            return Result.error("-1", "密码修改失败");
        }
    }
    @GetMapping("/forget")
    public Result<?> forgetPassword(@RequestParam String email, @RequestParam String newPassword) {
        boolean success = userService.forgetPassword(email, newPassword);
        if (success) {
            return Result.success();
        } else {
            return Result.error("-1", "忘记密码操作失败");
        }
    }
    @GetMapping("/page")
    public Result<?> getUsersByPage(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "") String status,

            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> page = userService.getUsersByPage(username, name, role, status, currentPage, size);
        return Result.success(page);
    }
    @GetMapping("/role/{role}")
    public Result<?> getUserByRole(@PathVariable String role) {
        List<User> users = userService.getUserByRole(role);
        if (users != null && !users.isEmpty()) {
            return Result.success(users);
        } else {
            return Result.error("-1", "未找到该角色的用户");
        }
    }
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        Integer res = userService.deleteBatch(ids);
        if (res>0) {
            return Result.success();
        } else {
            if(res==-1)return Result.error("-1", "删除失败,请检查关联商家");
            if(res==-2)return Result.error("-2","删除失败，请检查关联库存");
            return Result.error("-1", "删除失败");
        }
    }
    @GetMapping
    public Result<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users != null && !users.isEmpty()) {
            return Result.success(users);
        } else {
            return Result.error("-1", "未找到用户");
        }
    }
    @PostMapping("/add")
    public Result<?> createUser(@RequestBody User user) {
        int res = userService.createUser(user);
        if (res == -1) return Result.error("-1", "用户名已存在");
        if (res == -2) return Result.error("-1", "邮箱已存在！");
        if (res > 0) {
            return Result.success(user);
        } else {
            return Result.error("-1", "创建用户失败");
        }
    }
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        boolean success = userService.updateUser(id, user);
        if (success) {
            return Result.success(user);
        } else {
            return Result.error("-1", "更新失败");
        }
    }
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteUserById(@PathVariable int id) {
        Integer res = userService.deleteUserById(id);
        if (res>0) {
            return Result.success();
        } else {
            LOGGER.info("delete res:{}",res);
            if(res==-1)return Result.error("-1", "删除失败,请检查关联商家");
            if(res==-2)return Result.error("-2","删除失败，请检查关联库存");
            return Result.error("-1", "删除失败");
        }
    }
}
