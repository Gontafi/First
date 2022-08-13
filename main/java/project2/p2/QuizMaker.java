package project2.p2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class QuizMaker extends Application {
    private Stage window;
    private ArrayList<Question> q;
    private Quiz quiz;
    private String[] ansSaver;
    private int rans = 0;
    private Label TIME = new Label("");
    private Timeline tl;
    private final Media media = new Media(new File("/Users/User/Desktop/java/IdeaProjects/p2/src/kahoot_music.mp3").toURI().toString());
    private final MediaPlayer mediaPlayer = new MediaPlayer(media);
    private boolean ShowAnswers = false;
    private boolean isoff = false;
    private int sec = 0;
    private boolean shuffelquestions = true;
    private boolean started = false;
    private int QUIESTION_INDEX = 0;
    private boolean isTest;
    private boolean isTruefalse;
    private ServerSocket server;
    private Map<String, Player> playerMap;
    private ArrayList<String> nicks = new ArrayList<>();
    private int[] secondomer;
    private int isready = 0;
    private LinkedList<Threader> threads = new LinkedList<>();
    private ArrayList<ArrayList<String>> SFQ = new ArrayList<>();
    private boolean inQuestion = false;
    private int clientNo = 1;
    private ArrayList<Boolean> send = new ArrayList<>();
    private boolean Answered = false;
    private boolean finish = false;
    private int Pin = genPin();
    public void Checkan(){
        for(int i = 0; i < q.size();i++){
            if(ansSaver[i].equals(q.get(i).getAnswer()))
                rans++;
        }
    }
    public void changer(){
        sec--;
        TIME.setText(String.valueOf(sec));
    }

    public void fillquestion(){

        q = quiz.getQuestions();
        ansSaver = new String[q.size()];

        if(shuffelquestions)
            Collections.shuffle(q);
        for(Question v : q ){
            if(v.getClass().getSimpleName().equals("Test")){
                ArrayList<String> str = new ArrayList<>();
                Test t = (Test)v;
                for(int i = 0; i < 4 ;i++){
                    str.add(t.getOptionAt(i));
                }
                Collections.shuffle(str);
                str.add("Noans");
                SFQ.add(str);
            }
            else
                SFQ.add(new ArrayList<>());
        }
    }
    public StackPane startpage() throws IOException {
        StackPane sp = new StackPane();
        FileChooser fc = new FileChooser();
        Button button = new Button("Select .txt file");
        Image image = new Image(new FileInputStream("/Users/User/Desktop/java/IdeaProjects/p2/src/img/background.jpg"));
        ImageView imageView = new ImageView(image);

        imageView.setFitHeight(505D);
        imageView.setFitWidth(600D);

        button.setOnAction(actionEvent -> {
            File sf = fc.showOpenDialog(window);
            try {
                quiz = Quiz.loadFromFile(sf.getAbsolutePath());
                fillquestion();
            } catch (InvalidQuizFormatException | IOException e) {
                e.printStackTrace();
            }
            try {
                RequestPage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        sp.getChildren().addAll(imageView, button);
        return sp;
    }
    private static int genPin(){
        return (int) (Math.random()*1000000);
        //return 12345;
    }
    private void remover(String name){
        playerMap.remove(name);
        nicks.remove(name);
    }

    public void RequestPage() throws IOException {
        StackPane root = new StackPane();

        root.setStyle("-fx-background-color: #3e147f");

        BorderPane borderPane = new BorderPane();

        Label lbl = new Label("Game PIN:\n" + Pin);
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20));

        lbl.setAlignment(Pos.CENTER);
        lbl.setMinWidth(600);
        borderPane.setTop(lbl);
        Button run = new Button("Start");
        run.setAlignment(Pos.CENTER);
        run.setMaxWidth(600D);
        borderPane.setBottom(run);
        root.getChildren().addAll(borderPane);
        server = new ServerSocket(2022);
        th tp = new th(run, borderPane);
        tp.start();
        run.setDisable(true);
        window.setScene(new Scene(root, 600D, 500D));
        window.setTitle("Waiting...");
    }
    class th extends Thread{
        private Button r;
        private BorderPane bp;

        public th(Button bt, BorderPane b){
            r = bt;
            bp = b;
        }
        @Override
        public void run() {
            while (true) {
                if(!started) {
                    try {
                        System.out.println("Waiting for incomes");
                        Socket socket = server.accept();
                        DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
                        System.out.println(clientNo + " Client is Connected!");
                        //threads.add(new Thread(new Threader(socket,r,bp)));
                        //threads.get(threads.size()-1).start();
                        Threader t = new Threader(socket, r, bp);
                        threads.add(t);
                        threads.getLast().start();

                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
    class Threader extends Thread{
        private Socket socket;
        private Button r;
        private BorderPane bp;
        private boolean connected = false;
        public Threader(Socket sock, Button bt, BorderPane b){
            socket = sock;
            r = bt;
            bp = b;
        }

        @Override
        public void run() {

            try {
                r.setDisable(true);
                String lastpage;
                final int current_index;
                current_index = clientNo - 1;
                DataInputStream fromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
                r.setOnAction(a->{
                    tl = new Timeline(new KeyFrame(Duration.millis(1000  ), actionEvent -> changer()));
                    tl.setCycleCount(15);
                    tl.setAutoReverse(false);
                    mediaPlayer.setVolume(0.5);
                    mediaPlayer.play();
                    window.setTitle(quiz.getName().substring(0  ,quiz.getName().indexOf(".txt")));
                    started = true;
                    secondomer = new int[nicks.size()];
                    try {
                        window.setScene(new Scene(question(QUIESTION_INDEX),600D  , 505D  ));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                if(!connected) {
                    while (true) {
                        int clientPin = fromClient.readInt();
                        if (clientPin != Pin) {
                            toClient.writeUTF("Wrong PIN!");
                            toClient.flush();
                            //socket.close();
                            //stop();
                        } else {
                            toClient.writeUTF("Success!");
                            toClient.flush();
                            connected = true;
                            clientNo++;
                            break;
                        }
                    }
                    String nickname = fromClient.readUTF();
                    System.out.println(clientNo + " " + fromClient.readUTF());
                    r.setDisable(false);
                    System.out.println(nickname);

                    nicks.add(nickname);
                    send.add(false);
                    if (playerMap == null)
                        playerMap = new HashMap<>();
                    playerMap.put(nickname, new Player(nickname));
                    playerMap.get(nickname).setAnslen(q.size());
                    isready++;
                    ArrayList<Label> label = new ArrayList<>();
                    for (String player : nicks) {
                        label.add(new Label(player));
                    }

                    HBox line = new HBox();
                    StackPane center = new StackPane();
                    Platform.runLater(() -> {
                        for (int i = 0; i < nicks.size(); i++) {
                            label.get(i).setTextFill(Color.WHITE);
                            label.get(i).setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.ITALIC, 20));
                            label.get(i).setStyle("-fx-background-color: #FFB3DE");
                            line.getChildren().add(label.get(i));
                            final int index = i;
                            label.get(i).setCursor(Cursor.HAND);
                            label.get(i).setOnMouseClicked(e -> {
//                                    remover(nicks.get(index));
//                                    System.out.println(index);
//                                    threads.remove(index);
//                                    isready--;
                                try {

                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            });
                        }
                        line.setSpacing(10D);
                        line.setAlignment(Pos.CENTER);
                        center.getChildren().add(line);
                        bp.setCenter(center);

                    });
                }
                while (true) {
                    Thread.sleep(10);
                    if(started) {
                        toClient.writeInt(555);
                        while (true) {
                            Thread.sleep(100);
                            if(finish){
                                toClient.writeUTF("FINISH");
                            }
                            //System.out.println("WORKS" + "inq:" + inQuestion + " sent:" + send + " answered:" + Answered);
                            if(!inQuestion) {

                                isTest = q.get(QUIESTION_INDEX).getClass().getSimpleName().equals("Test");
                                isTruefalse = q.get(QUIESTION_INDEX).getClass().getSimpleName().equals("TrueFalse");
                                if (isTest && !send.get(current_index)) {
                                    toClient.writeUTF("T");
                                    toClient.flush();
                                    send.set(current_index,true);
                                } else if(!isTest && !send.get(current_index)) {
                                    if(isTruefalse){
                                        toClient.writeUTF("TF");
                                        toClient.flush();
                                        send.set(current_index, true);
                                        System.out.println("TRUEFALSE");
                                    }
                                    else {
                                        toClient.writeUTF("F");
                                        toClient.flush();
                                        send.set(current_index, true);
                                    }
                                }
                            }
                            if(inQuestion) {
                                Answered = playerMap.get(nicks.get(current_index)).getAns(QUIESTION_INDEX) != null;
                                if (isTest && !Answered) {
                                    String clientChoice = fromClient.readUTF();
                                    if (clientChoice.equals("Noans")) {
                                        playerMap.get(nicks.get(current_index)).setAns(QUIESTION_INDEX, SFQ.get(QUIESTION_INDEX).get(4));
                                        System.out.println(nicks.get(current_index) + ": " + SFQ.get(QUIESTION_INDEX).get(4));
                                    }
                                    if (clientChoice.equals("Red")) {
                                        playerMap.get(nicks.get(current_index)).setAns(QUIESTION_INDEX, SFQ.get(QUIESTION_INDEX).get(0));
                                        System.out.println(nicks.get(current_index) + ": " + SFQ.get(QUIESTION_INDEX).get(0));
                                    }
                                    if (clientChoice.equals("Orange")) {
                                        playerMap.get(nicks.get(current_index)).setAns(QUIESTION_INDEX, SFQ.get(QUIESTION_INDEX).get(1));
                                        System.out.println(nicks.get(current_index) + ": " + SFQ.get(QUIESTION_INDEX).get(1));
                                    }
                                    if (clientChoice.equals("Blue")) {
                                        playerMap.get(nicks.get(current_index)).setAns(QUIESTION_INDEX, SFQ.get(QUIESTION_INDEX).get(2));
                                        System.out.println(nicks.get(current_index) + ": " + SFQ.get(QUIESTION_INDEX).get(2));
                                    }
                                    if (clientChoice.equals("Green")) {
                                        playerMap.get(nicks.get(current_index)).setAns(QUIESTION_INDEX, SFQ.get(QUIESTION_INDEX).get(3));
                                        System.out.println(nicks.get(current_index) + ": " + SFQ.get(QUIESTION_INDEX).get(3));
                                    }
                                    if(secondomer[current_index] < sec) {
                                        secondomer[current_index] = sec;
                                    }
                                } else if (!isTest && !Answered) {
                                    String clientChoice = fromClient.readUTF();
                                    System.out.println(nicks.get(current_index) + ": " + clientChoice);
                                    playerMap.get(nicks.get(current_index)).setAns(QUIESTION_INDEX, clientChoice);
                                    if(secondomer[current_index] < sec) {
                                        secondomer[current_index] = sec;
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        };
    }
    public void top() {
        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #3e147f");

        Label lbl = new Label("TOP 3 Table\n");
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Times New Roman", FontWeight.BOLD, 40));

        lbl.setAlignment(Pos.CENTER);
        lbl.setMinWidth(600);
        VBox vbox = new VBox();
        vbox.setMinWidth(600D);
        vbox.setMinHeight(350D);
        ArrayList<Player> pl = new ArrayList<>();
        int k = 0;
        for (String name : nicks){
            playerMap.get(name).ansCheck(QUIESTION_INDEX, q.get(QUIESTION_INDEX).getAnswer(), secondomer[k++]);
            pl.add(playerMap.get(name));
        }
        Collections.sort(pl);
        for(Player p : pl){
            System.out.println(p);
        }
        System.out.println(Arrays.toString(secondomer));

        for(int i = 0; i<3;i++){
            if(i < nicks.size()) {
                Label label = new Label();
                label.setText(i + 1 + ". " + pl.get(i).getName() + " pts: " + pl.get(i).points);
                label.setTextFill(Color.WHITE);
                label.setFont(Font.font("Times New Roman", FontWeight.BOLD, 25));
                vbox.getChildren().add(label);
            }
        }
        for(int i = 0; i<nicks.size();i++){
            secondomer[i] = 0;
        }
        vbox.setAlignment(Pos.CENTER);
        borderPane.setCenter(vbox);
        borderPane.setTop(lbl);
        Button run = new Button("Next");
        borderPane.setBottom(new StackPane(run));
        if(q.size() == QUIESTION_INDEX + 1){
            run.setOnAction(ev-> {
                    finish = true;
                    System.exit(0);
                    });
            Platform.runLater(() ->{
                run.setText("Finish");
            });
        }
        else {
            QUIESTION_INDEX++;
            inQuestion = false;
            run.setOnAction(ev -> {
                try {
                    window.setScene(new Scene(question(QUIESTION_INDEX), 600D, 505D));
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        window.setScene(new Scene(borderPane,600D, 500D));
    }
    public FlowPane question(int i) throws IOException, InterruptedException {
        if(!ShowAnswers) {
            for (int j = 0; j < send.size(); j++) {
                send.set(j, false);
            }
            Thread.sleep(1000);
            inQuestion = true;
        }
//        for(Threader t : threads){
//            System.out.println(t.getName() + " " + t.isAlive());
//        }
//        System.out.println(threads.size());
        FlowPane box = new FlowPane(Orientation.VERTICAL);
        Label Q = new Label(i+1 + ")" + q.get(i).getDescription());
        Q.setMaxWidth(600D);
        Q.setAlignment(Pos.TOP_CENTER);
        Q.setTextAlignment(TextAlignment.CENTER);
        Q.setWrapText(true);
        Q.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,20));
        HBox questionline = new HBox();
        questionline.setPrefSize(600D  ,50D  );
        questionline.setAlignment(Pos.TOP_CENTER);
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(questionline);

        Button left = new Button("<-");
        Button right = new Button("next");

        left.setDisable(true);
        if(!ShowAnswers) {
            right.setDisable(true);
            sec = 15;
            tl = new Timeline(new KeyFrame(Duration.millis(1000), actionEvent -> changer()));
            tl.setCycleCount(15);
            tl.setAutoReverse(false);
            tl.play();
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    System.out.println("15 sec passed");
                    tl.stop();
                    left.setDisable(false);
                    right.setDisable(false);
                    ShowAnswers = true;
                    Platform.runLater(()->{
                        try {
                            window.setScene(new Scene(question(i), 600D, 505D));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });

                }
            };
            timer.schedule(timerTask, 16000L);
        }
        left.setVisible(false);
        if(q.size() == i + 1) {
            right.setText("✓");
        }
        left.setMinSize(30D,30D);
        right.setMinSize(30D,30D);
        borderPane.setLeft(new StackPane(left));
        borderPane.setRight(new StackPane(right));

        left.setAlignment(Pos.CENTER);
        right.setAlignment(Pos.CENTER);


        left.setOnAction(actionEvent -> {
            try {
                window.setScene(new Scene(question(--QUIESTION_INDEX), 600D  , 505D  ));
            } catch (InterruptedException | IOException ignored){}
        });
        right.setOnAction(ev ->{
            if(ShowAnswers){
                ShowAnswers = false;
                top();
            }
            else
                top();
        });
        /*
        if(q.size() != i + 1) {
            right.setOnAction(actionEvent -> {
                //try {
                top();
                //window.setScene(new Scene(question(++QUIESTION_INDEX), 600D  , 505D  ));
                //} catch (InterruptedException | IOException ignored) {}
            });
        }
        else{
            right.setOnAction(actionEvent -> {
                try {
                    result();
                } catch (FileNotFoundException ignored) {}
            });
        }*/
        borderPane.setPrefSize(600D  ,350D  );
        VBox timerandimage = new VBox();
        TIME.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15));
        if(q.get(i).getClass().getSimpleName().equals("Test")) {
            questionline.getChildren().add(Q);
            Image img = new Image(new FileInputStream("/Users/User/Desktop/java/IdeaProjects/p2/src/img/logo.png"));
            ImageView imgkahoot = new ImageView(img);
            imgkahoot.setFitHeight(200D);
            imgkahoot.setFitWidth(350D);
            timerandimage.getChildren().addAll(TIME,imgkahoot);
            timerandimage.setAlignment(Pos.CENTER);
            borderPane.setCenter(timerandimage);

            HBox AB = new HBox();
            AB.setPrefSize(600D  ,75D  );
            Rectangle A = new Rectangle(296D  ,74D  );
            Rectangle B = new Rectangle(296D  ,74D  );
            AB.setPadding(new Insets(2D  ));
            AB.setSpacing(5D  );
            A.setFill(Color.RED);
            B.setFill(Color.ORANGE);

            HBox CD = new HBox();
            CD.setPrefSize(600D  ,75D  );
            Rectangle C = new Rectangle(296D,74D);
            Rectangle D = new Rectangle(296D,74D);
            CD.setPadding(new Insets(5D,0,0D,2D));
            CD.setSpacing(5D  );
            C.setFill(Color.BLUE);
            D.setFill(Color.GREEN);

            A.setStyle("-fx-border-style: solid inside;" +
                    "-fx-border-radius: 5;");
            B.setStyle("-fx-border-style: solid inside;" +
                    "-fx-border-radius: 5;");
            C.setStyle("-fx-border-style: solid inside;" +
                    "-fx-border-radius: 5;");
            D.setStyle("-fx-border-style: solid inside;" +
                    "-fx-border-radius: 5;");


            Pane Abutton = new Pane();
            Pane Bbutton = new Pane();
            Pane Cbutton = new Pane();
            Pane Dbutton = new Pane();
            ToggleGroup tg = new ToggleGroup();

            RadioButton Achoice = new RadioButton();
            RadioButton Bchoice = new RadioButton();
            RadioButton Cchoice = new RadioButton();
            RadioButton Dchoice = new RadioButton();
            Achoice.setToggleGroup(tg);
            Bchoice.setToggleGroup(tg);
            Cchoice.setToggleGroup(tg);
            Dchoice.setToggleGroup(tg);

            Label Aans = new Label(SFQ.get(QUIESTION_INDEX).get(0));
            Label Bans = new Label(SFQ.get(QUIESTION_INDEX).get(1));
            Label Cans = new Label(SFQ.get(QUIESTION_INDEX).get(2));
            Label Dans = new Label(SFQ.get(QUIESTION_INDEX).get(3));

            if(ansSaver[i] != null && Aans.getText().equals(ansSaver[i]))
                Achoice.setSelected(true);
            if(ansSaver[i] != null && Bans.getText().equals(ansSaver[i]))
                Bchoice.setSelected(true);
            if(ansSaver[i] != null && Cans.getText().equals(ansSaver[i]))
                Cchoice.setSelected(true);
            if(ansSaver[i] != null && Dans.getText().equals(ansSaver[i]))
                Dchoice.setSelected(true);
            A.setOnMouseClicked(e -> {
                Achoice.setSelected(true);
                ansSaver[i] = Aans.getText();
            });
            B.setOnMouseClicked(e -> {
                Bchoice.setSelected(true);
                ansSaver[i] = Bans.getText();
            });
            C.setOnMouseClicked(e -> {
                Cchoice.setSelected(true);
                ansSaver[i] = Cans.getText();
            });
            D.setOnMouseClicked(e -> {
                Dchoice.setSelected(true);
                ansSaver[i] = Dans.getText();
            });

            Achoice.setOnAction(e -> ansSaver[i] = Aans.getText());
            Bchoice.setOnAction(e -> ansSaver[i] = Bans.getText());
            Cchoice.setOnAction(e -> ansSaver[i] = Cans.getText());
            Dchoice.setOnAction(e -> ansSaver[i] = Dans.getText());

            Aans.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15  ));
            Aans.setTextFill(Color.WHITE);
            Aans.setMaxHeight(70F  );
            Bans.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15  ));
            Bans.setTextFill(Color.WHITE);
            Bans.setMaxHeight(70F  );
            Cans.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15  ));
            Cans.setTextFill(Color.WHITE);
            Cans.setMaxHeight(70F  );
            Dans.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15  ));
            Dans.setTextFill(Color.WHITE);
            Dans.setMaxHeight(70F  );

            HBox Aside = new HBox(Achoice,Aans);
            HBox Bside = new HBox(Bchoice,Bans);
            HBox Cside = new HBox(Cchoice,Cans);
            HBox Dside = new HBox(Dchoice,Dans);

            Aans.setMaxWidth(250D);
            Bans.setMaxWidth(250D);
            Cans.setMaxWidth(250D);
            Dans.setMaxWidth(250D);
            Aans.setWrapText(true);
            Bans.setWrapText(true);
            Cans.setWrapText(true);
            Dans.setWrapText(true);

            Aside.setAlignment(Pos.CENTER_LEFT);
            Aside.setSpacing(5D);
            Aside.setPadding(new Insets(25D,0,0,5D));

            Bside.setAlignment(Pos.CENTER_LEFT);
            Bside.setSpacing(5D);
            Bside.setPadding(new Insets(25D,0,0,5D));

            Cside.setAlignment(Pos.CENTER_LEFT);
            Cside.setSpacing(5D);
            Cside.setPadding(new Insets(25D,0,0,5D));

            Dside.setAlignment(Pos.CENTER_LEFT);
            Dside.setSpacing(5D);
            Dside.setPadding(new Insets(25D,0,0,5D));

            Abutton.getChildren().addAll(A,Aside);
            Bbutton.getChildren().addAll(B,Bside);
            Cbutton.getChildren().addAll(C,Cside);
            Dbutton.getChildren().addAll(D,Dside);
            AB.getChildren().addAll(Abutton,Bbutton);
            CD.getChildren().addAll(Cbutton,Dbutton);
            if(ShowAnswers){

                Achoice.setDisable(true);
                Bchoice.setDisable(true);
                Cchoice.setDisable(true);
                Dchoice.setDisable(true);


                A.setFill(Color.DARKRED);
                B.setFill(Color.DARKRED);
                C.setFill(Color.DARKRED);
                D.setFill(Color.DARKRED);

                if(Aans.getText().equals(q.get(i).getAnswer()))
                    A.setFill(Color.GREEN);
                if(Bans.getText().equals(q.get(i).getAnswer()))
                    B.setFill(Color.GREEN);
                if(Cans.getText().equals(q.get(i).getAnswer()))
                    C.setFill(Color.GREEN);
                if(Dans.getText().equals(q.get(i).getAnswer()))
                    D.setFill(Color.GREEN);

            }

            Abutton.setCursor(Cursor.HAND);
            Bbutton.setCursor(Cursor.HAND);
            Cbutton.setCursor(Cursor.HAND);
            Dbutton.setCursor(Cursor.HAND);

            box.getChildren().addAll(borderPane,AB,CD);
        } else if (q.get(QUIESTION_INDEX).getClass().getSimpleName().equals("TrueFalse")) {

            questionline.getChildren().add(Q);
            Button True = new Button("TRUE");
            Button False = new Button("FALSE");


            True.setStyle("-fx-background-color: red");
            True.setMinHeight(140D);
            True.setMinWidth(300D);
            True.setTextFill(Color.WHITE);
            True.setFont(Font.font("Times New Roman", FontWeight.BOLD, 16));
            False.setMinHeight(140D);
            False.setMinWidth(300D);
            False.setFont(Font.font("Times New Roman", FontWeight.BOLD, 16));
            True.setTextFill(Color.WHITE);
            False.setTextFill(Color.WHITE);
            False.setStyle("-fx-background-color: blue");
            Image img = new Image(new FileInputStream("/Users/User/Desktop/java/IdeaProjects/p2/src/img/logo.png"));
            ImageView imgkahoot = new ImageView(img);
            imgkahoot.setFitHeight(200D);
            imgkahoot.setFitWidth(350D);
            borderPane.setCenter(timerandimage);
            timerandimage.getChildren().addAll(TIME,imgkahoot);
            timerandimage.setAlignment(Pos.CENTER);
            borderPane.setCenter(timerandimage);
            True.setDisable(true);
            False.setDisable(true);
            if(ShowAnswers){
                if("true".equals(q.get(i).getAnswer())) {
                    True.setStyle("-fx-background-color: green");
                    False.setStyle("-fx-background-color: red");
                }
                else{
                    True.setStyle("-fx-background-color: red");
                    False.setStyle("-fx-background-color: green");
                }
            }
            HBox AB = new HBox(True, False);
            AB.setSpacing(5);

            box.getChildren().addAll(borderPane, new StackPane(AB));
        } else {
            Image K = new Image(new FileInputStream("/Users/User/Desktop/java/IdeaProjects/p2/src/img/k.png"));
            ImageView KKK = new ImageView(K);
            KKK.setFitWidth(40D);
            KKK.setFitHeight(25D);

            KKK.setOnMouseClicked(e ->{
                if(isoff) {
                    mediaPlayer.play();
                    isoff = false;
                }
                else{
                    mediaPlayer.stop();
                    isoff = true;
                }
            });

            questionline.getChildren().addAll(KKK,Q);

            Image img = new Image(new FileInputStream("/Users/User/Desktop/java/IdeaProjects/p2/src/img/fillin.png"));
            ImageView imgkahoot = new ImageView(img);
            imgkahoot.setFitHeight(200D  );
            imgkahoot.setFitWidth(350D  );
            timerandimage.getChildren().addAll(TIME, imgkahoot);
            timerandimage.setAlignment(Pos.CENTER);
            borderPane.setCenter(timerandimage);

            Text type = new Text("FillIn question");
            type.setTextAlignment(TextAlignment.CENTER);
            type.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,20));
            TextField field = new TextField();
            field.setMinWidth(500D);

            field.setDisable(true);

            field.setText(type.getText());
            field.setOnMouseClicked(e ->field.clear());
            VBox textline = new VBox(type, field);

            textline.setAlignment(Pos.TOP_CENTER);
            textline.setSpacing(10D  );
            textline.setPadding(new Insets(10D  ));

            HBox answerbox = new HBox(textline);
            answerbox.setPrefSize(600D  ,75D  );
            answerbox.setAlignment(Pos.CENTER);
            if(ansSaver[i] != null){
                field.setText(ansSaver[i]);
            }
            field.setOnKeyReleased(keyEvent -> ansSaver[i] = field.getText());

            Text correct_ans = new Text();
            if(ShowAnswers){
                field.setDisable(true);
                /*if(ansSaver[i].equals(q.get(i).getAnswer())){
                    field.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
                }
                else{
                    field.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                }*/
                correct_ans.setText("Correct answer: " + q.get(i).getAnswer());
                correct_ans.setStyle("-fx-font-size: 20px;");
                correct_ans.setTextAlignment(TextAlignment.CENTER);
            }
            HBox newbox = new HBox(correct_ans);
            newbox.setAlignment(Pos.CENTER);

            answerbox.autosize();
            borderPane.autosize();
            correct_ans.autosize();

            box.getChildren().addAll(borderPane, answerbox, newbox);
        }
        return box;
    }
    public void result() throws FileNotFoundException {
        VBox res = new VBox();
        System.out.println(playerMap.size());
        for(int i = 0;i<playerMap.size();i++){
            System.out.println(playerMap.get(i).toString());
        }
        if(!ShowAnswers) Checkan();
        mediaPlayer.stop();
        tl.stop();
        isoff = true;

        Text Head = new Text("Your Result:");
        Head.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,25  ));
        Text percentage = new Text(String.valueOf((((int)(rans/q.size()*10000)))/100.0) + "%");
        percentage.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15  ));
        Text rightans = new Text("Number of correct answers: " + rans + "/" + q.size());
        rightans.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,12  ));
        Text timer = new Text("Finished in " + TIME.getText());
        timer.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,12  ));

        Button showans = new Button("Show answers");
        showans.setMinSize(300D  ,50D  );
        showans.setStyle("-fx-background-color: rgb(45, 94, 227); ");
        showans.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15  ));
        showans.setTextFill(Color.WHITE);
        showans.setCursor(Cursor.HAND);
        showans.setOnAction(e ->{
            ShowAnswers = true;
            try {
                window.setScene(new Scene(question(0),600F  , 505F  ));
            } catch (FileNotFoundException | InterruptedException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        Button Close = new Button("Close Test");
        Close.setMinSize(300D  ,50D  );
        Close.setStyle("-fx-background-color: db2121; ");
        Close.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15));
        Close.setTextFill(Color.WHITE);
        Close.setCursor(Cursor.HAND);
        Close.setOnAction(ActionEvent ->{
            System.exit(0);
        });

        Image finish = new Image(new FileInputStream("/Users/User/Desktop/java/IdeaProjects/p2/src/img/result.png"));
        ImageView finishimg = new ImageView(finish);

        finishimg.setFitHeight(280D  );
        finishimg.setFitWidth(500D  );

        HBox Header = new HBox(Head);
        HBox percent = new HBox(percentage);
        HBox right = new HBox(rightans);
        HBox time = new HBox(timer);
        HBox blue = new HBox(showans);
        HBox red = new HBox(Close);
        HBox p = new HBox(finishimg);

        Header.setMinHeight(34D  );
        percent.setMinHeight(34D  );
        right.setMinHeight(34D  );
        time.setMinHeight(34D  );
        blue.setMinHeight(75D  );
        red.setMinHeight(34D  );

        Head.setTextAlignment(TextAlignment.CENTER);
        Header.setAlignment(Pos.CENTER);
        percent.setAlignment(Pos.CENTER);
        right.setAlignment(Pos.CENTER);
        time.setAlignment(Pos.CENTER);
        blue.setAlignment(Pos.CENTER);
        red.setAlignment(Pos.CENTER);
        p.setAlignment(Pos.CENTER);


        res.getChildren().addAll(Header, percent, right,time, blue, red, p);
        res.autosize();
        window.setScene(new Scene(res,600D  , 505D  ));
    }

    @Override
    public void start(Stage stage) throws IOException{
        window = stage;
        Scene scene = new Scene(new StackPane(startpage()), 600D  , 505D  );
        //Scene scene = new Scene(top(), 600D  , 505D);

        TIME.setFont(Font.font("Times New Roman",FontWeight.BOLD,FontPosture.REGULAR,15  ));

        shuffelquestions = true;

        window.setScene(scene);
        window.show();
    }

    public static void main(String[] args) {
        launch();
    }
}