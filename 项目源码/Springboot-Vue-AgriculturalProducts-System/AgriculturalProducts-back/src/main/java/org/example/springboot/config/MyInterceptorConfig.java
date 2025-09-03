package org.example.springboot.config;

import org.example.springboot.util.FileUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MyInterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path projectRootPath = null;
        projectRootPath = Paths.get(FileUtil.FILE_BASE_PATH);
        String imgFolderPath = projectRootPath.resolve("img").toAbsolutePath().toString();
        registry.addResourceHandler("/api/img/**").addResourceLocations("file:" + imgFolderPath + "/");
    }

}
