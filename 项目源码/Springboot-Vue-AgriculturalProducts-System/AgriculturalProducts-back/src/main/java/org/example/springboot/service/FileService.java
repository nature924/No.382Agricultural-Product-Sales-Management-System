package org.example.springboot.service;


import io.micrometer.common.util.StringUtils;
import org.example.springboot.common.Result;
import org.example.springboot.enumClass.FileType;
import org.example.springboot.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    public Result<?> upLoad(MultipartFile file,FileType fileType) {
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(file.getOriginalFilename())) {
            LOGGER.error("文件不存在");
            return Result.error("-1", "文件不存在！");
        }
        LOGGER.info("upload FILE:" + file.getOriginalFilename());
        String path = FileUtil.saveFile(file,null,fileType.getTypeName());
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(path)) {
            return Result.success(path);
        } else {
            return Result.error("-1", "文件上传失败");
        }
    }
    public Result<?> fileRemove(@PathVariable String filename){
        String filePath="\\img\\"+filename;

        boolean res = FileUtil.deleteFile(filePath);

        return res? Result.success():Result.error("-1","删除失败");

    }

    public List<String> uploadMultiple(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            LOGGER.error("没有文件上传");
            return null;
        }

        List<String> successPaths = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                if (StringUtils.isEmpty(file.getOriginalFilename())) {
                    failedFiles.add(file.getOriginalFilename() + ": 文件不存在");
                    continue;
                }
                LOGGER.info("upload FILE:" + file.getOriginalFilename());
                String path = FileUtil.saveFile(file,null,FileType.COMMON.getTypeName());
                if (StringUtils.isNotBlank(path)) {
                    successPaths.add(path);
                } else {
                    failedFiles.add(file.getOriginalFilename() + ": 文件上传失败");
                }
            } catch (Exception e) {
                LOGGER.error("文件上传时发生异常 " + e.getMessage(), e);
                failedFiles.add(file.getOriginalFilename() + ": 文件上传时发生异常");
            }
        }

        // 检查是否所有文件都成功上传
        if (!failedFiles.isEmpty()) {
            // 如果有文件上传失败，删除所有成功上传的文件
            for (String path : successPaths) {
                File uploadedFile = new File(path);
                if (uploadedFile.exists() && uploadedFile.isFile()) {
                    if (uploadedFile.delete()) {
                        LOGGER.info("Deleted successfully uploaded file: " + path);
                    } else {
                        LOGGER.warn("Failed to delete file: " + path);
                    }
                }
            }

            // 返回错误信息
            return null;
        } else {
            // 如果全部成功，则只返回成功路径
            return successPaths;
        }
    }
}
