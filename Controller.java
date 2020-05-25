package app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Controller {

    public String username = "username";
    private ChatService chatService;

    @FXML
    private Button sendButton;

    @FXML
    private Button cleanChatButton;

    @FXML
    private Label usernameLabel;

    @FXML
    private TextArea messagesTextArea;

    @FXML
    private TextField messageTextField;

    public void addMessage(String message){
        this.messagesTextArea.appendText(message + "\n");
    }

    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public ChatService getChatService() {
        return chatService;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void sendMessage() throws IOException {
        String time = getCurrentDate();
        try{
            int len = this.messagesTextArea.getText().length();
            chatService.send(this.username, this.username, this.messageTextField.getText());
            try{
                Thread.sleep(120);
                if(len == this.messagesTextArea.getText().length()) this.addMessage("[channel_uxio]" + "[" + this.username + "]" + this.messageTextField.getText());
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
        messageTextField.setText("");
    }

    public void getUsername(){
        TextInputDialog dialog = new TextInputDialog("");

        dialog.setTitle("Nombre de Usuario");
        dialog.setHeaderText("Introduzca su nombre de usuario:");
        dialog.setContentText("Nombre de usuario:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            try {
                chatService.nick(username);
                chatService.join(username, this.username);
            } catch (IOException ex){
                System.out.println(ex.getMessage());
            }
            this.username = name;
            this.usernameLabel.setText(name);
        });

    }

    public void getChannel(){
        TextInputDialog dialog = new TextInputDialog("");

        dialog.setTitle("Canal");
        dialog.setHeaderText("Introduzca el nombre del canal al que se conectará:");
        dialog.setContentText("Canal:");

        Optional<String> result = dialog.showAndWait();

        System.out.println(result.isPresent());
    }

    public void getMessage() throws SocketException, IOException{
        try{
            List<String> message = this.chatService.takeMessages();
            message.stream().forEach(x -> addMessage(x));
        } catch (Exception e){
            if(e.getMessage().contains("Socket closed")) System.exit(0);
            System.out.println("Socket: " + e.getMessage());
        }
    }

    public void sendButtonClicked(){
        try {
            sendMessage();
        } catch(Exception e){
            System.out.println("IO: " + e.getMessage());
        }
    }

    public void cleanChat(){
        this.messagesTextArea.setText("");
    }
}
