package org.sketchide.ui;

import javax.swing.*;
import java.awt.*;

import org.sketchide.engine.SketchRunner;

public class SketchIDEFrame extends JFrame {

    private final CodeEditorPanel codeEditorPanel;
    private final SketchCanvasPanel canvasPanel;
    private SketchRunner runner;
    private StringBuilder modeString;
    private final String defaultText = """
            // Static Sketch Mode
            
            @Override
            public void draw(Graphics2D g) {
                // This method is called to render the sketches
                // Use 'g' object (Graphics2D) to render effect and shapes.
                // Example:
                // g.setColor(Color.WHITE): This will set the color of the sketch pen to white
                // g.fillOval(50, 50, 100, 100) : With 100,100 as centre, paints an oval of radius 50
                // Existing imports: java.awt.*;
                // You can import additional libraries
            
            }
            
            @Override
            public int init() {
                // For animation mode, use this method
                // initialise instance variables and frame change rate
                //return int delaySketch
                return 33;
            }
            """;
    private final String defaultTextAnimation = defaultText.replaceAll("Static Sketch Mode",
                                                                    "Animation Mode");

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

        codeEditorPanel.setText(defaultText);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        modeString = new StringBuilder("Static Sketch");

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
            if(runner != null) {
                runner.stopAnimation();
            }
            runner = new SketchRunner(code, canvasPanel);
            runner.run(modeString.toString());
        });
        fileMenu.add(runItem);

        JMenuItem stopItem = new JMenuItem("Stop Code");
        stopItem.addActionListener(e -> {
            runner.stopAnimation();
        });
        fileMenu.add(stopItem);

        JMenuItem restartItem = new JMenuItem("Restart Code");
        restartItem.addActionListener(e -> {
            runner.restartAnimation();
        });
        fileMenu.add(restartItem);

        // Transform Menu
        JMenu transformMenu = new JMenu("Transform");
        transformMenu.add(new JMenuItem("Rotate"));
        transformMenu.add(new JMenuItem("Scale"));
        transformMenu.add(new JMenuItem("Mirror"));

        // Mode Menu
        JMenu modeMenu = new JMenu("Mode");
        JMenuItem staticSketches = new JMenuItem("Static Sketch");
        staticSketches.addActionListener(e -> {
            changeMode("Static Sketch");
        });
        modeMenu.add(staticSketches);
        JMenuItem animations = new JMenuItem("Animation");
        animations.addActionListener(e -> {
            changeMode("Animation");
        });
        modeMenu.add(animations);

        // Audio Menu
        JMenu audioMenu = new JMenu("Audio");
        audioMenu.add(new JMenuItem("Convert to Audio"));

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(transformMenu);
        menuBar.add(audioMenu);
        menuBar.add(modeMenu);

        return menuBar;
    }

    private void changeMode(String toMode) {
        if(!modeString.toString().equals(toMode)) {
            String existingCode = codeEditorPanel.getCode();
            String panelText = toMode.equals("Static Sketch") ?
                    existingCode.replace("Animation", "Static Sketch") :
                    existingCode.replace("Static Sketch", "Animation");
            codeEditorPanel.setText(panelText);
            modeString = new StringBuilder(toMode);
        }
    }
}
