package sample;

import javafx.application.Application;
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

        System.out.println(parameters.getRaw().size());
        for (int i = 0; i < parameters.getRaw().size(); i++)
        {
            System.out.println(parameters.getRaw().get(i));

            if (i == 0) computerName = parameters.getRaw().get(0);

            if (i == 1) sharedFolder = parameters.getRaw().get(1);

        }




        //Client listview
        clientFiles = new ListView<>();
        ObservableList<String> files = FXCollections.observableArrayList("LOL", "TEST");
        clientFiles.setItems(files);

        clientFiles.getItems().clear();
        //Load in shared folder

        sharedFolder = "/home/scott/Documents/Assignment2/sharedFolder";

        File directory = new File(sharedFolder);



        makeClientFileList(directory);

        //Server listview
        serverFiles = new ListView<>();
        ObservableList<String> files2 = FXCollections.observableArrayList("server", "server.txt");
        serverFiles.setItems(files2);



        //Download Button
        Button downloadBtn = new Button();
        downloadBtn.setText("Download");

        downloadBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

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

                String file = clientFiles.getSelectionModel().getSelectedItem();


                String data = "UPLOAD\n" + file + "\n";

               data += readFile(new File(sharedFolder + "/" + file));


                System.out.println("Uploading with command: " + data);

                connectToServer(data, false);


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
                data += "\n";
            }

            return data;
        }

        System.out.println("Could not find file.");

        return"";
    }


    void sendFiles(String data)
    {
        try {
            DatagramSocket socket = new DatagramSocket(16789);

            String IP = "192.197.54.136";

            InetAddress address = InetAddress.getByName(IP);
            DatagramPacket outputPacket = new DatagramPacket(data.getBytes(), data.length(), address, 12465);

            socket.send(outputPacket);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Alt
        Socket socket = new Socket("localhost", 8080);
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        out.print(data);
        out.flush();*/
    }

    void connectToServer(String data, boolean downloading)
    {
        Socket socket = null;

        try {
            socket = new Socket("localhost", 8080);
        } catch (UnknownHostException e) {
        System.err.println("Unknown host");
    } catch (IOException e) {
        System.err.println("IOEXception while connecting to server");
    }

    //Check if socket succeeded and isn't null
    if (socket == null) {
        System.err.println("Null socket");
    } //If so then start downloading/uploading
        else {

        //S
        if (downloading) {
            //Send request to download file of given name (data)
            send("DOWNLOAD" + "\n" + data + "\n",socket);


            //Save file locally
            receiveFile(socket, new File(sharedFolder + "/" + data));

        }
        else if (data == "")
        {

            //Get a list of file names --------------------------------------------

            //Send DIR command to server
            send("DIR" + "\n", socket);


            if(socket.isClosed()) System.out.println("Socket was closed");

            //Close the send socket
            /*try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //open socket...
            try {
                socket = new Socket("localhost", 8080);

                //socket.setKeepAlive(true);

            } catch (IOException e) {
                e.printStackTrace();
            }*/


            receiveList(socket);
        }
        else {

            //Send data string/file and upload it to server --------------------------

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
        boolean tryingToConnect = true;


        while (tryingToConnect) {


            System.out.println("Trying to get server file list...");



            try {
                //Receive
                InputStream inStream = socket.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                BufferedReader input = new BufferedReader(reader);
                String currentLine = null;

                System.out.println(socket.getInputStream());

                //BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //while (!input.ready())
                //{
                //    System.out.println("waiting for input");
                //}


                serverFiles.getItems().clear();

                while ((currentLine = input.readLine()) != null) {


                        serverFiles.getItems().add(currentLine);
                        System.out.println("Got file: " + currentLine);
                        tryingToConnect = false;
                    }

               }
             catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failure");
            }
        }
    }

    private void receiveFile(Socket socket, File file)
    {
        boolean tryingToConnect = true;
        String data = "";


        while (tryingToConnect) {


            System.out.println("Trying to get server file list...");



            try {
                //Receive
                InputStream inStream = socket.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                BufferedReader input = new BufferedReader(reader);
                String currentLine = null;

                System.out.println(socket.getInputStream());


                while ((currentLine = input.readLine()) != null) {



                    data += currentLine;
                    System.out.println(currentLine);
                    tryingToConnect = false;
                }

                //Check if successful in reading file
                if (!tryingToConnect)
                {
                    //Print line by line
                    try(PrintWriter out = new PrintWriter(file))
                    {

                        out.print(data);

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
    }

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

    //Recursive function that can add onto the list of local files
    public String makeClientFileList(File file)
    {

        //Open directory sharedFolder

        ListView<String> listView = new ListView<>();


        if (file.isDirectory()) {
            // process all the files in that directory
            File[] contents = file.listFiles();
            for (File current : contents) {
                clientFiles.getItems().add(makeClientFileList(current));

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
