package org.example.springboot.util;


import org.example.springboot.entity.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenusUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MenusUtils.class);
    public static List<Menu> allocMenus(List<Menu> allMenuList, String role) {
        List<Menu> childMenus = new ArrayList<>();
        List<Menu> parentMenus = new ArrayList<>();
        LOGGER.info("allMenu"+allMenuList.toString());
        for (Menu menu : allMenuList) {
            String roles = menu.getRole();

            if(role==null){
                if (menu.getPid() == null) {
                    parentMenus.add(menu);
                } else {
                    childMenus.add(menu);
                }
                continue;
            }
            if (roles != null && !roles.isEmpty()) {
                String[] roleArray = roles.split(",");
                for (String roleItem : roleArray) {
                    if (roleItem.equals(role)) {
                        if (menu.getPid() == null) {
                            parentMenus.add(menu);
                        } else {
                            childMenus.add(menu);
                        }
                        break; // 匹配到角色后，不需要继续检查其他角�?
                    }
                }
            }
        }

        for (Menu parentMenu : parentMenus) {
            List<Menu> childrenList = childMenus.stream()
                    .filter(menu -> parentMenu.getId().equals(menu.getPid()))
                    .collect(Collectors.toList());
            parentMenu.setChildren(childrenList);
        }
        LOGGER.info(parentMenus.toString());
        return parentMenus;
    }
}
