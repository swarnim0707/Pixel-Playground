package org.sketchide.engine;

import java.awt.Graphics2D;

public interface Sketch {
    void draw(Graphics2D g);
    int init();
}