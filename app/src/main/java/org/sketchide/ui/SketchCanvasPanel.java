package org.sketchide.ui;

import org.sketchide.engine.Sketch;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class SketchCanvasPanel extends JPanel {

    public Sketch sketch;

    public SketchCanvasPanel() {
        setBackground(Color.DARK_GRAY);
    }

    public BufferedImage getCanvasAsPicture() {
        int width = this.getWidth();
        int height = this.getHeight();
        if (width <= 0 || height <= 0) {
            return null; // nothing to capture
        }

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        this.paint(g2d);
        g2d.dispose();
        return img;
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
