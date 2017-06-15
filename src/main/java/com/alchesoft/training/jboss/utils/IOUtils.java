package com.alchesoft.training.jboss.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IOUtils {

    private static BufferedReader bufRead;

    static {
        InputStreamReader isr = new InputStreamReader(System.in) ;

        bufRead = new BufferedReader(isr) ;
    }
    public static String readLine(String s) {
        System.out.print(s);
        String returnValue = null;
        try {
            returnValue =  bufRead.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnValue;
    }
    public static int readInt(String s) {
        System.out.print(s);
        int returnValue=0;

        try {
            String txt = bufRead.readLine();
            returnValue = Integer.parseInt(txt);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

}
