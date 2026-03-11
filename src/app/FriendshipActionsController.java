package app;

import domain.User;
import service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FriendshipActionsController {
    private UserService service;

    @FXML private TextField txtId1, txtId2;
    @FXML private ComboBox<String> comboType1, comboType2;

    public void setService(UserService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        comboType1.getItems().addAll("Persoana", "Rata");
        comboType2.getItems().addAll("Persoana", "Rata");

        // Default-uri
        comboType1.getSelectionModel().select(0);
        comboType2.getSelectionModel().select(0);
    }

    private User getUserFromInput(TextField txtId, ComboBox<String> comboType) {
        try {
            Long id = Long.parseLong(txtId.getText());
            int typeCode = comboType.getValue().equals("Persoana") ? 1 : 2;

            // Folosim metoda din Service: 1=Persoana, 2=Rata
            return service.findUserByIdAndType(id, typeCode);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    public void handleAddFriendship() {
        processFriendship(true);
    }

    @FXML
    public void handleRemoveFriendship() {
        processFriendship(false);
    }

    private void processFriendship(boolean isAdd) {
        User u1 = getUserFromInput(txtId1, comboType1);
        User u2 = getUserFromInput(txtId2, comboType2);

        if (u1 == null || u2 == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Unul dintre utilizatori nu a fost gasit sau ID-ul este invalid!");
            alert.show();
            return;
        }

        if (isAdd) {
            service.addFriendship(u1, u2);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Prietenie adaugata intre " + u1.getUsername() + " si " + u2.getUsername());
            alert.show();
        } else {
            service.removeFriendship(u1, u2);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Prietenie stearsa intre " + u1.getUsername() + " si " + u2.getUsername());
            alert.show();
        }
    }
}