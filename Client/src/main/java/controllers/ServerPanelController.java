package controllers;

import commands.ErrorCommand;
import commands.FilesListCommand;
import commands.GetFilesListCommand;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import services.NetworkClient;
import util.FileInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ServerPanelController extends PanelController {

    public ServerPanelController(TableView<FileInfo> table,
                                 TableColumn<FileInfo, String> typeFileColumn,
                                 TableColumn<FileInfo, String> fileNameColumn,
                                 TableColumn<FileInfo, Long> fileSizeColumn,
                                 TableColumn<FileInfo, String> fileDateColumn,
                                 TextField pathField) {
        super(table, typeFileColumn, fileNameColumn, fileSizeColumn, fileDateColumn, pathField);
    }

    @Override
    public void buttonPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    @Override
    public void updateList(Path path) {
        NetworkClient.getInstance().sendCommandToServer(new GetFilesListCommand(path));
        System.out.println("Начало чтения команды с сервера");
        Object receivedCommand = NetworkClient.getInstance().readCommandFromServer();
        System.out.println("Команда с сервера прочитана");
        if (receivedCommand instanceof FilesListCommand) {
            System.out.println("Получены файлы");
            FilesListCommand command = (FilesListCommand) receivedCommand;
            pathField.setText(command.getCurrentServerPath());
            table.getItems().clear();
            table.getItems().addAll(command.getFilesList());
            table.sort();
        } else if (receivedCommand instanceof ErrorCommand) {
            String message = ((ErrorCommand) receivedCommand).getErrorMessage();
            Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void updateList(List<FileInfo> filesList) {
        pathField.setText(rootPath.toString());
        table.getItems().clear();
        table.getItems().addAll(filesList);
        table.sort();
    }

    @Override
    public void setMouseOnTableAction() {
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (table.getSelectionModel().getSelectedItem().getType() == FileInfo.FileType.DIRECTORY) {
                    Path path = Paths.get(pathField.getText()).resolve(table.getSelectionModel().getSelectedItem().getFileName());
                    updateList(path);
                }
            }
        });
    }
}
