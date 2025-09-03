package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.enumClass.FileType;
import org.example.springboot.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@RequestMapping("/file")
@RestController
public class FileController {

    @Autowired
    private FileService fileService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);
    @PostMapping("/upload/img")
    public Result<?> upLoad(@RequestParam("file") MultipartFile file) {
      return   fileService.upLoad(file, FileType.IMG);
    }
    @PostMapping("/uploadMultiple")
    public Result<?> uploadMultiple(@RequestParam("files") List<MultipartFile> files) {
        List<String> strings = fileService.uploadMultiple(files);
        return !strings.isEmpty() ? Result.success(strings):Result.error("-1","文件上传失败");
    }
}





