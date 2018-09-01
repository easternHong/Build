package com.main.utils.log;

import javax.swing.*;
import java.util.LinkedList;

public class ConsoleThread implements Runnable {

    private JTextArea jTextArea;
    private JScrollPane jScrollPane;
    private final LinkedList<String> logList = new LinkedList<String>();

    public ConsoleThread() {
    }

    public void setjScrollPane(JScrollPane jScrollPane) {
        this.jScrollPane = jScrollPane;
    }

    public void setjTextArea(JTextArea jTextArea) {
        this.jTextArea = jTextArea;
    }

    public void append(final String ctn) {
        synchronized (logList) {
            logList.add(ctn);
            if (jTextArea != null && jScrollPane != null) {
                logList.notify();
            }
        }
    }

    public void run() {
        try {
            while (true) {
                synchronized (logList) {
                    if (logList.size() == 0) {
                        logList.wait();
                    } else {
                        synchronized (logList) {
                            this.jTextArea.append(logList.poll());
                        }
                        this.jTextArea.setCaretPosition(this.jTextArea.getText().length());

                        try { //使垂直滚动条自动向下滚动
                            jScrollPane.getVerticalScrollBar().setValue(jScrollPane.getVerticalScrollBar().getMaximum());
                        } catch (Exception ex) {
                            //异常不做处理
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
