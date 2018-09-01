package com.main.utils;

import com.main.utils.log.ConsoleThread;

import javax.swing.*;

public class Log {

    public static Log instance = new Log();
    private static ConsoleThread consoleThread = new ConsoleThread();

    public void init(JTextArea jTextArea, JScrollPane jScrollPane) {
        consoleThread.setjScrollPane(jScrollPane);
        consoleThread.setjTextArea(jTextArea);
        new Thread(consoleThread).start();
    }

    public static void i(String message) {
        consoleThread.append(">>>" + message);
        consoleThread.append("\n");
        System.out.println(message);
    }

}
