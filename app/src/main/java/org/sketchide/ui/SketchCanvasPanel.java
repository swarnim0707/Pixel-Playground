package org.sketchide.ui;

import org.sketchide.engine.Sketch;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class SketchCanvasPanel extends JPanel {

    private Sketch sketch;

    public SketchCanvasPanel() {
        setBackground(Color.DARK_GRAY);
    }

    public void setSketch(Sketch s) {
        this.sketch = s;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(sketch == null) {
            g.setColor(Color.GRAY);
            g.drawString("No Sketch Loaded", 20, 20);
            return;
        }
        sketch.draw((Graphics2D) g);
    }

    public void showError(String errorMsg) {
        JOptionPane.showMessageDialog(this, errorMsg,
                "Compilation Error", JOptionPane.ERROR_MESSAGE);
    }
}
