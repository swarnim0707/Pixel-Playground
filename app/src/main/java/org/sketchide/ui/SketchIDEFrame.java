    package org.sketchide.ui;

    import javax.imageio.ImageIO;
    import javax.swing.*;
    import javax.swing.filechooser.FileNameExtensionFilter;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.awt.geom.AffineTransform;
    import java.awt.image.BufferedImage;
    import java.io.*;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.concurrent.atomic.AtomicBoolean;
    import java.util.List;

    import org.sketchide.engine.Sketch;
    import org.sketchide.engine.SketchRunner;

    public class SketchIDEFrame extends JFrame {

        private final CodeEditorPanel codeEditorPanel;
        private final SketchCanvasPanel canvasPanel;
        private SketchRunner runner;
        private StringBuilder modeString;

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

            String defaultText = """
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
            saveItem.addActionListener(e -> {
                if(runner != null) {
                    if(runner.animationTimer == null) {
                        this.saveAsPNG();
                    }
                    else {
                        this.saveAsVideo();
                    }
                }
            });
            fileMenu.add(saveItem);
            fileMenu.addSeparator();

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

        public void saveAsPNG() {
            BufferedImage image = canvasPanel.getCanvasAsPicture();

            if(image == null) {
                JOptionPane.showMessageDialog(this, "Canvas is not ready for capture.",
                        "Save Image",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Sketch as Image");
            fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));
            String defaultName = "sketch_" + System.currentTimeMillis() + ".png";
            fileChooser.setSelectedFile(new File(defaultName));
            int userChoice = fileChooser.showSaveDialog(this);
            if (userChoice != JFileChooser.APPROVE_OPTION) {
                return; // user canceled
            }

            File chosenFile = fileChooser.getSelectedFile();
            String path = chosenFile.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".png")) {
                chosenFile = new File(path + ".png");
            }

            if (chosenFile.exists()) {
                int resp = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (resp != JOptionPane.YES_OPTION) {
                    return; // do not overwrite
                }
            }

            try {
                boolean ok = ImageIO.write(image, "png", chosenFile);
                if (!ok) {
                    // In rare cases, writer not found
                    JOptionPane.showMessageDialog(this,
                            "No suitable PNG writer found.",
                            "Save Image",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Image saved: " + chosenFile.getAbsolutePath(),
                            "Save Image",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Failed to save image:\n" + ex.getMessage(),
                        "Save Image",
                        JOptionPane.ERROR_MESSAGE);
            }

        }

        public void saveAsVideo() {
            if(runner == null || runner.animationTimer == null) {
                JOptionPane.showMessageDialog(this,
                        "No animation to export. Please run an animation first.",
                        "Export Video Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Prompt for duration of video
            String duration = JOptionPane.showInputDialog(this,
                    "Enter video duration in seconds",
                    "Export Video Duration",
                    JOptionPane.PLAIN_MESSAGE);

            if(duration == null || duration.isEmpty() || duration.trim().startsWith("-")) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid duration",
                        "Export Video Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int durationSec = Integer.parseInt(duration);

            // Choose background colour

            Color bgColor = JColorChooser.showDialog(
                    this,
                    "Choose background color for video frames",
                    Color.BLACK // default
            );
            if (bgColor == null) {
                // User cancelled color chooser; default to black
                bgColor = Color.BLACK;
            }

            // Calculating number of frames

            int delay = runner.animationTimer.getDelay();
            double fps = 1000.0/delay;
            int totalFrames = (int) Math.max(1.0, fps*durationSec);

            int w = canvasPanel.getWidth();
            int h = canvasPanel.getHeight();

            w = (w % 2 == 0) ? w : w + 1;
            h = (h % 2 == 0) ? h : h + 1;

            while(true) {
                // Get resolution from the user
                String currentRes = JOptionPane.showInputDialog(this,
                        "Export in the current resolution? [y/n]",
                        "Export Video Duration",
                        JOptionPane.PLAIN_MESSAGE);
                currentRes = currentRes.trim().toLowerCase();
                if(currentRes.isEmpty()) return;
                if(!currentRes.equals("y") && !currentRes.equals("n")) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid option [y or n]",
                            "Export Video Error",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                if(currentRes.equals("y")) break;

//                String newRes = JOptionPane.show(this,
//                        "Export in the current resolution? [y/n]",
//                        "Export Video Duration",
//                        JOptionPane.PLAIN_MESSAGE);
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Animation as MP4");
            fileChooser.setFileFilter(new FileNameExtensionFilter("MP4 Video (*.mp4)", "mp4"));
            String defaultName = "animation_" + System.currentTimeMillis() + ".mp4";
            fileChooser.setSelectedFile(new File(defaultName));
            int userChoice = fileChooser.showSaveDialog(this);
            if (userChoice != JFileChooser.APPROVE_OPTION) {
                return; // user cancelled
            }
            File chosenFile = fileChooser.getSelectedFile();
            String outPath = chosenFile.getAbsolutePath();
            if (!outPath.toLowerCase().endsWith(".mp4")) {
                chosenFile = new File(outPath + ".mp4");
            }
            if (chosenFile.exists()) {
                int resp = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (resp != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Creating background BufferImage to draw frames for the export
            BufferedImage bg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bg.createGraphics();
            g.setClip(0, 0, w, h);

            // Prepare a progress dialog
            JDialog progressDialog = new JDialog(this, "Exporting Video...", false);
            JProgressBar progressBar = new JProgressBar(0, totalFrames);
            progressBar.setStringPainted(true);
            JButton cancelButton = new JButton("Cancel");
            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(progressBar);
            panel.add(Box.createVerticalStrut(10));
            panel.add(cancelButton);
            progressDialog.getContentPane().add(panel);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(this);

            // The frames for the animation will be saved here
            Path tempDir;
            try {
                tempDir = Files.createTempDirectory("Animation_Frames");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // The export will occur in a background thread and the cancel button will be on EDT
            // So it will have to be thread-safe
            AtomicBoolean cancelled;
            cancelled = new AtomicBoolean(false);
            cancelButton.addActionListener(e -> cancelled.set(true));

            Color finalBgColor = bgColor;
            File finalChosenFile = chosenFile;

            int finalW = w;
            int finalH = h;
            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        AffineTransform origTransform = g.getTransform();
                        Composite origComposite = g.getComposite();
                        Stroke origStroke = g.getStroke();
                        Paint origPaint = g.getPaint();
                        Sketch sketch = (Sketch) runner.cls.getDeclaredConstructor().newInstance();
                        for(int i = 0; i<totalFrames; i++) {
                            if(cancelled.get()) return null;

                            g.setComposite(AlphaComposite.Src);  // ensures clearing works correctly
                            g.setColor(new Color(0,0,0,0));  // transparent fill
                            g.fillRect(0, 0, finalW, finalH);

                            g.setTransform(origTransform);
                            g.setComposite(origComposite);
                            g.setStroke(origStroke);
                            g.setPaint(origPaint);

                            sketch.draw(g); // Draws using the sketch object copy to the background graphics

                            BufferedImage img = new BufferedImage(finalW, finalH, BufferedImage.TYPE_INT_RGB);
                            Graphics2D g2 = img.createGraphics();

                            g2.setColor(finalBgColor);
                            g2.fillRect(0, 0, finalW, finalH);
                            g2.drawImage(bg, 0, 0, null);
                            g2.dispose();

                            String filename = String.format("frame_%06d.png", i);
                            File outFile = tempDir.resolve(filename).toFile();
                            try {
                                boolean ok = ImageIO.write(img, "png", outFile);
                                if (!ok) {
                                    throw new IOException("No suitable PNG writer found for frame " + i);
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                throw new RuntimeException("Failed writing frame " + i + ": " + ex.getMessage(), ex);
                            }

                            // 8.1.4 Update progress
                            publish(i + 1);
                        }

                        if (cancelled.get()) {
                            return null;
                        }

                        // 8.2 After capturing frames, build ffmpeg command
                        /*
                         ffmpeg -y -framerate {fps} -i frame_%06d.png -c:v libx264 -pix_fmt yuv420p output.mp4
                         */
                        ProcessBuilder pb = getProcessBuilder(fps, tempDir, finalChosenFile);
                        Process proc = pb.start();

                        StringBuilder ffmpegOutput = new StringBuilder();
                        String line;

                        // Read ffmpeg output (optional, can log or ignore)
                        try (InputStream is = pb.redirectErrorStream() ?
                                proc.getInputStream() : proc.getErrorStream();
                             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        ) {
                            while ((line = reader.readLine()) != null) {
                                ffmpegOutput.append(line).append("\n");
                                if (cancelled.get()) {
                                    proc.destroy();
                                    break;
                                }
                            }
                        }
                        int exitCode = proc.waitFor();
                        if (exitCode != 0) {
                            System.out.println(ffmpegOutput);
                            throw new RuntimeException("ffmpeg exited with code " + exitCode);
                        }
                    } finally {
                        // 8.3 Clean up temp frames directory if not cancelled
                        // If cancelled, also delete partial frames
                        g.dispose();
                        try {
                            deleteDirectoryRecursively(tempDir.toFile());
                        } catch (Exception e) {
                            // ignore cleanup errors
                            e.printStackTrace();
                        }
                    }

                    return null;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    // Update progress bar. The last value is the latest frame count.
                    int framesDone = chunks.getLast();
                    progressBar.setValue(framesDone);
                    progressBar.setString(String.format("Captured %d / %d frames", framesDone, totalFrames));
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    if (cancelled.get()) {
                        JOptionPane.showMessageDialog(SketchIDEFrame.this,
                                "Video export was cancelled.",
                                "Export Video",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        try {
                            get(); // to rethrow exceptions if any
                            JOptionPane.showMessageDialog(SketchIDEFrame.this,
                                    "Video saved: " + finalChosenFile.getAbsolutePath(),
                                    "Export Video",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(SketchIDEFrame.this,
                                    "Error during video export:\n" + ex.getCause().getMessage(),
                                    "Export Video",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        }

        private void deleteDirectoryRecursively(File dir) {
            if(dir.isDirectory()) {
                File[] files = dir.listFiles();
                if(files != null) {
                    for(File file: files) {
                        deleteDirectoryRecursively(file);
                    }
                }
            }

            boolean deleted = dir.delete();
            if(!deleted) {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete all temporary frames created during export. Please delete manually",
                        "Export Video",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        private static ProcessBuilder getProcessBuilder(double fps, Path tempDir, File chosenFile) {
            List<String> cmd = List.of(
                    "ffmpeg",
                    "-y",
                    "-framerate", String.valueOf((int)fps),
                    "-i", tempDir.resolve("frame_%06d.png").toString(),
                    "-c:v", "libx264",
                    "-pix_fmt", "yuv420p",
                    chosenFile.getAbsolutePath()
            );
            ProcessBuilder pb = new ProcessBuilder(cmd);
            // Set working dir to tempDir so ffmpeg finds files easily
            pb.directory(tempDir.toFile());
            pb.redirectErrorStream(true);
            return pb;
        }

    }
