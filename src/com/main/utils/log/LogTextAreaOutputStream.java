package com.main.utils.log;

import javax.swing.*;

public class LogTextAreaOutputStream extends TextAreaOutputStream {
    public LogTextAreaOutputStream(JTextArea textArea, String title) {
        super(textArea, title);
    }
}
