package org.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Menu;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.MenuMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.MenusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/menu")
public class MenuController {
    @Resource
    MenuMapper menuMapper;
    @Resource
    UserMapper userMapper;

    public static final Logger LOGGER = LoggerFactory.getLogger(MenuController.class);

    private void updateMenuRole(Menu menu) {
        // 获取父级菜单
        Menu parentMenu = menuMapper.selectOne(Wrappers.<Menu>lambdaQuery().eq(Menu::getId, menu.getPid()));

        if (menu.getPid() == null) {
            // 当前menu是父级菜单，获取其所有子菜单
            List<Menu> submenus = menuMapper.selectList(Wrappers.<Menu>lambdaQuery().eq(Menu::getPid, menu.getId()));
            String parentRole = menu.getRole();

            // 遍历所有子菜单，更新它们的角色为父菜单的角色
            for (Menu submenu : submenus) {
                submenu.setRole(parentRole);
                menuMapper.updateById(submenu); // 更新子菜单角色
            }
            return;
        }
        if (parentMenu == null) {
            return; // 如果没有找到父级菜单，直接返回
        }
        // 当前是子菜单
        Integer parentId = parentMenu.getId();
        // Integer parentRole = parentMenu.getRole();
        // 查询父级菜单下的所有子菜单
        List<Menu> childrenMenus = menuMapper.selectList(Wrappers.<Menu>lambdaQuery().eq(Menu::getPid, parentId));

        Set<String> parentRoleSet = new HashSet<>();
        // 遍历所有子菜单，统计角色
        for (Menu childMenu : childrenMenus) {
            String childRole = childMenu.getRole();
            if (childRole != null && !childRole.isEmpty()) {
                String[] roleArray = childRole.split(",");
                for (String role : roleArray) {
                    parentRoleSet.add(role.trim());
                }
            }

        }
        StringBuilder parentRoles = new StringBuilder();
        for (String role : parentRoleSet) {
            if (parentRoles.length() > 0) {
                parentRoles.append(",");
            }
            parentRoles.append(role);
        }
        menu.setRole(String.valueOf(parentRoles));
        menuMapper.updateById(menu);

    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody Menu menu) {
        System.out.println(menu);

        Menu res = menuMapper.selectOne(Wrappers.<Menu>lambdaQuery().eq(Menu::getId, menu.getId()));
        if (res != null) {
            int i = menuMapper.updateById(menu);
            if (i > 0) {
                this.updateMenuRole(menu);
                return Result.success("更新成功");
            } else {
                return Result.error("-1", "更新失败");
            }
        } else {
            int insert = menuMapper.insert(menu);
            if (insert > 0) {
                this.updateMenuRole(menu);// 更新父级菜单
                return Result.success("插入成功");
            } else {
                return Result.error("-1", "插入失败");
            }
        }

    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable int id, @RequestBody Menu menu) {
        menu.setId(id);
        LOGGER.info("UPDATE menu:" + menu);
        int res = menuMapper.updateById(menu);
        if (res > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "修改失败");
        }
    }

    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        int b = menuMapper.deleteBatchIds(ids);
        if (b > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "删除失败");
        }

    }

    @DeleteMapping("/deleteById/{id}")
    public Result<?> deleteById(@PathVariable("id") Integer id) {
        int result = menuMapper.deleteById(id);
        if (result == 0) {
            return Result.error("-1", "删除失败，未找到指定的菜单项");
        }
        return Result.success("删除成功");
    }

    // 通过 ID 查询菜单项
    @GetMapping("/find/{id}")
    public Result<?> findById(@PathVariable("id") Integer id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            return Result.error("-1", "未找到指定的菜单项");
        }
        return Result.success(menu);
    }

    @GetMapping("/findAll")
    public Result<?> findAll() {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_num");

        List<Menu> menuList = menuMapper.selectList(queryWrapper);

        // 构造一级菜单及其子菜单
        List<Menu> parentList = new ArrayList<>();
        for (Menu menu : menuList) {
            if (menu.getPid() == null) { // 假设Pid为null表示是一级菜单
                List<Menu> children = new ArrayList<>();
                for (Menu subMenu : menuList) {
                    if (menu.getId().equals(subMenu.getPid())) {
                        children.add(subMenu);
                    }
                }
                if (!children.isEmpty()) {
                    menu.setChildren(children);
                }
                parentList.add(menu);
            }
        }

        return Result.success(parentList);
    }

    @GetMapping("/findParentMenus")
    public Result<?> findParentMenus(@RequestParam Integer currentPage,
            @RequestParam Integer size) {
        Page<Menu> page = new Page<>(currentPage, size);
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_num");

        // 只查询顶级菜单
        queryWrapper.isNull("pid");

        Page<Menu> menuPage = menuMapper.selectPage(page, queryWrapper);
        List<Menu> records = menuPage.getRecords();
        for (Menu record : records) {

            if (record.getPid() == null && record.getPath() == null) {
                record.setHasChildren(true);
            }
        }
        menuPage.setRecords(records);

        return Result.success(menuPage);
    }

    @GetMapping("/findChildrenMenus/{id}")
    public Result<?> findChildrenMenus(@PathVariable("id") Long parentId) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", parentId);
        queryWrapper.orderByAsc("sort_num");

        List<Menu> childrenList = menuMapper.selectList(queryWrapper);

        return Result.success(childrenList);
    }

    @GetMapping("/getMenuTree/{userId}")
    public Result<?> getMenuTree(@PathVariable Integer userId) {
        User user = userMapper.selectById(userId);
        LOGGER.info("userId:{}", userId);
        LOGGER.info(user.toString());

        if (ObjectUtils.isNull(user))
            return Result.error("-1", "用户不存在");
        List<Menu> roleMenuList = menuMapper.selectList(null);
        List<Menu> menus = MenusUtils.allocMenus(roleMenuList, user.getRole());
        return Result.success(menus);
    }

}