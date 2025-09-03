package org.example.springboot.enumClass;

public enum FileType {
    // 文本文件
    TXT("text"),
    // PDF文件
    PDF("pdf"),
    // 图像文件
    IMG("img"),

    // 音频文件
    AUDIO("audio"),
    VIDEO("video"),
    COMMON("common");
    // 视频文件


    private final String typeName;

    FileType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}