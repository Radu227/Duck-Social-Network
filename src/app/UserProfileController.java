package app;

import domain.*;
import service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;

public class UserProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label lblUsername;
    @FXML private Label lblFriendsCount;
    @FXML private Label lblTypeSpecific;
    @FXML private Label lblDetailValue;

    private UserService service;
    private User currentUser;
    private Stage stage;

    public void setService(UserService service, User user, Stage stage) {
        this.service = service;
        this.currentUser = user;
        this.stage = stage;
        initData();
    }

    private void initData() {
        lblUsername.setText(currentUser.getUsername());

        // Calculam prietenii
        List<User> prieteniReali = service.getPrietenii(currentUser);
        int friendsCount = prieteniReali.size();
        lblFriendsCount.setText(String.valueOf(friendsCount));
        lblFriendsCount.setText(String.valueOf(friendsCount));

        if (currentUser instanceof Persoana p) {
            lblTypeSpecific.setText("Ocupație:");
            lblDetailValue.setText(p.getOcupatie());
        } else if (currentUser instanceof Duck d) {
            lblTypeSpecific.setText("Tip Rață:");
            lblDetailValue.setText(d.getTip().toString());
        }

        loadProfileImage();
    }

    private void loadProfileImage() {
        String path = currentUser.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    profileImageView.setImage(new Image(file.toURI().toString()));
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else {
        }
    }

    @FXML
    public void handleChangePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagini", "*.jpg", "*.png", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String newPath = selectedFile.getAbsolutePath();
            currentUser.setImagePath(newPath);
            service.updateUserImage(currentUser); // Salvam in DB
            loadProfileImage(); // Refresh
        }
    }

    @FXML
    public void handleClose() { stage.close(); }
}