package project2.p2;

import java.io.BufferedInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class test_task {
    public static void main(String[] args) {
        BufferedInputStream bis = new BufferedInputStream(System.in);
        Scanner in = new Scanner(bis);
        int t = in.nextInt();
        while(t-- > 0){
            int n = in.nextInt();
            int a[] = new int[n];
            for(int i = 0;i < n;i++){
                a[i] = in.nextInt();
            }
            int res[] = new int[n];
            int d = -1;
            for(int i = 0;i < n;i++){
                if(i == n-1 && a[i] == 0 && n>1){
                    a[i-1] = i+1;
                    a[i] = i;
                }
                if(a[i] != i+1){
                    if(d!=-1) {
                        res[i] = d;
                        d = -1;
                    }
                    else
                        res[i] = i + 1;
                }
                else if(i + 1 < n){
                    res[i] = i + 2;
                    d = i + 1;
                }
            }
            for(int i = 0;i < n;i++){
                System.out.print(res[i] + " ");
            }
            System.out.println();
        }
    }
}

