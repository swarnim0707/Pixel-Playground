package org.sketchide.engine;

import org.sketchide.ui.SketchCanvasPanel;

import javax.tools.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class SketchRunner {

    private final String code;
    private final SketchCanvasPanel canvasPanel;
    public SketchRunner(String code, SketchCanvasPanel canvasPanel) {
        this.code = code;
        this.canvasPanel = canvasPanel;
    }

    public void run() {
        try {
//           Prepare temp directory for the sketch file for compilation
            Path tempDir = Files.createTempDirectory("sketch");
//            Write source file
            String className = "UserSketch";
            Path packageDir = tempDir.resolve("dynamic");
            Files.createDirectories(packageDir);
            Path sourceFile = packageDir.resolve(className + ".java");
            Files.writeString(sourceFile, wrapSource(className, code));

            // 3) Compile
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager =
                    compiler.getStandardFileManager(diagnostics, null, null);

            Iterable<? extends JavaFileObject> compUnits =
                    fileManager.getJavaFileObjectsFromFiles(List.of(sourceFile.toFile()));

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, fileManager, diagnostics,
                    List.of("-classpath", System.getProperty("java.class.path")),
                    null, compUnits);

            boolean success = task.call();
            fileManager.close();

            if (!success) {
                StringBuilder errorMsg = new StringBuilder();
                diagnostics.getDiagnostics().forEach(d -> {
                    errorMsg.append(d.getKind()).append(": ").append(d.getMessage(null))
                            .append("\n");
                });
                canvasPanel.showError(errorMsg.toString());
                return;
            }

            // 4) Load class
            URLClassLoader loader = new URLClassLoader(
                    new URL[]{ tempDir.toUri().toURL() },
                    this.getClass().getClassLoader()
            );
            Class<?> cls = loader.loadClass("dynamic.UserSketch");
            Sketch sketch = (Sketch) cls.getDeclaredConstructor().newInstance();

            // 5) Set and repaint
            canvasPanel.setSketch(sketch);
            canvasPanel.repaint();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private String wrapSource(String className, String body) {
        return """
            package dynamic;
            import java.awt.*;
            import org.sketchide.engine.Sketch;
            public class %s implements Sketch {
                @Override
                public void draw(Graphics2D g) {
                    %s
                }
            }
            """.formatted(className, body);
    }
}