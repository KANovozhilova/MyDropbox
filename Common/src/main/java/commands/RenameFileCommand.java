package commands;

import java.io.Serializable;

public class RenameFileCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String oldFileName;
    private final String newFileName;

    public RenameFileCommand(String oldFileName, String newFileName) {
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    public String getOldFileName() {
        return oldFileName;
    }

    public String getNewFileName() {
        return newFileName;
    }
}
