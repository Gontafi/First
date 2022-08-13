package project2.p2;
import java.io.*;
import java.util.*;
public class Quiz {
    private String name;
    private final ArrayList<Question> questions = new ArrayList<>();
    public void addQuestions(Question q) {questions.add(q);}
    public ArrayList<Question> getQuestions(){
        return questions;
    }
    public Quiz(){}
    public void setName(String name) {this.name = name;}
    public String getName() {return name;}
    public static Quiz loadFromFile(String s) throws InvalidQuizFormatException, IOException {
        File file = new File(s);
        FileReader fr = new FileReader(s);
        BufferedReader r = new BufferedReader(fr);
        Quiz quiz = new Quiz();
        quiz.setName(file.getName());
        String line = r.readLine();
        if (line == null || line.equals("")) throw new InvalidQuizFormatException();
        while (line != null) {
            FillIn typeF = new FillIn();
            Test typeT = new Test();
            TrueFalse tf = new TrueFalse();
            String q = line;
            if(q.contains("(tf)")){
                tf.setDescription(q.substring(0,q.indexOf("(tf)")));
                line = r.readLine();
                if (line == null || line.equals("")) throw new InvalidQuizFormatException();
                tf.setAnswer(line);
                quiz.addQuestions(tf);
                line = r.readLine();
                line = r.readLine();
            }
            else {
                line = r.readLine();
                if (line == null || line.equals("")) throw new InvalidQuizFormatException();
                String ans = line;
                line = r.readLine();
                if (q.contains("{blank}")) {
                    typeF.setDescription(q.substring(0, q.indexOf("{blank}")));
                    typeF.setAnswer(ans);
                    quiz.addQuestions(typeF);

                } else {
                    typeT.setDescription(q);
                    String[] answers = new String[4];
                    answers[0] = (ans);
                    typeT.setAnswer(ans);
                    for (int i = 1; i < 4; i++) {
                        answers[i] = (line);
                        line = r.readLine();
                        if (i != 3 && (line == null || line.equals(""))) throw new InvalidQuizFormatException();
                    }
                    String[] options = answers.clone();
                    typeT.setOptions(options);
                    quiz.addQuestions(typeT);
                }
                line = r.readLine();
            }
        }
        r.close();
        fr.close();
        return quiz;
    }
    public void m() { System.out.println("==========================================================\n");}
    public void velcometext(){System.out.println("WELCOME TO " + getName().substring(0, getName().indexOf(".")) + " QUIZ!");}
    public void l(){System.out.println("\n__________________________________________________________\n");}

    public boolean testquiz(Question q){
        Scanner in = new Scanner(System.in);
        boolean correct = false;
        Test t = (Test)q;
        List<String> shuffled = new ArrayList<>();
        for(int i = 0; i < 4; i++)
            shuffled.add(t.getOptionAt(i));
        Collections.shuffle(shuffled);
        for(int i = 0; i < 4; i++)
            System.out.println((char)(65+i) + ") " + shuffled.get(i));
        System.out.println("------------------------------");
        System.out.print("Enter the correct choice: ");
        char answer = in.next().charAt(0);
        while(true) {
            if((int)answer <= 68 && (int)answer >= 65) {
                if(t.getAnswer().equals(shuffled.get((int) answer % 65))) {
                    System.out.println("Correct!");
                    correct = true;
                }
                else
                    System.out.println("Incorrect!");
                break;
            } else {
                System.out.print("Invalid choice! Try again (Ex: A, B, ...): ");
                answer = in.next().charAt(0);
            }
        }
        return correct;
    }
    public boolean fillinquiz(Question q){
        Scanner in = new Scanner(System.in);
        boolean correct = false;
        System.out.println("------------------------------");
        System.out.println("Type your answer: ");
        String answer = in.next();
        if(answer.equals(q.getAnswer())) {
            System.out.println("Correct!");
            correct = true;
        }
        else
            System.out.println("Incorrect!");
        return correct;
    }
    public void start() {
        m();velcometext();l();
        int counter = 1;
        int right_ans = 0;
        Collections.shuffle(questions);
        for (Question q : questions){
            System.out.println(counter + "." + q.getDescription());
            if(q.getClass().getSimpleName().equals("Test") && testquiz(q)) right_ans++;
            else if(q.getClass().getSimpleName().equals("FillIn") && fillinquiz(q)) right_ans++;
            counter++;l();
        }
        System.out.println("Correct Answers: " + (((int) ((right_ans * 1.) / questions.size() * 10000))) / 100.0 + "%");
    }
    @Override
    public String toString() {
        return "Quiz{" +
                "name='" + name + '\'' +
                ", questions=" + questions +
                '}';
    }
}
