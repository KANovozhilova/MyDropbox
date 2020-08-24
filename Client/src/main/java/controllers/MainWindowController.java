package controllers;

import commands.ErrorCommand;
import commands.FilesListCommand;
import commands.GetFilesListCommand;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import services.NetworkClient;
import util.FileInfo;
import util.TransferItem;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    private ClientPanelController clientTable;
    private ServerPanelController serverTable;

    @FXML
    private Button refreshButtonServer;

    @FXML
    private Button refreshButtonClient;

    @FXML
    private ComboBox<String> logicalDisksBox;

    @FXML
    private TextField clientPathField;

    @FXML
    private Button pathUpButton;

    @FXML
    private TextField serverPathField;

    @FXML
    private TableView<FileInfo> clientTableView;

    @FXML
    private TableColumn<FileInfo, String> iconColumnClient;

    @FXML
    private TableColumn<FileInfo, String> nameColumnClient;

    @FXML
    private TableColumn<FileInfo, Long> sizeColumnClient;

    @FXML
    private TableColumn<FileInfo, String> dateColumnClient;


    @FXML
    private TableView<FileInfo> serverTableView;

    @FXML
    private TableColumn<FileInfo, String> iconColumnServer;

    @FXML
    private TableColumn<FileInfo, String> nameColumnServer;

    @FXML
    private TableColumn<FileInfo, Long> sizeColumnServer;

    @FXML
    private TableColumn<FileInfo, String> dateColumnServer;

    @FXML
    private Button pathUpButtonServer;

    @FXML
    private Button downloadButton;

    @FXML
    private Button uploadButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button renameButton;

    @FXML
    private TableView<TransferItem> operationTable;

    @FXML
    private TableColumn<TransferItem, String> operationColumn;

    @FXML
    private TableColumn<TransferItem, Button> infoColumn;

    @FXML
    private TableColumn<TransferItem, String> fileNameColumn;

    @FXML
    private TableColumn<TransferItem, ProgressIndicator> progressColumn;

    @FXML
    private TableColumn<TransferItem, Button> goToFileColumn;

    @FXML
    private TableColumn<TransferItem, Button> deleteItemColumn;

    private OperationTableController operationTableController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setTooltips();
        setContextMenusToTables();
        clientTable = new ClientPanelController(
                clientTableView,
                iconColumnClient,
                nameColumnClient,
                sizeColumnClient,
                dateColumnClient,
                clientPathField
        );
        serverTable = new ServerPanelController(
                serverTableView,
                iconColumnServer,
                nameColumnServer,
                sizeColumnServer,
                dateColumnServer,
                serverPathField
        );
        clientTable.setRootPath(".");
        clientTable.updateList();
        operationTableController = new OperationTableController(
                operationTable,
                operationColumn,
                infoColumn,
                fileNameColumn,
                progressColumn,
                goToFileColumn,
                deleteItemColumn,
                this,
                clientTable,
                serverTable
        );
        prepareLogicalDisksBox();
        prepareServerTableContent();
    }

    private void setContextMenusToTables() {
        ContextMenu clientContextMenu = new ContextMenu();
        ContextMenu serverContextMenu = new ContextMenu();
        MenuItem downloadItem = new MenuItem("Скачать файл");
        MenuItem uploadItem = new MenuItem("Отправить в облако");
        MenuItem renameClientItem = new MenuItem("Переименовать файл");
        MenuItem renameServerItem = new MenuItem("Переименовать файл на сервере");
        MenuItem deleteClientItem = new MenuItem("Удалить файл");
        MenuItem deleteServerItem = new MenuItem("Удалить файл с сервера");
        MenuItem refreshClientItem = new MenuItem("Обновить");
        MenuItem refreshServerItem = new MenuItem("Обновить на сервере");

        downloadItem.setOnAction(event -> {
            if (!downloadButton.isDisabled()) {
                downloadButtonAction();
            }
        });
        uploadItem.setOnAction(event -> {
            if (!uploadButton.isDisabled()) {
                uploadButtonAction();
            }
        });
        renameClientItem.setOnAction(event -> {
            if (!renameButton.isDisabled()) {
                renameButtonAction();
            }
        });
        renameServerItem.setOnAction(event -> {
            if (!renameButton.isDisabled()) {
                renameButtonAction();
            }
        });
        deleteClientItem.setOnAction(event -> {
            if (!deleteButton.isDisabled()) {
                deleteButtonAction();
            }
        });
        deleteServerItem.setOnAction(event -> {
            if (!deleteButton.isDisabled()) {
                deleteButtonAction();
            }
        });
        refreshClientItem.setOnAction(event -> refreshClientFilesList());
        refreshServerItem.setOnAction(event -> refreshServerFilesList());

        clientContextMenu.getItems().addAll(refreshClientItem, new SeparatorMenuItem(), uploadItem, renameClientItem, deleteClientItem);
        serverContextMenu.getItems().addAll(refreshServerItem, new SeparatorMenuItem(), downloadItem, renameServerItem, deleteServerItem);

        clientTableView.setContextMenu(clientContextMenu);
        serverTableView.setContextMenu(serverContextMenu);
    }

    private void setTooltips() {
        Tooltip refreshButtonTooltip = new Tooltip("Обновить текущий каталог");
        refreshButtonClient.setTooltip(refreshButtonTooltip);
        refreshButtonServer.setTooltip(refreshButtonTooltip);

        Tooltip pathUpButtonTooltip = new Tooltip("Перейти в предыдущий каталог");
        pathUpButton.setTooltip(pathUpButtonTooltip);
        pathUpButtonServer.setTooltip(pathUpButtonTooltip);

        logicalDisksBox.setTooltip(new Tooltip("Выбрать диск"));
    }

    private void prepareServerTableContent() {
        NetworkClient.getInstance().sendCommandToServer(new GetFilesListCommand(null));
        FilesListCommand command = (FilesListCommand) NetworkClient.getInstance().readCommandFromServer();
        serverTable.setRootPath(command.getRootServerPath());
        serverTable.updateList(command.getFilesList());
    }

    public void buttonPathUpAction(ActionEvent actionEvent) {
        clientTable.buttonPathUpAction(actionEvent);
    }

    public void buttonPathUpServerAction(ActionEvent actionEvent) {
        serverTable.buttonPathUpAction(actionEvent);
    }

    private void prepareLogicalDisksBox() {
        logicalDisksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            logicalDisksBox.getItems().add(p.toString());
        }
        logicalDisksBox.getSelectionModel().select(0);
    }

    @FXML
    private void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        clientTable.updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    private void disableButtons() {
        renameButton.setDisable(true);
        deleteButton.setDisable(true);
        downloadButton.setDisable(true);
        uploadButton.setDisable(true);
    }

    private void enableButtons() {
        renameButton.setDisable(false);
        deleteButton.setDisable(false);
        downloadButton.setDisable(false);
        uploadButton.setDisable(false);
    }

    public void downloadButtonAction() {
        FileInfo fileInfo = (FileInfo) serverTable.getSelectedItem();
        if (fileInfo == null || fileInfo.getType() == FileInfo.FileType.DIRECTORY) {
            String message = "Выберите файл, для скачивания с сервера";
            showInfoAlert(message, Alert.AlertType.WARNING, true);
            return;
        }
        Path checkedPath = Paths.get(clientTable.getCurrentPath(), fileInfo.getFileName());
        if (Files.exists(checkedPath)) {
            if (showConfirmAlert("Файл " + fileInfo.getFileName() + " уже существует, желаете перезаписать?")) {
                try {
                    Files.delete(checkedPath);
                } catch (IOException e) {
                    showInfoAlert("Невозможно перезаписать файл, " + fileInfo.getFileName() + "нет доступа", Alert.AlertType.WARNING, true);
                    return;
                }
            } else {
                return;
            }
        }
        String currentServerDir = serverTable.getCurrentPath();
        Path sourcePath = Paths.get(currentServerDir, fileInfo.getFileName());
        Path destPath = Paths.get(clientTable.getCurrentPath());

        TransferItem item = new TransferItem(TransferItem.Operation.DOWNLOAD, sourcePath, destPath);
        operationTableController.updateOperationTable(item);

        NetworkClient.getInstance().getFileFromServer(item, this::refreshClientFilesList);
        disableButtons();
    }

    public void uploadButtonAction() {
        String errorMessage = "Выберите файл для отправки на сервер";
        String fileName = clientTable.getSelectedFileName();
        if (fileName == null) {
            showInfoAlert(errorMessage, Alert.AlertType.WARNING, true);
            return;
        }
        String currentDir = clientTable.getCurrentPath();
        Path sourcePath = Paths.get(currentDir, fileName);
        if (Files.isDirectory(sourcePath)) {
            showInfoAlert(errorMessage, Alert.AlertType.WARNING, true);
            return;
        }
        Path destPath = Paths.get(serverTable.getCurrentPath());

        TransferItem item = new TransferItem(TransferItem.Operation.UPLOAD, sourcePath, destPath);
        operationTableController.updateOperationTable(item);

        NetworkClient.getInstance().sendFileToServer(item, this::refreshServerFilesList);
        disableButtons();
    }

    public void refreshServerFilesList(Path destDir) {
        Platform.runLater(() -> {
            enableButtons();
            if (destDir.toString().equals(serverTable.getCurrentPath())) {
                serverTable.updateList(destDir);
            }
        });
    }

    public void refreshClientFilesList(Path destDir) {
        Platform.runLater(() -> {
            enableButtons();
            if (destDir.toString().equals(clientTable.getCurrentPath())) {
                clientTable.updateList(destDir);
            }
        });
    }

    public Path getSelectedPathFromClientPanel() {
        if (clientTable.getSelectedFileName() == null) {
            return null;
        }
        Path path = null;
        if (clientTable.getSelectedFileName() != null) {
            path = Paths.get(clientTable.getCurrentPath(), clientTable.getSelectedFileName());
            if (Files.isDirectory(path)) {
                return null;
            }
        }
        return path;
    }

    public Path getSelectedPathFromServerPanel() {
        if (serverTable.getSelectedFileName() == null) {
            return null;
        }
        Path path = null;
        if (serverTable.getSelectedItem() != null) {
            path = Paths.get(serverTable.getCurrentPath(), ((FileInfo) serverTable.getSelectedItem()).getFileName());
            if (Files.isDirectory(path)) {
                return null;
            }
        }
        return path;
    }

    public void deleteButtonAction() {
        Path clientPath = getSelectedPathFromClientPanel();
        Path serverPath = getSelectedPathFromServerPanel();
        if (clientPath == null && serverPath == null) {
            showInfoAlert("Выберите файл для удаления", Alert.AlertType.WARNING, false);
            return;
        }
        if (clientPath != null) {
            if (showConfirmAlert("Вы уверены, что хотите удалить файл " + clientPath.getFileName())) {
                try {
                    Files.delete(clientPath);
                    clientTable.updateList(clientPath.getParent());
                } catch (IOException e) {
                    String message = "Ошибка удаления! Файл " + clientPath.getFileName() + " занят другим процессом";
                    System.out.println(message);
                    showInfoAlert(message, Alert.AlertType.WARNING, true);
                }
            }
        } else {
            if (showConfirmAlert("Вы уверены, что хотите удалить файл " + serverPath.getFileName() + " с сервера")) {
                NetworkClient.getInstance().deleteFileFromServer(serverPath);
                if (NetworkClient.getInstance().readCommandFromServer() instanceof ErrorCommand) {
                    String message = "Ошибка удаления! Файл " + serverPath.getFileName() + " занят другим процессом";
                    System.out.println(message);
                    showInfoAlert(message, Alert.AlertType.WARNING, true);
                } else {
                    serverTable.updateList(serverPath.getParent());
                }
            }
        }
    }

    public void renameButtonAction() {
        Path clientPath = getSelectedPathFromClientPanel();
        Path serverPath = getSelectedPathFromServerPanel();
        if (clientPath == null && serverPath == null) {
            showInfoAlert("Выберите файл для переименования", Alert.AlertType.WARNING, false);
            return;
        }
        if (clientPath != null) {
            String newName = showTextInputDialog(clientPath.getFileName().toString(),
                    "Переименование файла " + clientPath.getFileName(),
                    "Введите новое имя файла: "
            );
            if (newName != null) {
                try {
                    Files.move(clientPath, Paths.get(clientPath.getParent().toString(), newName));
                    clientTable.updateList(clientPath.getParent());
                } catch (IOException e) {
                    String message = "Ошибка переименования! Файл " + clientPath.getFileName() + " занят другим процессом";
                    System.out.println(message);
                    showInfoAlert(message, Alert.AlertType.WARNING, true);
                }
            }
        } else {
            String newName = showTextInputDialog(serverPath.getFileName().toString(),
                    "Переименование файла " + serverPath.getFileName() + " на сервере",
                    "Введите новое имя файла: "
            );
            if (newName != null) {
                Path newPath = Paths.get(serverPath.getParent().toString(), newName);
                NetworkClient.getInstance().renameFileOnServer(serverPath.toString(), newPath.toString());
                if (NetworkClient.getInstance().readCommandFromServer() instanceof ErrorCommand) {
                    String message = "Ошибка переименования! Файл " + serverPath.getFileName() + " занят другим процессом";
                    System.out.println(message);
                    showInfoAlert(message, Alert.AlertType.WARNING, true);
                } else {
                    serverTable.updateList(serverPath.getParent());
                }
            }
        }
    }

    public boolean showConfirmAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "");
        alert.setTitle("Подтверждение операции");
        alert.setHeaderText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public String showTextInputDialog(String filename, String msgWithFileName, String message) {
        TextInputDialog dialog = new TextInputDialog(filename);
        dialog.setTitle("Окно ввода данных");
        dialog.setHeaderText(msgWithFileName);
        dialog.setContentText(message);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void showInfoAlert(String message, Alert.AlertType type, boolean needWait) {
        Alert alert = new Alert(type, "", ButtonType.OK);
        alert.setTitle("Информационное окно");
        alert.setHeaderText(message);
        if (needWait) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    public void refreshServerFilesList() {
        serverTable.updateList(Paths.get(serverTable.getCurrentPath()));
    }

    public void refreshClientFilesList() {
        clientTable.updateList(Paths.get(clientTable.getCurrentPath()));
    }
}
