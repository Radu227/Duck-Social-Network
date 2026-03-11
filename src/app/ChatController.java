package app;

import domain.*;
import javafx.application.Platform;
import service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ChatController {
    private UserService service;
    private User currentUser;  //expeditorul
    private User chatPartner;  //destinatarul

    @FXML private ListView<String> listMessages;
    @FXML private TextField txtMessage;
    @FXML private Label lblPartner;

    // Pastram mesajele originale in memorie ca sa stim la care dam Reply
    private List<Message> conversationMessages;

    private Consumer<Message> myListener;

    public void setService(UserService service, User currentUser, User chatPartner) {
        this.service = service;
        this.currentUser = currentUser;
        this.chatPartner = chatPartner;

        lblPartner.setText("Conversație: " + currentUser.getUsername() + " <-> " + chatPartner.getUsername());
        refreshConversation();

        myListener = (Message msg) -> {
            boolean isRelevant = false;

            // Mesaj primit de la partener
            if (msg.getFrom().getId().equals(chatPartner.getId()) &&
                    msg.getTo().stream().anyMatch(u -> u.getId().equals(currentUser.getId()))) {
                isRelevant = true;
            }

            // Mesaj trimis de mine (de pe alta fereastra)
            if (msg.getFrom().getId().equals(currentUser.getId()) &&
                    msg.getTo().stream().anyMatch(u -> u.getId().equals(chatPartner.getId()))) {
                isRelevant = true;
            }

            if (isRelevant) {
                Platform.runLater(() -> {
                    refreshConversation();
                });
            }
        };

        service.addChatListener(myListener);

        refreshConversation();
    }

    private void refreshConversation() {
        conversationMessages = service.getConversation(currentUser, chatPartner);

        listMessages.getItems().clear();

        //Afisarea conversatiei
        for (Message m : conversationMessages) {
            String prefix = m.getFrom().equals(currentUser) ? "EU: " : m.getFrom().getUsername() + ": ";
            String content = m.getMessage();

            //Daca e Reply, adaugam si textul original
            if (m instanceof ReplyMessage) {
                Message original = ((ReplyMessage) m).getOriginalMessage();
                String originalText = (original != null) ? original.getMessage() : "mesaj sters";
                content += " (Reply la: '" + originalText + "')";
            }

            String time = "[" + m.getDate().getHour() + ":" + m.getDate().getMinute() + "]";
            listMessages.getItems().add(time + " " + prefix + content);
        }
    }

    @FXML
    public void handleSendMessage() {
        String text = txtMessage.getText();
        if (text.isEmpty()) return;

        List<Long> toIds = Collections.singletonList(chatPartner.getId());
        List<String> toTypes = Collections.singletonList(getUserType(chatPartner));

        try {
            service.sendMessage(currentUser.getId(), getUserType(currentUser), toIds, toTypes, text, null);
            txtMessage.clear();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Eroare: " + e.getMessage());
            a.show();
        }
    }

    @FXML
    public void handleReply() {
        //Verificam ce mesaj e selectat in lista
        int index = listMessages.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            new Alert(Alert.AlertType.WARNING, "Selecteaza un mesaj din lista pentru a da Reply!").show();
            return;
        }

        String text = txtMessage.getText();
        if (text.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Scrie textul raspunsului in casuta de jos!").show();
            return;
        }

        Message original = conversationMessages.get(index);

        List<Long> toIds = Collections.singletonList(chatPartner.getId());
        List<String> toTypes = Collections.singletonList(getUserType(chatPartner));

        try {
            service.sendMessage(currentUser.getId(), getUserType(currentUser), toIds, toTypes, text, original);
            txtMessage.clear();
            //refreshConversation();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Eroare: " + e.getMessage());
            a.show();
        }
    }

    private String getUserType(User u) {
        return (u instanceof Persoana) ? "PERSOANA" : "RATA";
    }

    public void cleanup() {
        service.removeChatListener(myListener);
    }
}