import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import services.NetworkClient;

public class ClientApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Image icon = new Image("image/logo.png");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/logInScreen.fxml"));
        Parent root = fxmlLoader.load();
        NetworkClient.getInstance().start();
        Scene scene = new Scene(root);
        scene.getStylesheets().add((getClass().getResource("/css/style.css")).toExternalForm());
        primaryStage.setTitle("Авторизация");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(icon);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            NetworkClient.getInstance().stop();
            Platform.exit();
        });
        primaryStage.show();
    }
}
