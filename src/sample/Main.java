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

import java.io.File;
import java.io.FileNotFoundException;
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

        //Get command line arguments
        Application.Parameters parameters;
        parameters = getParameters();

        System.out.println(parameters.getRaw().size());
        for (int i = 0; i < parameters.getRaw().size(); i++)
        {
            System.out.println(parameters.getRaw().get(i));
        }

        //Client listview
        clientFiles = new ListView<>();
        ObservableList<String> files = FXCollections.observableArrayList("LOL", "TEST");
        clientFiles.setItems(files);


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

                String file = sharedFolder;

                file += serverFiles.getSelectionModel().getSelectedItem();



                System.out.println("Download " + file);

            }
        });

        //Upload button
        Button uploadBtn = new Button();
        uploadBtn.setText("Upload");

        uploadBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                String file = clientFiles.getSelectionModel().getSelectedItem();

               String data = readFile(new File(file));



                System.out.println(data);



            }
        });


        gridPane.addRow(0, uploadBtn);
        gridPane.addColumn(1, downloadBtn);

        gridPane.addRow(1, clientFiles);
        gridPane.addColumn(1, serverFiles);
        gridPane.setHgap(5.0f);


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
            while (scanner.hasNext()) {
                data += scanner.next();

            }

            return data;
        }

        return"";
    }

    public ListView<String> makeList()
    {
        //Open directory sharedFolder

        ListView<String> listView = new ListView<>();

     return   listView;
    }


    public static void main(String[] args) {

        launch(args);



    }
}
