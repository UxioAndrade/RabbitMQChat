package app;

import com.rabbitmq.client.DeliverCallback;
import javafx.application.Application;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

public class Client extends Application  {

    private Controller controllerHandle;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("RabbitMQ");
        this.controllerHandle = loader.getController();
        primaryStage.setScene(new Scene(root, 800, 640));
        primaryStage.show();
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    this.controllerHandle.sendMessage();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });

        ChatService chatService = null;
        try {
            chatService = new ChatService("localhost");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            return;
        }
        this.controllerHandle.setChatService(chatService);
        this.controllerHandle.getUsername();
        this.controllerHandle.getChannel();
        Thread thread = new Thread( () -> {
            try {
                while (true) {
                    try {
                        Thread.sleep(10);
                        this.controllerHandle.getMessage();
                    } catch (InterruptedException ex){
                        System.out.println(ex.getMessage());
                    }
                }
            } catch (SocketException e) {
                System.out.println("Socket: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            }
        });
        thread.start();
    }

    @Override
    public void stop(){
        try {
            this.controllerHandle.getChatService().channel.close();
            this.controllerHandle.getChatService().channel.getConnection().close();
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        System.out.println("Hasta pronto");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
