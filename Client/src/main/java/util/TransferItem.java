package util;

import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;

import java.nio.file.Path;

public class TransferItem {

    public enum Operation {
        DOWNLOAD,
        UPLOAD
    }

    private final Operation operation;
    private ImageView operationImage;
    private final Path sourceFile;
    private final String fileName;
    private final Path dstFile;
    private final ProgressIndicator progressIndicator;
    private final Button infoButton;
    private final Button deleteItemButton;
    private final Button goToFileButton;
    private boolean isSuccess = false;

    public TransferItem(Operation operation, Path sourceFile, Path dstFile) {
        this.operation = operation;
        if (operation.equals(Operation.DOWNLOAD)) {
            operationImage = new ImageView("image/download.png");
        } else if (operation.equals(Operation.UPLOAD)) {
            operationImage = new ImageView("image/upload.png");
        }
        this.sourceFile = sourceFile;
        this.fileName = dstFile.toString() + "\\" + sourceFile.getFileName();
        this.dstFile = dstFile;
        this.progressIndicator = new ProgressIndicator();
        this.infoButton = new Button("Инфо");
        this.deleteItemButton = new Button("", new ImageView("image/delete.png"));
        this.goToFileButton = new Button("", new ImageView("image/link.png"));
        disableButtons();
    }

    public Path getSourceFile() {
        return sourceFile;
    }

    public ImageView getOperationImage() {
        return operationImage;
    }

    public Button getGoToFileButton() {
        return goToFileButton;
    }

    public Path getDstFile() {
        return dstFile;
    }

    public Button getInfoButton() {
        return infoButton;
    }

    public Button getDeleteItemButton() {
        return deleteItemButton;
    }

    public String getFileName() {
        return fileName;
    }

    public Operation getOperation() {
        return operation;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(double progressValue) {
        progressIndicator.setProgress(progressValue);
    }

    public void disableButtons() {
        deleteItemButton.setDisable(true);
        goToFileButton.setDisable(true);
        infoButton.setDisable(true);
    }

    public void enableButtons() {
        deleteItemButton.setDisable(false);
        goToFileButton.setDisable(false);
        infoButton.setDisable(false);
    }
}
