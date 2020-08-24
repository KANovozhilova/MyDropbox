package commands;

import util.FileInfo;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public class FilesListCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<FileInfo> filesList;
    private final String currentServerPath;
    private String rootServerPath;

    public FilesListCommand(List<FileInfo> filesList, Path currentPath) {
        this.filesList = filesList;
        this.currentServerPath = currentPath.toString();
    }

    public List<FileInfo> getFilesList() {
        return filesList;
    }

    public String getCurrentServerPath() {
        return currentServerPath;
    }

    public String getRootServerPath() {
        return rootServerPath;
    }

    public void setRootServerPath(Path rootServerPath) {
        this.rootServerPath = rootServerPath.toString();
    }
}
