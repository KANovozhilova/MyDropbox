package controllers;

import commands.AuthCommand;
import commands.SignUpCommand;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import services.NetworkClient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthWindowsController implements Initializable {
    
    public AnchorPane signInPane;
    public AnchorPane signUpPane;
    public Button signUpButton;

    @FXML
    private TextField loginText;

    @FXML
    private PasswordField passwordText;

    @FXML
    private PasswordField passwordTextRepeat;

    @FXML
    private Button signUpScreenButton;

    @FXML
    private Button logInButton;

    @FXML
    private Label signInLabel;

    @FXML
    private Button signInScreenButton;

    private PauseTransition pause;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (pause == null) {
            pause = new PauseTransition();
        }
    }

    public void startAuthentication() {
        if (checkTextFields()) {
            setLabelError("Некорректный ввод данных. Попробуйте ещё раз!");
            return;
        }
        String login = loginText.getText().trim();
        String password = passwordText.getText().trim();
        NetworkClient.getInstance().sendCommandToServer(new AuthCommand(login, password));
        AuthCommand command = (AuthCommand) NetworkClient.getInstance().readCommandFromServer();
        if (command.isAuthorized()) {
            logInButton.setDisable(true);
            setLabelOk("Вход выполнен!");
            NetworkClient.getInstance().setUserId(command.getUserID());
            runWithPause(200, event -> openMainWindow());
        } else {
            setLabelError(command.getMessage());
        }
    }

    public void startRegistration() {
        if (checkTextFields()) {
            setLabelError("Некорректный ввод данных.");
            return;
        }
        if (!passwordText.getText().equals(passwordTextRepeat.getText())) {
            setLabelError("Пароли не совпадают!");
            return;
        }
        String login = loginText.getText().trim();
        String password = passwordText.getText().trim();
        NetworkClient.getInstance().sendCommandToServer(new SignUpCommand(login, password));
        SignUpCommand command = (SignUpCommand) NetworkClient.getInstance().readCommandFromServer();
        if (command.isSignUp()) {
            setLabelOk("Регистрация выполнена успешно. Переход на окно авторизации.");
            runWithPause(1500, event -> openSignInScreen());
        } else {
            setLabelError(command.getMessage());
        }
    }

    public void openSignUpScreen() {
        try {
            showNewStage((Stage) signUpScreenButton.getScene().getWindow(),
                    "/fxml/signUpScreen.fxml",
                    "Регистрация",
                    event -> {
                        NetworkClient.getInstance().stop();
                        Platform.exit();
                    });
        } catch (IOException e) {
            System.err.println("Ошибка загрузки экрана регистрации!");
            e.printStackTrace();
        }
    }

    public void openSignInScreen() {
        try {
            showNewStage((Stage) signInScreenButton.getScene().getWindow(),
                    "/fxml/logInScreen.fxml",
                    "Авторизация",
                    event -> {
                        NetworkClient.getInstance().stop();
                        Platform.exit();
                    }
            );
        } catch (IOException e) {
            System.err.println("Ошибка загрузки экрана авторизации!");
            e.printStackTrace();
        }
    }

    private void showNewStage(Stage stage, String FXMLFile, String title, EventHandler<WindowEvent> onCloseEvent) throws IOException {
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource(FXMLFile)));
        newScene.getStylesheets().add((getClass().getResource("/css/style.css")).toExternalForm());
        stage.setTitle(title);
        stage.setScene(newScene);
        stage.setOnCloseRequest(onCloseEvent);
    }

    private boolean checkTextFields() {
        if (loginText.getText().trim().isEmpty() || passwordText.getText().trim().isEmpty()) {
            loginText.setText("");
            passwordText.setText("");
            return true;
        }
        return false;
    }

    public void setLabelError(String message) {
        Platform.runLater(() -> {
            signInLabel.setText(message);
            signInLabel.setTextFill(Color.TOMATO);
        });
    }

    public void setLabelOk(String message) {
        Platform.runLater(() -> {
            signInLabel.setText(message);
            signInLabel.setTextFill(Color.GREEN);
        });
    }

    public void runWithPause(int duration, EventHandler<ActionEvent> event) {
        pause.setDuration(Duration.millis(duration));
        pause.setOnFinished(event);
        pause.play();
    }

    private void openMainWindow() {
        try {
            Stage mainWindow = new Stage();
            Scene mainScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainWindow.fxml")));
            mainScene.getStylesheets().add((getClass().getResource("/css/style.css")).toExternalForm());
            mainWindow.setTitle("MyDropbox");
            mainWindow.setScene(mainScene);
            mainWindow.setOnCloseRequest(event -> {
                NetworkClient.getInstance().stop();
                Platform.exit();
            });
            Image icon = new Image("image/logo.png");
            mainWindow.getIcons().add(icon);
//            mainWindow.setResizable(false);
            logInButton.getScene().getWindow().hide();
            mainWindow.show();
        } catch (IOException e) {
            System.err.println("Ошибка загрузки главного экрана приложения!");
            e.printStackTrace();
        }
    }
}
