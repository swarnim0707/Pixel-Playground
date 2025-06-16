# SketchIDE

SketchIDE is a Java-based live-coding environment and creative coding platform that lets users write, compile, and run Java sketches on the fly. It supports static and animated sketches, off-screen rendering, image and video export, and offers a modular architecture for future extensions (undo/redo, custom compressors, scene graph, audio transformations, game templates, etc.).

## Table of Contents

* [Overview](#overview)
* [Key Features](#key-features)

    * [Implemented Features](#implemented-features)
    * [Planned Features](#planned-features)
* [Architecture](#architecture)
* [Getting Started](#getting-started)

    * [Prerequisites](#prerequisites)
    * [Build & Run](#build--run)
* [Usage](#usage)

    * [Code Editor & Sketch Interface](#code-editor--sketch-interface)
    * [Run, Stop, Restart](#run-stop-restart)
    * [Mode Selection](#mode-selection)
    * [Exporting Images & Videos](#exporting-images--videos)
    * [Key Bindings](#key-bindings)
* [Future Roadmap](#future-roadmap)
* [Contributing](#contributing)
* [License](#license)
* [Acknowledgements](#acknowledgements)

## Overview

SketchIDE is a standalone Java application built with Swing. Users write Java code to create and animate visuals, apply transformations, integrate audio elements, and prototype simple games. It features dynamic compilation, live rendering, off-screen frame capture, and export to PNG and MP4 via FFmpeg integration. The design is modular to allow adding features like undo/redo, custom compression, scene graph, audio processing, and game templates.

## Key Features

### Implemented Features

* **Live Code Editor**: Uses RSyntaxTextArea for Java syntax highlighting, code folding, and editing.
* **Dynamic Compilation & Class Loading**: Wraps user code into a `Sketch` implementation, compiles at runtime with Java Compiler API, and loads via a custom classloader.
* **Sketch Interface**: Defines `void draw(Graphics2D g)` and `int init()` methods for static and animated sketches.
* **Static & Animation Modes**: Static mode draws a single frame; animation mode uses `javax.swing.Timer` to repaint periodically based on a returned delay.
* **Canvas Rendering**: `SketchCanvasPanel` extends JPanel and overrides `paintComponent` to invoke `sketch.draw`, handling EDT correctly and showing errors when they occur.
* **PNG Export**: Captures the canvas into a `BufferedImage` and saves it as PNG via `ImageIO`, with file chooser dialogs.
* **Video (MP4) Export with Alpha Blending**: Captures frames off-screen into `BufferedImage`, clears each frame before drawing, pads to even dimensions when needed, applies background color to preserve appearance of transparency, and encodes to MP4 via FFmpeg. Progress is shown via SwingWorker and a progress dialog, with cancellation support.
* **UI Patterns**: Uses SwingWorker for background tasks, modal dialogs (JOptionPane, JFileChooser, JColorChooser), menus with radio-button items for mode selection, and planned key bindings for common actions.
* **Error Handling**: Compilation errors are shown to the user; runtime exceptions in drawing are caught or reported; FFmpeg errors are captured and displayed.

### Planned Features

1. **Undo/Redo System**

    * Snapshots of user code and canvas state, with custom or standard compression for storage.
    * Command-pattern or similar approach for applying and reverting changes.

2. **Custom Compressor for Code**

    * Implement a custom compression/decompression method (custom tokenizer + Huffman encoding and grammar-based compression using DAGs (like Re-Pair)) to embed code efficiently in metadata and for undo/redo storage.

3. **Scene Graph & Transformations**

    * A node-based scene graph for shapes and groups, with hierarchical transforms.
    * Geometry-based operations (e.g., Voronoi, Delaunay, mesh warping) and filters.

4. **Audio Transformations**

    * Integrate audio input and processing (FFT, filters) to enable audio-reactive visuals.
    * Export synchronized audio-video via FFmpeg.

5. **Game Templates with Stateful Snapshots**

    * Templates for simple games (e.g., Snake, Maze) with input handling.
    * Ability to save and revert game states, enabling replay or branching.

## Architecture

* **Project Structure**: Gradle multi-module setup (e.g., UI, engine, utilities, future modules).
* **Main Components**:

    * `Main.java`: Launches the Swing UI on the Event Dispatch Thread.
    * `SketchIDEFrame`: JFrame containing the code editor and canvas, menus, and action handlers.
    * `CodeEditorPanel`: Wraps RSyntaxTextArea for editing sketch code.
    * `SketchCanvasPanel`: Extends JPanel; overrides `paintComponent` to call `sketch.draw` and provides methods for image capture.
    * `SketchRunner`: Handles wrapping user code, dynamic compilation, class loading, instantiation, and managing the animation Timer.
    * `Sketch` interface: Requires `draw(Graphics2D)` and `init()` methods.
* **Export Pipeline**:

    * **Off-Screen Rendering**: For each frame, create or clear a `BufferedImage`, set a background (to preserve intended appearance), call `sketch.draw`, and write the result.
    * **Alpha Handling**: Use Graphics2D drawing to composite ARGB frames onto an RGB image with background color, preserving the look of transparent content.
    * **Dimension Adjustment**: Detect odd dimensions and pad or crop to even sizes for H.264 encoding.
    * **FFmpeg Integration**: Use ProcessBuilder to run FFmpeg with appropriate arguments (`-framerate`, input pattern, codec settings). Capture and show logs for any errors.
    * **Progress & Cancellation**: Use SwingWorker to perform frame capture and encoding in a background thread, updating a JProgressBar and allowing user cancellation.
* **UI Details**:

    * **Menus & Modes**: Menu items with radio-button style to indicate mode; action listeners invoke run/stop/restart functions.
    * **Dialogs**: Use JOptionPane for input and messages, JFileChooser for saving files, JColorChooser for background selection.
    * **Key Bindings**: Plan to add InputMap/ActionMap bindings or menu accelerators for shortcuts like Ctrl+R (Run) and Ctrl+S (Save).
    * **Threading**: Ensure UI updates occur on the EDT; perform long tasks off-EDT via SwingWorker.

## Getting Started

### Prerequisites

* Java Development Kit (JDK) 21 or later
* Gradle (wrapper included) or similar build tool
* FFmpeg installed and accessible via system PATH for video export
* RSyntaxTextArea dependency (managed by Gradle)

### Build & Run

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/sketchide.git
   cd sketchide
   ```
2. Build:

   ```bash
   ./gradlew build
   ```
3. Run:

   ```bash
   java -jar build/libs/sketchide.jar
   ```

   Ensure FFmpeg is available in PATH or configured appropriately.

## Usage

### Code Editor & Sketch Interface

* Write Java code implementing `draw(Graphics2D g)` and optionally `init()`.
* Use the provided template or create custom sketches.

### Run, Stop, Restart

* **Run**: Compile and execute the sketch. In static mode, renders once; in animation mode, starts a timer for repeated rendering.
* **Stop**: Stops animation.
* **Restart**: Restarts animation on the current sketch instance.

### Mode Selection

* Choose between Static Sketch and Animation mode via the Mode menu. The selected mode is indicated. Code templates may reflect the mode.

### Exporting Images & Videos

* **Save as PNG**: Capture the current canvas image and save via a file chooser.
* **Export Video**: In animation mode, specify duration and background color. Frames are rendered off-screen, cleared each frame, padded if needed, composited onto the background, and encoded into MP4 with FFmpeg. Progress dialog shows status and allows cancellation.

### Key Bindings

* To be added: configure shortcuts (e.g., Ctrl+R for run, Ctrl+S for save) using InputMap/ActionMap or menu accelerators.

## Future Roadmap

* **Undo/Redo**: Snapshot code and canvas states, custom or standard compression for history.
* **Custom Compressor**: Implement compression/decompression for embedding code in metadata and storing history.
* **Scene Graph & Transformations**: Node-based structure, hierarchical transforms, geometry algorithms.
* **Audio Processing**: FFT-based visualizations, audio-reactive sketches, synchronized export.
* **Game Templates**: Examples with state saving/reverting, replay, branching.
* **Editor Enhancements**: Autocomplete, error highlighting, dynamic classpath.
* **Packaging & Distribution**: Cross-platform distribution, bundling or configuring FFmpeg, user preferences, plugin support.

## Contributing

Contributions welcome. Fork the repo, create a branch, implement features or fixes, and open a pull request. Please follow the existing code style and include documentation or tests as appropriate.

## License

GNU Affero General Public License v3.0

## Acknowledgements

* RSyntaxTextArea for code editing features.
* FFmpeg for video encoding.
* Inspiration from creative coding environments such as Processing.
* Community resources for guidance on Swing, off-screen rendering, and FFmpeg integration.
