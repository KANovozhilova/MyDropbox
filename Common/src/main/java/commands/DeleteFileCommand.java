package commands;

import java.io.Serializable;

public class DeleteFileCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String fileName;

    public DeleteFileCommand(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
