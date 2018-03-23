package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main extends Application {

    ServerSocket serverSocket = null;

    boolean connected = true;

    public static int port = 8080;

    private String sharedFolder = "sharedFolder";

    private List<ClientThread> threads = null;

    @Override
    public void start(Stage primaryStage) throws Exception{



        threads =new ArrayList<>();

        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Started server.");

        while(connected) {
            Socket clientSocket = serverSocket.accept();
            //Below won't execute until a client connects...

            //System.out.println("Client joined...");

            threads.add(new ClientThread(clientSocket));
            threads.get(threads.size()-1).start();

            //Check to remove threads from list once they are completed
            for (int i = threads.size() - 1; i >= 0 ; i--)
            {
                if (threads.get(i).finished)
                {
                    //System.out.println("Removed a ClientThread");
                    threads.remove(i);
                }
            }

        }

        serverSocket.close();
    }



    public static void main(String[] args) {


        launch(args);
    }
}
