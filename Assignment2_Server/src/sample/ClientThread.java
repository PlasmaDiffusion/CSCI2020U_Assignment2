package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//This class contains all functions the server will need, along with serving as a thread.
public class ClientThread extends  Thread {

    private static String sharedFolder = "sharedFolder";

    //Boolean for Main to know when a thread is done and joined.
    public boolean finished;

    Socket clientSocket;

    ClientThread(Socket clientSocket) {

        finished = false;
        this.clientSocket = clientSocket;
    }

    public void run(){
        //Receiver
        InputStream inStream = null;


        //Prepare the file list
        getFileList(new File(sharedFolder));


        //Get string from client and prepare it for reading
        try {
            inStream = clientSocket.getInputStream();

            InputStreamReader reader = new InputStreamReader(inStream);
            BufferedReader input = new BufferedReader(reader);
            String currentLine = null;

            PrintWriter outStream = null;

            outStream = new PrintWriter(clientSocket.getOutputStream(), true);


            //Prepare some variables for interpreting the data
            String data = "";

            //Mode changes based on what the command is.
            int mode = -1;

            int currentLineIndex = 0;
            String fileName = "";


            while ((currentLine = input.readLine()) != null) {

                //Look for a command in the first line
                if (currentLineIndex == 0) {

                    if (currentLine.equals("DIR")) {
                        mode = 0;
                        break;
                    } //Loop will hang
                    else if (currentLine.equals("UPLOAD")) mode = 1;
                    else if (currentLine.equals("DOWNLOAD")) mode = 2;


                }

                System.out.println(currentLine);


                //Look for name of file in second line
                if (currentLineIndex == 1) fileName = currentLine;

                if (currentLineIndex == 1 && mode == 2) break;

                //First line is command and second line is name of file, so ignore those lines for the file's data
                if (currentLineIndex > 1) {data += currentLine; data+="\n";}


                currentLineIndex++;
            }

            if (clientSocket.isClosed()) System.out.println("socket closed");

            //Do something depending on the command
            switch (mode) {
                case 0:

                    //Output all the files available for download
                    //OutputStream outStream = clientSocket.getOutputStream();

                    //Send a list of files
                    List<String> fileList;

                    fileList = getFileList(new File(sharedFolder));

                    for (int i = 0; i < fileList.size(); i++) {
                        System.out.println(fileList.get(i));
                        outStream.println(fileList.get(i));

                    }

                    outStream.flush();
                    System.out.println("Sent file list");


                    break;

                case 1:

                    //Get data from a new file and save it
                    receiveFile(data, new File(sharedFolder + "/" + fileName));

                    //Update own file list for whenever a client needs it
                    getFileList(new File (sharedFolder));

                    break;

                case 2:

                    //Send a file to be downloaded
                    outputFile(outStream, new File(sharedFolder + "/" + fileName));


                    break;
            }


            if (input != null) input.close();

            if (outStream != null) outStream.close();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



        //Thread can now be joined
        try{

            finished = true;
            join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Output file for a client to download
    void outputFile(PrintWriter out, File file)
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
                data +="\n";

            }


            out.print(data);
            out.flush();

        }
    }

    //Called when a client is uploading a file
    void receiveFile(String data, File newFile)
    {

        System.out.println("Saving file...\n");

        //Print line by line
        try(PrintWriter out = new PrintWriter(newFile))
        {


            Scanner scanner = null;

                scanner = new Scanner(data);


            String currentLine = "";

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



}

