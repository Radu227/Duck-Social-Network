package app;

import domain.Duck;
import domain.FriendRequest;
import domain.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import repository.Page;
import service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.util.Pair;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ButtonBar.ButtonData;
import domain.Persoana;

import domain.Event;
import domain.RaceEvent;

import app.UserProfileController;

public class DuckController {

    private UserService service;
    private ObservableList<Duck> model = FXCollections.observableArrayList();

    private int currentPage = 0;
    private int pageSize = 5;
    private int totalNumberOfElements = 0;
    private User loggedUser;

    @FXML
    private TableView<Duck> duckTable;
    @FXML
    private TableColumn<Duck, Long> colId;
    @FXML
    private TableColumn<Duck, String> colUsername;
    @FXML
    private TableColumn<Duck, String> colEmail;
    @FXML
    private TableColumn<Duck, String> colTip;
    @FXML
    private TableColumn<Duck, Double> colViteza;
    @FXML
    private TableColumn<Duck, Double> colRezistenta;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button btnPrevious;
    @FXML
    private Button btnNext;
    @FXML
    private Label labelPage;
    @FXML
    private ComboBox<Integer> comboPageSize;

    @FXML private TableView<Persoana> personTable;
    @FXML private TableColumn<Persoana, Long> colPersId;
    @FXML private TableColumn<Persoana, String> colNume;
    @FXML private TableColumn<Persoana, String> colPrenume;
    @FXML private TableColumn<Persoana, String> colOcupatie;
    @FXML private TableColumn<Persoana, Double> colEmpatie;

    @FXML private TextField txtRequestUsername;
    @FXML private TableView<FriendRequest> requestTable;
    @FXML private TableColumn<FriendRequest, String> colReqFrom;
    @FXML private TableColumn<FriendRequest, String> colReqDate;
    @FXML private TableColumn<FriendRequest, String> colReqStatus;

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, Long> colEventId;
    @FXML private TableColumn<Event, String> colEventName;
    @FXML private TableColumn<Event, String> colEventSubscribers;

    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBarCursa;
    @FXML private Button btnStartCursa;

    private ObservableList<Event> modelEvenimente = FXCollections.observableArrayList();

    private Consumer<FriendRequest> requestListener;

    private ObservableList<Persoana> modelPersoane = FXCollections.observableArrayList();

    public void setService(UserService service) {
        this.service = service;
        initModel();
    }

