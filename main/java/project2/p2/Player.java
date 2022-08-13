package project2.p2;

import java.util.Arrays;

public class Player implements Comparable<Player>{
    int rightans = 0;
    String name;
    String []ans;
    int points = 0;
    public Player(){

    }
    public void setAnslen(int length){
        ans = new String[length];
    }
    public void setAns(int index, String answer){
        ans[index] = answer;
    }
    public void ansCheck(int index, String right, int sec){
        if(ans[index].equals(right)) {
            rightans++;
            points += (int)((double)sec/30 * 1000);
            System.out.println(name + ":" + points + " sec:" + sec);
        }
        System.out.println(name + ":" + ans[index] + " compare " + right + " is " +ans[index].equals(right));
    }

    public String getAns(int i) {
        return ans[i];
    }
    public Player(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Player{" +
                "rightans=" + rightans +
                ", name='" + name + '\'' +
                ", ans=" + Arrays.toString(ans) +
                '}';
    }

    @Override
    public int compareTo(Player o) {
        if(points == o.points)
            return 0;
        else if(points < o.points)
            return 1;
        else
            return -1;
    }
}
