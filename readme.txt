Scott Cooper 100580683

Repostiory link: https://github.com/PlasmaDiffusion/CSCI2020U_Assignment2


To compile the client program, go to src/sample and enter: javac Main.java

To compile the server program, go to src/sample and enter: javac Main.java ClientThread.java

To run the client, enter from the Assignment 2 folder:java -cp src sample.Main [computer name] [shared folder path]

To run the server, enter from the Assignment2_Server folder: java -cp src sample.Main sample.ClientThread


The server will use files from sharedFolder. The client also has a sharedFolder but can work with whatever other folders.
As I did this assignment individually and there was no specification for inputting an address, the program defaults to using local host when connecting.
