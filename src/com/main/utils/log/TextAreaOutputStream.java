package com.main.utils.log;


import com.main.utils.Utils;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class TextAreaOutputStream extends OutputStream {

    private final JTextArea textArea;
    private final StringBuilder sb = new StringBuilder();
    private String title;

    public TextAreaOutputStream(final JTextArea textArea, String title) {
        this.title = title;
        this.textArea = textArea;
//        textArea.setFont(new java.awt.Font("宋体", 0, 14));
        sb.append(title + "> ");
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public void write(int b) throws IOException {

        if (b == '\n') {
            final String text = sb.toString() + "\n";
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
//                    String out = new String(text.getBytes("ISO-8859-1"));
                        textArea.append(Utils.INSTANCE.getTime() + ":" + text);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            sb.setLength(0);
            sb.append(title + "> ");
            return;
        }

        sb.append((char) b);
    }
}
