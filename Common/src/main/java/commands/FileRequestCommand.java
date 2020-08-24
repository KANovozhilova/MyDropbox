package commands;

import java.io.Serializable;

public class FileRequestCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String fileToDownload;

    public FileRequestCommand(String fileName) {
        this.fileToDownload = fileName;
    }

    public String getFileToDownload() {
        return fileToDownload;
    }
}
