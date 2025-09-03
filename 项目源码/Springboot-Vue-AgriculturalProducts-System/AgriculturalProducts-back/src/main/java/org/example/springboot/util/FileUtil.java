package org.example.springboot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    private final static  Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    public final static String FILE_BASE_PATH = System.getProperty("user.dir") + "/files/";
    // 获取项目根目录路�?
    public static Path getProjectRootPath() throws IOException {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:.");
        if (resources.length == 0) {
            throw new IOException("Cannot find project root path.");
        }
        // 通常第一个资源就是项目的根目�?
        File rootDir = resources[0].getFile();
        return rootDir.toPath();
    }

    // 公共的文件保存方�?
    public static String saveFile(MultipartFile file, String folderName, String baseDir) {
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        long timestamp = System.currentTimeMillis();
        String extension = ""; // 文件扩展名，默认为空

        // 获取文件扩展�?
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String dFileName = timestamp + extension;

        // 获取项目根目录路�?
        Path projectRootPath = null;
        try {
            projectRootPath = Paths.get(FILE_BASE_PATH);
            Path fileDirectory = projectRootPath.resolve(baseDir);

            // 如果folderName不为null，则在指定目录后面加入folderName
            if (folderName != null && !folderName.isEmpty()) {
                fileDirectory = fileDirectory.resolve(folderName);
            }

            if (!Files.exists(fileDirectory)) {
                Files.createDirectories(fileDirectory); // 如果目录不存在，则创建目�?
            }
            Path uploadFilePath = fileDirectory.resolve(dFileName);
            File uploadFile = uploadFilePath.toFile();

            file.transferTo(uploadFile);
            LOGGER.info("File saved at: {}", uploadFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // 返回相对路径，以便在Web应用中访�?
        String relativePath = "/" + baseDir + "/" + (folderName != null && !folderName.isEmpty() ? folderName + "/" : "") + dFileName;
        return relativePath;
    }

    // 保存图片的方�?
    public static String saveImage(MultipartFile file, String folderName) {
        return saveFile(file, folderName, "img");
    }

    // 保存视频的方�?
    public static String saveVideo(MultipartFile file, String folderName) {
        return saveFile(file, folderName, "videos");
    }
    /**
     * 根据文件名删除文�?
     *
     * @param filename 文件名（相对于项目根目录的相对路径）
     * @return 删除成功返回 true，否则返�?false
     */
    public static boolean deleteFile(String filename) {
        try {
            // 获取文件的绝对路�?
            Path filePath = Paths.get(FILE_BASE_PATH, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                LOGGER.info("File deleted: {}", filePath);
                return true;
            } else {
                LOGGER.warn("File not found: {}", filePath);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting file: {}", filename, e);
            return false;
        }
    }

    public static void writeToFile(String fileName, String content) throws IOException {
        // 创建文件对象
        File file = new File(fileName);

        // 获取并打印文件的绝对路径
        System.out.println("Writing to file: " + file.getAbsolutePath());

        // 使用 try-with-resources 确保 FileWriter 在使用完毕后自动关闭
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        }

    }
}
