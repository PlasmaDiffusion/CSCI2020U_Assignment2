package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientThread extends  Thread {

    private static String sharedFolder = "sharedFolder";


    Socket clientSocket;

    ClientThread(Socket clientSocket) {


        this.clientSocket = clientSocket;
    }

    public void run(){
        //Receiver
        InputStream inStream = null;


        //
        getFileList(new File(sharedFolder));


        try {
            inStream = clientSocket.getInputStream();

            InputStreamReader reader = new InputStreamReader(inStream);
            BufferedReader input = new BufferedReader(reader);
            String currentLine = null;

            //Writer
            PrintWriter outStream = null;

            outStream = new PrintWriter(clientSocket.getOutputStream(), true);


            //Prepare to read in string from client

            String data = "";

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


                    System.out.println("Mode: " + mode);
                }

                System.out.println(currentLine);


                //Look for name of file in second line
                if (currentLineIndex == 1) fileName = currentLine;

                if (currentLineIndex == 1 && mode == 2) break;

                //First line is command and second line is name of file, so ignore those lines for the file's data
                if (currentLineIndex > 1) data += currentLine;


                currentLineIndex++;
            }

            System.out.println("Lines: " + currentLineIndex);

            //outStream.println("pls receive\n pretty pls\n");
            //outStream.flush();

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
                        System.out.println("Ready to send " + fileList.get(i));
                        outStream.println(fileList.get(i));

                    }

                    outStream.flush();
                    System.out.println("Sent file list");


                    break;

                case 1:

                    //Get data from a new file and save it
                    receiveFile(data, new File(sharedFolder + "/" + fileName));


                    break;

                case 2:

                    //Send a file to be downloaded
                    outputFile(outStream, new File(sharedFolder + "/" + fileName));


                    break;
            }

            //Send

            if (input != null) input.close();

            if (outStream != null) outStream.close();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



        //Thread can now be joined
        try{

            System.out.println("Thread finished.");
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

                System.out.println(data);
            }


            out.print(data);
            out.flush();

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

