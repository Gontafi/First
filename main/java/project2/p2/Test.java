package project2.p2;
import java.util.Arrays;
public class Test extends Question{
    private String[] options;
    private int numOfOptions = 0;
    public Test(){}
    public void setOptions(String[] o){
        options = o.clone();numOfOptions = o.length;
    }
    public String getOptionAt(int i){
        return options[i];
    }
    @Override public String toString() {
        return "Test{" + "options=" + Arrays.toString(options) +
                ", numOfOptions=" + numOfOptions + '}';
    }
}