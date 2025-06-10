package org.sketchide.ui;

import javax.swing.*;
import java.awt.*;

import org.sketchide.engine.SketchRunner;

public class SketchIDEFrame extends JFrame {

    private CodeEditorPanel codeEditorPanel;
    private SketchCanvasPanel canvasPanel;
    private SketchRunner runner;

    public SketchIDEFrame() {
        setTitle("Sketch IDE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create panels
        codeEditorPanel = new CodeEditorPanel();
        canvasPanel = new SketchCanvasPanel();

        // Split layout
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            codeEditorPanel,
            canvasPanel
        );
        splitPane.setDividerLocation(500);

        add(splitPane, BorderLayout.CENTER);

        setJMenuBar(createMenuBar());

        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("New"));
        fileMenu.add(new JMenuItem("Open"));
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> System.out.println("Save logic here!"));
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Export"));
        JMenuItem runItem = new JMenuItem("Run Code");
        runItem.addActionListener(e -> {
            String code = codeEditorPanel.getCode();
            runner = new SketchRunner(code, canvasPanel);
            runner.run();
        });
        fileMenu.add(runItem);

        // Transform Menu
        JMenu transformMenu = new JMenu("Transform");
        transformMenu.add(new JMenuItem("Rotate"));
        transformMenu.add(new JMenuItem("Scale"));
        transformMenu.add(new JMenuItem("Mirror"));

        // Audio Menu
        JMenu audioMenu = new JMenu("Audio");
        audioMenu.add(new JMenuItem("Convert to Audio"));

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(transformMenu);
        menuBar.add(audioMenu);

        return menuBar;
    }
}
