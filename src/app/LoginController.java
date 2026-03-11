package app;

import domain.User;
import service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    private UserService service;
    private Stage loginStage;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    public void setService(UserService service) {
        this.service = service;
    }

    @FXML
    public void handleLogin() {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Completeaza toate campurile!");
            return;
        }

        User loggedUser = service.login(user, pass);

        if (loggedUser != null) {
            //Login cu succes
            openMainWindow(loggedUser);
        } else {
            lblError.setText("Username sau parola incorecta!");
        }
    }

    private void openMainWindow(User loggedUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DuckView.fxml"));
            Parent root = loader.load();

            DuckController controller = loader.getController();
            controller.setService(service);

            controller.setLoggedUser(loggedUser);

            Stage mainStage = new Stage();
            mainStage.setTitle("Duck Manager GUI");
            mainStage.setScene(new Scene(root, 700, 500));
            mainStage.show();

            //Curatam campurile de login
            txtUsername.clear();
            txtPassword.clear();
            lblError.setText("");

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Eroare la deschiderea aplicatiei!");
        }
    }
}