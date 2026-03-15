package com.educore.lessonmaterial;

public enum MaterialType {
    PDF("ملف PDF"),
    VIDEO("ملف فيديو"),
    IMAGE("صورة"),
    DOC("مستند DOC"),
    PPT("عرض PPT"),
    AUDIO("ملف صوتي"),
    ARCHIVE("ملف مضغوط"),
    OTHER("ملف آخر");

    private final String arabicName;

    MaterialType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }

    public boolean isDocument() {
        return this == PDF || this == DOC || this == PPT;
    }

    public boolean isVideo() {
        return this == VIDEO;
    }

    public boolean isImage() {
        return this == IMAGE;
    }

    public boolean isAudio() {
        return this == AUDIO;
    }

    public boolean isArchive() {
        return this == ARCHIVE;
    }

    // طريقة للمساعدة في تحديد نوع الملف من الامتداد
    public static MaterialType fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return OTHER;
        }

        String ext = extension.toLowerCase();
        switch (ext) {
            case "pdf":
                return PDF;
            case "mp4", "avi", "mov", "wmv", "flv", "mkv":
                return VIDEO;
            case "jpg", "jpeg", "png", "gif", "bmp", "webp":
                return IMAGE;
            case "doc", "docx":
                return DOC;
            case "ppt", "pptx":
                return PPT;
            case "mp3", "wav", "aac", "flac":
                return AUDIO;
            case "zip", "rar", "7z", "tar", "gz":
                return ARCHIVE;
            default:
                return OTHER;
        }
    }

    // طريقة للمساعدة في تحديد نوع الملف من اسم الملف
    public static MaterialType fromFileName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return OTHER;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return fromExtension(extension);
    }
}
