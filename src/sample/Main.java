package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.Observable;
import java.util.Scanner;

public class Main extends Application {



    ListView<String> clientFiles;
    ListView<String> serverFiles;

    static String computerName;
    static String sharedFolder;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Assignment 2 Client");

        GridPane gridPane = new GridPane();

        sharedFolder = null;

        //Get command line arguments
        Application.Parameters parameters;
        parameters = getParameters();

        for (int i = 0; i < parameters.getRaw().size(); i++)
        {
            //System.out.println(parameters.getRaw().get(i));

            if (i == 0) computerName = parameters.getRaw().get(0);

            if (i == 1) sharedFolder = parameters.getRaw().get(1);

        }


        //Used a hardcoded folder for testing within javafx
        //sharedFolder = "/home/scott/Documents/Assignment2/sharedFolder";


        //Initialize client list view
        clientFiles = new ListView<>();


        //Exit if file directory was not found
        if (sharedFolder == null)
        {
            System.out.println("Error: Have to enter name and a directory respectively.");
            Platform.exit();

            return;

        }


        //Make sure the directory exists.
        File directory = new File(sharedFolder);

        if (!directory.isDirectory())
        {
            System.out.println("Error: Shared folder path was not a directory.");
            Platform.exit();

            return;
        }


        //List files from shared folder
        makeClientFileList(directory);

        //Prepare the server listview
        serverFiles = new ListView<>();
        ObservableList<String> files2 = FXCollections.observableArrayList("(Not connected)");
        serverFiles.setItems(files2);



        //Download Button
        Button downloadBtn = new Button();
        downloadBtn.setText("Download");

        downloadBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                //When clicked, send the selcted filename to the server to be downloaded.
                String file = "";

                file += serverFiles.getSelectionModel().getSelectedItem();


                System.out.println("Downloading: " + file);

                connectToServer(file, true);



            }
        });

        //Upload button
        Button uploadBtn = new Button();
        uploadBtn.setText("Upload");

        uploadBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                //When clicked, send the upload
                String file = clientFiles.getSelectionModel().getSelectedItem();


                String data = "UPLOAD\n" + file + "\n";

               data += readFile(new File(sharedFolder + "/" + file));


                System.out.println("Uploading with command: " + data);

                connectToServer(data, false);


                //After uploading receive the updated server list
                connectToServer("", false);

            }
        });


        gridPane.addRow(0, uploadBtn);
        gridPane.addColumn(1, downloadBtn);

        gridPane.addRow(1, clientFiles);
        gridPane.addColumn(1, serverFiles);
        gridPane.setHgap(5.0f);


        //Get proper names of server files
        connectToServer("", false);


        primaryStage.setScene(new Scene(gridPane, 600, 275));
        primaryStage.show();



    }

    private String readFile(File file)
    {

        String data = "";

        //Check for file
        if (file.exists()) {
            // count the words in this file
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //Read in all of file
            while (scanner.hasNextLine()) {
                data += scanner.nextLine();
                data += " \n";
            }

            return data;
        }

        System.out.println("Could not find file.");

        return"";
    }

    //First this function tries to connect to the server. Then it will send/receive something depending on the arguments.
    void connectToServer(String data, boolean downloading)
    {
        Socket socket = null;

        try {
            socket = new Socket("localhost", 8080);
        } catch (UnknownHostException e) {
        System.err.println("Unknown host");
    } catch (IOException e) {
        System.err.println("IOException while connecting to server");
    }

    //Check if socket succeeded and isn't null. If it failed then exit the function.
    if (socket == null) {
        System.err.println("Failed to connect");
        return;
    } //If so then start downloading/uploading
        else {


        if (downloading) {
            //Send request to download file of given name (data)
            send("DOWNLOAD" + "\n" + data + "\n",socket);


            //Save file locally
            receiveFile(socket, new File(sharedFolder + "/" + data));

            //Update local directory.
            clientFiles.getItems().clear();
            File directory = new File(sharedFolder);
            makeClientFileList(directory);

        }
        else if (data == "")  //Get a list of file names --------------------------------------------
        {



            //Send DIR command to server
            send("DIR" + "\n", socket);


            if(socket.isClosed()) {
                System.out.println("Socket was closed");
                return;
            }

            //Update server file list (locally)
            receiveList(socket);
        }
        else { //Send data string/file and upload it to server --------------------------



            send(data, socket);


        }
    }

        //Close socket
        try{
            socket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    //Receives the servers files and adds them to the download list
    private void receiveList(Socket socket) {


            System.out.println("Trying to get server file list...");



            try {
                //Receive input from server
                InputStream inStream = socket.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                BufferedReader input = new BufferedReader(reader);
                String currentLine = null;




                serverFiles.getItems().clear();

                //Each line will be for one file
                while ((currentLine = input.readLine()) != null) {


                        serverFiles.getItems().add(currentLine);
                        System.out.println("Got file: " + currentLine);

                    }

               }
             catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failure");
            }

    }

    //Receive a file for download from the server
    private void receiveFile(Socket socket, File file)
    {
        boolean readingSucceeded = false;
        String data = "";



            System.out.println("Trying to get server file list...");



            try {
                //Receive
                InputStream inStream = socket.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                BufferedReader input = new BufferedReader(reader);
                String currentLine = null;



                while ((currentLine = input.readLine()) != null) {



                    data += currentLine;
                    data += "\n";
                    System.out.println(currentLine);
                    readingSucceeded = true;
                }

                //If successful in reading file, then save it
                if (readingSucceeded)
                {
                    System.out.println("Saving file...\n");

                    //Print line by line
                    try(PrintWriter out = new PrintWriter(file))
                    {


                        Scanner scanner = null;

                        scanner = new Scanner(data);


                        currentLine = "";

                        //Read in all of file
                        while (scanner.hasNextLine()) {

                            currentLine = scanner.nextLine();


                            out.println(currentLine);


                        }


                        out.close();

                    } catch (IOException e)
                    {
                        System.out.println("Problem while writing to file D:");
                        e.printStackTrace();
                    }



                }

            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failure");
            }

    }

    //Send a string to the server
    private void send(String data, Socket socket) {
        PrintWriter fileOut = null;

        try {
            fileOut = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fileOut != null) {
            fileOut.println(data);

            fileOut.flush();
            //fileOut.close();

            if (socket.isClosed()) System.out.println("Socked was closed");
        }
    }

    //Function that makes the list of local files
    public String makeClientFileList(File file)
    {

        //Open directory sharedFolder

        ListView<String> listView = new ListView<>();


        if (file.isDirectory()) {
            // process all the files in that directory
            File[] contents = file.listFiles();
            for (File current : contents) {
                clientFiles.getItems().add(current.getName());

            }
        }
        else if (file.exists())
        {

                return   file.getName();
        }


     return   "";
    }


    public static void main(String[] args) {

        launch(args);



    }
}
