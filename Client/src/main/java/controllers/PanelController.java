package controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import util.FileInfo;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class PanelController {

    protected TableView<FileInfo> table;
    protected TableColumn<FileInfo, String> typeFileColumn;
    protected TableColumn<FileInfo, String> fileNameColumn;
    protected TableColumn<FileInfo, Long> fileSizeColumn;
    protected TableColumn<FileInfo, String> fileDateColumn;

    protected TextField pathField;

    protected Path rootPath;

    public PanelController(TableView<FileInfo> table,
                           TableColumn<FileInfo, String> typeFileColumn,
                           TableColumn<FileInfo, String> fileNameColumn,
                           TableColumn<FileInfo, Long> fileSizeColumn,
                           TableColumn<FileInfo, String> fileDateColumn,
                           TextField pathField
    ) {
        this.table = table;
        this.typeFileColumn = typeFileColumn;
        this.fileNameColumn = fileNameColumn;
        this.fileSizeColumn = fileSizeColumn;
        this.fileDateColumn = fileDateColumn;
        this.pathField = pathField;
        initialize();
    }

    public void setRootPath(String rootPath) {
        this.rootPath = Paths.get(rootPath);
    }

    private void initialize() {
        typeFileColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        fileDateColumn.setCellValueFactory(new PropertyValueFactory<>("lastModified"));

        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else if (item == -1L) {
                    setText("-");
                } else {
                    String text = calculateSize(item);
                    setText(text);
                }
            }
        });
        setMouseOnTableAction();
    }

    public abstract void buttonPathUpAction(ActionEvent actionEvent);

    public abstract void updateList(Path path);

    public void updateList() {
        updateList(rootPath);
    }

    public abstract void setMouseOnTableAction();

    public String getCurrentPath() {
        return pathField.getText();
    }

    public String getSelectedFileName() {
        if (checkSelectedItem()) {
            return table.getSelectionModel().getSelectedItem().getFileName();
        }
        return null;
    }

    private boolean checkSelectedItem() {
        return table.isFocused() && table.getSelectionModel() != null && table.getSelectionModel().getSelectedItem() != null;
    }

    public Object getSelectedItem() {
        if (checkSelectedItem()) {
            return table.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    private String calculateSize(Long fileSize) {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            long sizeInKb = fileSize / 1024;
            return sizeInKb + " KB";
        } else if (fileSize < 1024 * 1024 * 1024) {
            long sizeInMb = fileSize / (1024 * 1024);
            return sizeInMb + " MB";
        } else {
            long sizeInGb = fileSize / (1024 * 1024 * 1024);
            return sizeInGb + " GB";
        }
    }
}