    @FXML
    public void initialize() {
        // Configurarea coloanelor tabelului
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        colViteza.setCellValueFactory(new PropertyValueFactory<>("viteza"));
        colRezistenta.setCellValueFactory(new PropertyValueFactory<>("rezistenta"));

        duckTable.setItems(model);

        // Configurare Filtru Tip Rata
        filterComboBox.getItems().addAll("TOATE", "Flying", "Swimming", "Flying_and_Swimming");
        filterComboBox.getSelectionModel().select("TOATE");

        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 0;
            initModel();
        });

        comboPageSize.getItems().addAll(3, 5, 10, 20);
        comboPageSize.setValue(pageSize);

        comboPageSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = newVal;
                currentPage = 0;
                initModel();
            }
        });
        //Configurarea tabelului de persoane
        colPersId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        colPrenume.setCellValueFactory(new PropertyValueFactory<>("prenume"));
        colOcupatie.setCellValueFactory(new PropertyValueFactory<>("ocupatie"));
        colEmpatie.setCellValueFactory(new PropertyValueFactory<>("nivelEmpatie")); // Atentie: getter-ul e getNivelEmpatie

        personTable.setItems(modelPersoane);

        colReqFrom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFrom().getUsername()));
        colReqDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Configurare Tabel Evenimente
        colEventId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEventName.setCellValueFactory(new PropertyValueFactory<>("nume"));
        // Pentru subscribers afisam doar marimea listei
        colEventSubscribers.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getSubscribers().size())));

        eventTable.setItems(modelEvenimente);
    }

    private void initModel() {
        if (service == null) return;

        String selectedFilter = filterComboBox.getValue();

        Page<Duck> page = service.getDucksOnPage(currentPage, pageSize, selectedFilter);

        model.setAll(page.getElementsOnPage());

        totalNumberOfElements = page.getTotalElementCount();
        updatePaginationControls();

        loadPersoane();

        loadEvents();
    }

    private void loadPersoane() {
        if (service == null) return;

        Iterable<Persoana> persoane = service.getAllPersoane();
        List<Persoana> list = new ArrayList<>();
        persoane.forEach(list::add);

        modelPersoane.setAll(list);
    }

    private void loadEvents() {
        if (service == null) return;
        List<Event> events = service.getEvenimente();
        modelEvenimente.setAll(events);
    }

    @FXML
    public void handleRefreshPersoane() {
        loadPersoane();
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        refreshRequests();

        if (requestListener == null) {
            requestListener = (req) -> Platform.runLater(() -> refreshRequests());
            service.addRequestListener(requestListener);
        }
    }

    private void refreshRequests() {
        if (loggedUser == null) return;
        requestTable.getItems().setAll(service.getRequests(loggedUser));
    }

    @FXML
    public void handleSendRequest() {
        try {
            service.sendFriendRequest(loggedUser.getUsername(), txtRequestUsername.getText());
            new Alert(Alert.AlertType.INFORMATION, "Cerere trimisa!").show();
            txtRequestUsername.clear();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    public void handleAcceptRequest() {
        FriendRequest req = requestTable.getSelectionModel().getSelectedItem();
        if (req == null) {
            new Alert(Alert.AlertType.WARNING, "Selecteaza o cerere!").show();
            return;
        }

        if (!"PENDING".equals(req.getStatus())) {
            new Alert(Alert.AlertType.WARNING, "Aceasta cerere a fost deja procesata!").show();
            return;
        }
        try {
            service.acceptFriendRequest(req);
            new Alert(Alert.AlertType.INFORMATION, "Ai acceptat prietenia!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    public void handleRejectRequest() {
        FriendRequest req = requestTable.getSelectionModel().getSelectedItem();
        if (req == null) {
            new Alert(Alert.AlertType.WARNING, "Selecteaza o cerere!").show();
            return;
        }

        if (!"PENDING".equals(req.getStatus())) {
            new Alert(Alert.AlertType.WARNING, "Aceasta cerere a fost deja procesata!").show();
            return;
        }
        try {
            service.rejectFriendRequest(req);
            new Alert(Alert.AlertType.INFORMATION, "Ai refuzat cererea!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }


    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) totalNumberOfElements / pageSize);
        if (totalPages == 0) totalPages = 1;

        labelPage.setText("Page " + (currentPage + 1) + " of " + totalPages);

        btnPrevious.setDisable(currentPage == 0);

        btnNext.setDisable((currentPage + 1) * pageSize >= totalNumberOfElements);
    }

    @FXML
    public void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            initModel();
        }
    }

    @FXML
    public void handleNextPage() {
        if ((currentPage + 1) * pageSize < totalNumberOfElements) {
            currentPage++;
            initModel();
        }
    }

    //Deschide fereastra de add/remove user
    @FXML
    public void handleOpenUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserActions.fxml"));
            Parent root = loader.load();

            UserActionsController controller = loader.getController();
            controller.setService(service);

            Stage stage = new Stage();
            stage.setTitle("Gestionare Utilizatori");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenFriendships() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FriendshipActions.fxml"));
            Parent root = loader.load();

            FriendshipActionsController controller = loader.getController();
            controller.setService(service);

            Stage stage = new Stage();
            stage.setTitle("Gestionare Prietenii");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowCommunities() {
        int nr = service.getNumarComunitati();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistici Comunitati");
        alert.setHeaderText(null);
        alert.setContentText("Numarul total de comunitati în retea este: " + nr);
        alert.showAndWait();
    }

    @FXML
    public void handleShowMostSociable() {
        List<domain.User> users = service.getCeaMaiSociabilaComunitate();
        StringBuilder sb = new StringBuilder();
        if (users.isEmpty()) {
            sb.append("Nu exista comunitati.");
        } else {
            sb.append("Cea mai sociabila comunitate are ").append(users.size()).append(" membri:\n");
            for (domain.User u : users) {
                sb.append("- ").append(u.getUsername()).append(" (ID: ").append(u.getId()).append(")\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cea mai sociabilă comunitate");
        alert.setHeaderText(null);
        alert.setContentText(sb.toString());
        alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    @FXML
    public void handleOpenChat() {
        //Verificam daca avem un user logat
        if (loggedUser == null) {
            new Alert(Alert.AlertType.ERROR, "Eroare: Nu ești logat!").show();
            return;
        }
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Start Chat");
        dialog.setHeaderText("Logat ca: " + loggedUser.getUsername() + " (ID: " + loggedUser.getId() + ")\n" +
                "Cu cine vrei să vorbești?");

        ButtonType openButtonType = new ButtonType("Deschide Chat", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(openButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField idPartner = new TextField();
        idPartner.setPromptText("ID Partener");

        ComboBox<String> typePartner = new ComboBox<>();
        typePartner.getItems().addAll("Persoana", "Rata");
        typePartner.getSelectionModel().select(0); // Selectam implicit Persoana

        grid.add(new Label("ID Destinatar:"), 0, 0);
        grid.add(idPartner, 1, 0);
        grid.add(new Label("Tip Destinatar:"), 0, 1);
        grid.add(typePartner, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == openButtonType) {
                return new Pair<>(idPartner.getText(), typePartner.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                String partnerIdString = result.getKey();
                String partnerTypeString = result.getValue();

                if (partnerIdString.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Introdu ID-ul partenerului!").show();
                    return;
                }

                User sender = loggedUser;

                Long partnerId = Long.parseLong(partnerIdString);
                int typeCode = partnerTypeString.equals("Persoana") ? 1 : 2;

                User receiver = service.findUserByIdAndType(partnerId, typeCode);

                if (receiver != null) {
                    if (sender.getId().equals(receiver.getId()) &&
                            sender.getClass().equals(receiver.getClass())) {
                        new Alert(Alert.AlertType.WARNING, "Nu poți vorbi cu tine însuți!").show();
                        return;
                    }

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatView.fxml"));
                    Parent root = loader.load();

                    ChatController controller = loader.getController();
                    controller.setService(service, sender, receiver);

                    Stage stage = new Stage();
                    stage.setTitle("Chat: " + sender.getUsername() + " -> " + receiver.getUsername());
                    stage.setScene(new Scene(root, 400, 500));
                    stage.show();

                    stage.setOnCloseRequest(e -> controller.cleanup());

                } else {
                    new Alert(Alert.AlertType.ERROR, "Utilizatorul destinatar nu a fost găsit!").show();
                }

            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "ID-ul trebuie să fie un număr valid!").show();
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Eroare la deschiderea chat-ului: " + e.getMessage()).show();
            }
        });
    }

    @FXML
    public void handleSubscribeEvent() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null || loggedUser == null) {
            new Alert(Alert.AlertType.WARNING, "Selecteaza un eveniment si asigura-te ca esti logat!").show();
            return;
        }
        // Abonare
        if(service.subscribeUserToEvent(loggedUser.getUsername(), selected)) {
            new Alert(Alert.AlertType.INFORMATION, "Te-ai inscris cu succes!").show();
            loadEvents(); // Refresh la tabel
        } else {
            new Alert(Alert.AlertType.ERROR, "Eroare la inscriere.").show();
        }
    }

    @FXML
    public void handleStartCursa() {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.ERROR, "Selectează o cursa!").show();
            return;
        }
        if (!(selected instanceof RaceEvent)) {
            new Alert(Alert.AlertType.ERROR, "Acest eveniment nu este o cursa!").show();
            return;
        }

        statusLabel.setText("Se rulează cursa... (Poti folosi aplicatia intre timp)");
        progressBarCursa.setVisible(true);
        progressBarCursa.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        btnStartCursa.setDisable(true);

        // Apelul
        service.simuleazaCursaAsincron(selected.getId())
                .thenAccept(mesajFinal -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("Finalizat!");
                        progressBarCursa.setVisible(false);
                        btnStartCursa.setDisable(false);

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Rezultat Cursa");
                        alert.setHeaderText("Gata!");
                        alert.setContentText(mesajFinal);
                        alert.show();

                        loadEvents(); // Refresh tabel
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("Eroare: " + ex.getMessage());
                        progressBarCursa.setVisible(false);
                        btnStartCursa.setDisable(false);
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    @FXML
    public void handleOpenProfile() {
        if (loggedUser == null) {
            new Alert(Alert.AlertType.ERROR, "Nu esti logat!").show();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/UserProfile.fxml"));

            Parent root = loader.load();
            UserProfileController controller = loader.getController();

            Stage stage = new Stage();
            controller.setService(service, loggedUser, stage);

            stage.setScene(new Scene(root));
            stage.setTitle("Profil: " + loggedUser.getUsername());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}