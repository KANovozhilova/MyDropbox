package util;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private final FileType type;
    private final String typeName;
    private String fileName;
    private long size;
    private final String lastModified;

    public FileInfo(Path path) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            this.fileName = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
            this.typeName = type.getName();
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3)).format(dtf);
        } catch (IOException e) {
            throw new RuntimeException("Невозможно создать список файлов из папки" + path.toString());
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTypeName() {
        return typeName;
    }

    public FileType getType() {
        return type;
    }

    public Long getSize() {
        return size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public enum FileType {
        FILE("File"),
        DIRECTORY("Dir");

        private final String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

