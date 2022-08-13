package project2.p2;
public abstract class Question{
    private String description;
    private String answer;
    public String getDescription(){
        return description;
    }
    public String getAnswer() {
        return answer;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String toString() {
        return "Question{"+"description='"+description+'\''+", answer='"+answer+'\''+'}';
    }
    public void setAnswer(String answer) {this.answer = answer;}
}