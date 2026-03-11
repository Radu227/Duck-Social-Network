package app;

import domain.*;
import service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class UserActionsController {
    private UserService service;

    @FXML private ComboBox<String> comboType;
    @FXML private TextField txtUsername, txtEmail, txtNume, txtPrenume, txtOcupatie, txtEmpatie, txtViteza, txtRezistenta;
    @FXML private PasswordField txtPass;
    @FXML private DatePicker dateNastere;
    @FXML private ComboBox<Duck.TipRata> comboDuckType;

    @FXML private VBox boxPersoana, boxRata; // Containerele cu campuri specifice

    @FXML private TextField txtDeleteId;
    @FXML private ComboBox<String> comboDeleteType;

    public void setService(UserService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        comboType.getItems().addAll("Persoana", "Rata");
        comboDeleteType.getItems().addAll("Persoana", "Rata");
        comboDuckType.getItems().setAll(Duck.TipRata.values());

        dateNastere.setDayCellFactory(param -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        comboType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Persoana".equals(newVal)) {
                boxPersoana.setVisible(true);
                boxPersoana.setManaged(true);
                boxRata.setVisible(false);
                boxRata.setManaged(false);
            } else {
                boxPersoana.setVisible(false);
                boxPersoana.setManaged(false);
                boxRata.setVisible(true);
                boxRata.setManaged(true);
            }
        });

        comboType.getSelectionModel().select("Persoana");
    }

    @FXML
    public void handleSaveUser() {
        try {
            String type = comboType.getValue();
            String user = txtUsername.getText();
            String email = txtEmail.getText();
            String pass = txtPass.getText();

            if (user.isEmpty() || email.isEmpty()) {
                showMessage("Eroare", "Username si email obligatorii!");
                return;
            }

            if ("Persoana".equals(type)) {
                String nume = txtNume.getText();
                String prenume = txtPrenume.getText();
                LocalDate data = dateNastere.getValue();
                String ocupatie = txtOcupatie.getText();
                double empatie = Double.parseDouble(txtEmpatie.getText());

                Persoana p = new Persoana(null, user, email, pass, nume, prenume, data, ocupatie, empatie);
                service.addUser(p);
            } else {
                Duck.TipRata tipRata = comboDuckType.getValue();
                double vit = Double.parseDouble(txtViteza.getText());
                double rez = Double.parseDouble(txtRezistenta.getText());

                Duck d;
                switch(tipRata) {
                    case Flying -> d = new FlyingDuck(null, user, email, pass, vit, rez);
                    case Swimming -> d = new SwimmingDuck(null, user, email, pass, vit, rez);
                    case Flying_and_Swimming -> d = new FlyingAndSwimmingDuck(null, user, email, pass, vit, rez);
                    default -> throw new IllegalStateException("Tip necunoscut");
                }
                service.addUser(d);
            }
            showMessage("Succes", "Utilizator adaugat!");

        } catch (NumberFormatException e) {
            showMessage("Eroare", "Campurile numerice (Viteza, Rezistenta, Empatie) trebuie sa fie numere!");
        } catch (Exception e) {
            showMessage("Eroare", e.getMessage());
        }
    }

    @FXML
    public void handleDeleteUser() {
        try {
            Long id = Long.parseLong(txtDeleteId.getText());
            String type = comboDeleteType.getValue();

            boolean result = false;
            if ("Persoana".equals(type)) {
                result = service.removePersoanaById(id);
            } else if ("Rata".equals(type)) {
                result = service.removeDuckById(id);
            }

            if (result) showMessage("Succes", "Utilizator sters!");
            else showMessage("Eroare", "Utilizatorul nu a fost gasit!");

        } catch (Exception e) {
            showMessage("Eroare", "ID invalid: " + e.getMessage());
        }
    }

    private void showMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}