package app;

import domain.*;
import repository.*;
import service.UserService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileReader;
import java.util.Properties;

public class GuiMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Properties props = new Properties();
        try {
            props.load(new FileReader("config.properties"));
        } catch (Exception e) {
            System.out.println("Nu am gasit config.properties, verificati calea!");
            return;
        }

        String dbUrl = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPassword = props.getProperty("db.password");

        Repository<Long, Persoana> persoanaRepo = new PersoanaDbRepository(dbUrl, dbUser, dbPassword);
        Repository<Long, Duck> duckRepo = new DuckDbRepository(dbUrl, dbUser, dbPassword);
        Repository<Long, Card> cardRepo = new CardDbRepository(dbUrl, dbUser, dbPassword, duckRepo);
        Repository<Long, Event> eventRepo = new EventDbRepository(dbUrl, dbUser, dbPassword, persoanaRepo, duckRepo);
        UserFriendshipDbRepository friendshipRepo = new UserFriendshipDbRepository(dbUrl, dbUser, dbPassword, persoanaRepo, duckRepo);
        Repository<Long, Message> messageRepo = new MessageDbRepository(dbUrl, dbUser, dbPassword, persoanaRepo, duckRepo);
        Repository<Long, FriendRequest> requestRepo = new FriendRequestDbRepository(dbUrl, dbUser, dbPassword, persoanaRepo, duckRepo);

        UserService userService = new UserService(persoanaRepo, duckRepo, cardRepo, eventRepo, friendshipRepo, messageRepo, requestRepo);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
        VBox root = loader.load();

        LoginController controller = loader.getController();
        controller.setService(userService);

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("Duck Manager GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}