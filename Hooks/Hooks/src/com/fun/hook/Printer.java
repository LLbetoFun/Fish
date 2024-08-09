package com.fun.hook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Printer {
    public static PrintWriter pw;
    static {
        try {
            pw = new PrintWriter(new FileWriter(new File(System.getProperty("user.home")+"/.fish","log.txt")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
