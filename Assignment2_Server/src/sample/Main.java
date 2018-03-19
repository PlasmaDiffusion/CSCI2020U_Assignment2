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

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        getFileList(new File(sharedFolder));

        ServerSocket serverSocket = new ServerSocket(port);
        while(connected)
        {
            Socket clientSocket = serverSocket.accept();
            //Receiver
            InputStream inStream = clientSocket.getInputStream();
            InputStreamReader reader = new InputStreamReader(inStream);
            BufferedReader input = new BufferedReader(reader);
            String currentLine = null;


            //Writer
            PrintWriter outStream = null;
            outStream = new PrintWriter(clientSocket.getOutputStream(), true);


            //Prepare to read in string from client

            String data = "";

            int mode = -1;

            int currentLineIndex =0;
            String fileName = "";

            while((currentLine = input.readLine()) != null)
            {

                //Look for a command in the first line
                if (currentLineIndex == 0) {

                    if (currentLine.equals("DIR")){ mode = 0; break;} //Loop will hang
                    else if (currentLine.equals("UPLOAD")) mode = 1;
                    else if (currentLine.equals("DOWNLOAD")) mode = 2;


                    System.out.println("Mode: " + mode);
                }

                System.out.println(currentLine);



                //Look for name of file in second line
                if (currentLineIndex == 1) fileName = currentLine;


                //First line is command and second line is name of file, so ignore those lines for the file's data
                if(currentLineIndex > 1) data+=currentLine;


                currentLineIndex++;
            }

            System.out.println("Lines: " + currentLineIndex);

            outStream.println("pls receive\n pretty pls\n");
            outStream.flush();

            if (clientSocket.isClosed()) System.out.println("socket closed");

            //Do something depending on the command
            switch (mode)
            {
                case 0:

                    //Output all the files available for download
                    //OutputStream outStream = clientSocket.getOutputStream();

                    //Send a list of files
                    List<String> fileList;

                    fileList = getFileList(new File(sharedFolder));

                    for (int i = 0; i<fileList.size(); i++)
                    {
                        System.out.println("Ready to send " + fileList.get(i));
                        outStream.println(fileList.get(i));
                        outStream.println("\n");

                    }

                    outStream.flush();
                    System.out.println("Sent file list");


                    break;

                case 1:

                    //Get data from a new file and save it
                    receiveFile(data, new File(fileName));


                    break;

                case 2:

                    //Send a file to be downloaded

                    break;
            }

            //if (inStream == null) connected = false;

            //Send


            if (input != null) input.close();

            if (outStream != null) outStream.close();
            clientSocket.close();

        }


        serverSocket.close();
    }

    void outputFile(String file)
    {
        try {
            Socket socket = new Socket("myhost.com", 8080);

            PrintWriter out = new PrintWriter(socket.getOutputStream());

            out.print(file);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Called when a client is uploading a file
    void receiveFile(String data, File newFile)
    {

            System.out.println("Saving file...\n");
            System.out.println(data);

            //Print line by line
            try(PrintWriter out = new PrintWriter(newFile))
            {

                out.print(data);

                out.close();

            } catch (IOException e)
            {
                System.out.println("Problem while writing to file D:");
                e.printStackTrace();
            }



    }

    //Called when retrieving a file for a client to download
    private void sendFile(File baseDir, String uri)
    {
        File file = new File(baseDir, uri);
        if (file.exists())
        {
            //sendError(404, "Not Found", );
        }
    }

    //Get all files within directory
    private List<String> getFileList(File file) {

        List<String> fileList = new ArrayList<>();


        if (file.isDirectory()) {
            // process all the files in that directory and add them to list
            File[] contents = file.listFiles();
            for (File current : contents) {
                fileList.add(current.getName());
            }


            return fileList;
        }

        System.out.println("sharedFolder was not found...");

        return fileList;
    }

    public static void main(String[] args) {


        launch(args);
    }
}
