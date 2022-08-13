package project2.p2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.swing.plaf.TableHeaderUI;
import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Client3 extends Application {

    private Stage window;
    private final double W = 300., H = 150.;
    private Pane root;
    private Socket socket;
    private DataOutputStream toServer;
    private DataInputStream fromServer;
    private String CurrentQuiestion;
    private boolean answered = false;
    private boolean is_sent = false;

    private void connectToServer() throws IOException {
        socket = new Socket("localhost", 2022);
        toServer = new DataOutputStream(socket.getOutputStream());
        fromServer = new DataInputStream(socket.getInputStream());
    }

    public Button kahootButton(String btnColor) {
        Button btn = new Button();
        btn.setMinWidth(W / 2. - 5);
        btn.setMinHeight(H / 2. - 5);
        btn.setStyle("-fx-background-color: " + btnColor);

        btn.setTextFill(Color.WHITE);
        btn.setWrapText(true);
        btn.setPadding(new Insets(10));

        Font font = Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 15);
        btn.setFont(font);
        return btn;
    }
    void gamePin(String nickname){
        answered = false;
        System.out.println();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if(!answered)
                        toServer.writeUTF("Noans");
                    inTest(nickname);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        timer.schedule(timerTask, 14000L);
        StackPane stackPane = new StackPane();
        VBox vBox1 = new VBox(5);
        Button btnRed = kahootButton("red");
        Button btnBlue = kahootButton("blue");
        vBox1.getChildren().addAll(btnRed, btnBlue);
        VBox vBox2 = new VBox(5);
        Button btnOrange = kahootButton("orange");
        Button btnGreen = kahootButton("green");
        vBox2.getChildren().addAll(btnOrange, btnGreen);

        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(vBox1, vBox2);
        stackPane.getChildren().addAll(hBox);

        btnRed.setOnAction(e -> {
            try {
                toServer.writeUTF("Red");
                toServer.flush();

                System.out.println("red");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            btnRed.setDisable(true);
            btnBlue.setDisable(true);
            btnOrange.setDisable(true);
            btnGreen.setDisable(true);
            answered = true;
            timer.cancel();
            inTest(nickname);
        });
        btnBlue.setOnAction(e -> {
            try {
                toServer.writeUTF("Blue");
                toServer.flush();

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            btnRed.setDisable(true);
            btnBlue.setDisable(true);
            btnOrange.setDisable(true);
            btnGreen.setDisable(true);
            answered = true;
            timer.cancel();
            inTest(nickname);
        });

        btnOrange.setOnAction(e -> {
            try {
                toServer.writeUTF("Orange");
                toServer.flush();

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            btnRed.setDisable(true);
            btnBlue.setDisable(true);
            btnOrange.setDisable(true);
            btnGreen.setDisable(true);
            answered = true;
            timer.cancel();
            inTest(nickname);
        });

        btnGreen.setOnAction(e -> {
            try {
                toServer.writeUTF("Green");
                toServer.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            btnRed.setDisable(true);
            btnBlue.setDisable(true);
            btnOrange.setDisable(true);
            btnGreen.setDisable(true);
            answered = true;
            timer.cancel();
            inTest(nickname);
        });
        window.setScene(new Scene(new Pane(stackPane),W, H));
    }
    void inTest(String nickname){
        new Thread(()->{
            System.out.println("in Test, next question finding process");
            try {
                CurrentQuiestion = fromServer.readUTF();
                System.out.println(CurrentQuiestion);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switch (CurrentQuiestion) {
                case "FINISH":
                    System.exit(0);
                case "F":
                    Platform.runLater(() -> {
                        Fillin(nickname);
                    });
                    break;
                case "T":
                    Platform.runLater(() -> {
                        gamePin(nickname);
                    });
                    break;
                case "TF":
                    Platform.runLater(() -> {
                        TrueFalse(nickname);
                    });
                    break;
            }

        }).start();
    }
    void TrueFalse(String nickname){
        StackPane pane = new StackPane();
        Button True = new Button("True");
        Button False = new Button("False");
        True.setMinWidth(W/2);
        True.setMinHeight(H);
        False.setMinWidth(W/2);
        False.setMinHeight(H);
        True.setStyle("-fx-background-color: red");
        False.setStyle("-fx-background-color: blue");
        pane.getChildren().add(new HBox(True,False));
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    toServer.writeUTF("Noans");
                    toServer.flush();
                    True.setDisable(true);
                    False.setDisable(false);
                    inTrueFalse(nickname);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        timer.schedule(timerTask, 14000L);
        True.setOnAction(e ->{
            try {
                toServer.writeUTF("true");
                toServer.flush();
                timer.cancel();
                True.setDisable(true);
                False.setDisable(false);
                inTrueFalse(nickname);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        False.setOnAction(e ->{
            try {
                toServer.writeUTF("false");
                toServer.flush();
                timer.cancel();
                True.setDisable(true);
                False.setDisable(false);
                inTrueFalse(nickname);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        window.setScene(new Scene(new Pane(pane),W, H));
    }
    void inTrueFalse(String nickname){
        new Thread(()->{
            System.out.println("in TrueFalse, next question finding process");
            try {
                CurrentQuiestion = fromServer.readUTF();
                System.out.println(CurrentQuiestion);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switch (CurrentQuiestion) {
                case "F":
                    Platform.runLater(() -> {
                        Fillin(nickname);
                    });
                    break;
                case "T":
                    Platform.runLater(() -> {
                        gamePin(nickname);
                    });
                    break;
                case "TF":
                    Platform.runLater(() -> {
                        TrueFalse(nickname);
                    });
                    break;
            }

        }).start();
    }
    void Fillin(String nickname){
        StackPane pane = new StackPane();
        TextField textField = new TextField();
        textField.setAlignment(Pos.CENTER);
        textField.setMaxWidth(W);
        textField.setMaxHeight(H/2);
        textField.setMinWidth(W);
        textField.setMinHeight(H/2);
        Button send = new Button("SEND");
        send.setMinSize(W,H/2);
        pane.getChildren().add(new VBox(textField,send));
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    toServer.writeUTF("Noans");
                    toServer.flush();
                    send.setDisable(true);
                    inFillin(nickname);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        timer.schedule(timerTask, 14000L);
        send.setOnAction(e ->{
            try {
                toServer.writeUTF(textField.getText());
                toServer.flush();
                timer.cancel();
                send.setDisable(true);
                inFillin(nickname);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        window.setScene(new Scene(new Pane(pane),W, H));
    }
    void inFillin(String nickname){
        new Thread(() ->{
            System.out.println("in Fillin, next question finding process");
            try {
                CurrentQuiestion = fromServer.readUTF();
                System.out.println(CurrentQuiestion);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switch (CurrentQuiestion) {
                case "F":
                    Platform.runLater(() -> {
                        Fillin(nickname);
                    });
                    break;
                case "T":
                    Platform.runLater(() -> {
                        gamePin(nickname);
                    });
                    break;
                case "TF":
                    Platform.runLater(() -> {
                        TrueFalse(nickname);
                    });
                    break;
            }
        }).start();
    }

    public StackPane nicknamePane() {
        StackPane stackPane = new StackPane();
        TextField tf = new TextField();
        tf.setPromptText("Enter username");
        tf.setMaxWidth(W / 3);
        tf.setMinHeight(40);
        tf.setAlignment(Pos.CENTER);
        Button btn = new Button("Enter");
        btn.setMaxWidth(W / 3);
        btn.setMinHeight(40);
        btn.setStyle("-fx-background-color:#333333");
        btn.setTextFill(Color.WHITE);
        btn.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 16));
        VBox vBox = new VBox(10);
        vBox.setMaxWidth(W / 2);
        vBox.setMaxHeight(H / 2);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(tf, btn);

        stackPane.getChildren().addAll(vBox);
        stackPane.setStyle("-fx-background-color: #46178f");
        btn.setOnAction(e -> {

            try {
                if(!is_sent) {

                    toServer.writeUTF(tf.getText());
                    toServer.writeUTF("I am ready!");
                    toServer.flush();
                    is_sent = true;
                }
                new Thread(()->{
                    try {
                        int response = 0;
                        try {
                            response = fromServer.readInt();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        if(response == 555) {
                            System.out.println("successfully started");
                            System.out.println("process");
                            CurrentQuiestion = fromServer.readUTF();
                            System.out.println(CurrentQuiestion);
                            if (CurrentQuiestion.equals("T")) {
                                Platform.runLater(() -> {
                                    window.setTitle(tf.getText());
                                    gamePin(tf.getText());
                                });
                            } else if (CurrentQuiestion.equals("F")) {
                                Platform.runLater(() -> {
                                    window.setTitle(tf.getText());
                                    Fillin(tf.getText());
                                });
                            }
                            else if(CurrentQuiestion.equals("TF")){
                                Platform.runLater(() -> {
                                    window.setTitle(tf.getText());
                                    TrueFalse(tf.getText());
                                });
                            }
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
                btn.setDisable(true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return stackPane;
    }

    public StackPane pinPane() {
        StackPane stackPane = new StackPane();
        TextField tf = new TextField();
        tf.setPromptText("Game PIN");
        tf.setMaxWidth(W / 3);
        tf.setMinHeight(40);
        tf.setAlignment(Pos.CENTER);
        Button btn = new Button("Enter");
        btn.setMaxWidth(W / 3);
        btn.setMinHeight(40);
        btn.setStyle("-fx-background-color:#333333");
        btn.setTextFill(Color.WHITE);
        btn.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 16));
        VBox vBox = new VBox(10);
        vBox.setMaxWidth(W / 2);
        vBox.setMaxHeight(H / 2);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(tf, btn);

        stackPane.getChildren().addAll(vBox);
        stackPane.setStyle("-fx-background-color: #3e147f");

        btn.setOnAction(e -> {
            try {
                toServer.writeInt(Integer.parseInt(tf.getText()));
                toServer.flush();
            } catch (IOException ignored) {
            }
            btn.setDisable(true);
            new Thread(() ->{
                String status = null;
                try {
                    status = fromServer.readUTF();
                } catch (IOException ex) {
                    System.out.println("you have been kicked from server");
                    System.exit(5);
                    throw new RuntimeException(ex);
                }
                if (status.equals("Success!")) {
                    Platform.runLater(()->{
                        window.setScene(new Scene(nicknamePane(), W, H));
                        window.setTitle("Enter Nickname");
                    });
                }
                System.out.println(status);
            }).start();
        });
        return stackPane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        connectToServer();
        root = pinPane();
        window.setScene(new Scene(root, W, H));
        window.show();
        window.setTitle("Enter PIN!");
        root.requestFocus();
    }
}
