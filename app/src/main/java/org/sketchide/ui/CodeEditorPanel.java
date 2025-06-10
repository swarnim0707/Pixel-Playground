package org.sketchide.ui;

import javax.swing.JPanel;
import java.awt.*;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class CodeEditorPanel extends JPanel {

    private RSyntaxTextArea textArea;

    public CodeEditorPanel() {
        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setTabsEmulated(true);
        textArea.setTabSize(4);
        textArea.setForeground(Color.BLACK);
        textArea.setBackground(Color.LIGHT_GRAY);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public String getCode() {
        return textArea.getText();
    }
}