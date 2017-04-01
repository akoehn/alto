/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.gui;

import com.bric.window.WindowList;
import com.bric.window.WindowMenu;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.TemplateInterpretedTreeAutomaton;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.InputCodec;
import de.up.ling.irtg.corpus.ChartAttacher;
import de.up.ling.irtg.corpus.Charts;
import de.up.ling.irtg.corpus.Corpus;
import de.up.ling.irtg.corpus.FileInputStreamSupplier;
import de.up.ling.irtg.corpus.OnTheFlyCharts;
import de.up.ling.irtg.maxent.MaximumEntropyIrtg;
import de.up.ling.irtg.util.CpuTimeStopwatch;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.irtg.util.GuiUtils;
import static de.up.ling.irtg.util.GuiUtils.showError;
import de.up.ling.irtg.util.Logging;
import de.up.ling.irtg.util.ValueAndTimeConsumer;
import de.up.ling.irtg.util.ProgressBarWorker;
import de.up.ling.irtg.util.TextInputDialog;
import de.up.ling.irtg.util.Util;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;
import static de.up.ling.irtg.util.GuiUtils.showError;

/**
 *
 * @author koller
 */
public class GuiMain extends javax.swing.JFrame implements ApplicationListener {

    private static File previousDirectory;
    private static GuiMain app;
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static final String GRAMMAR_SERVER_URL = "http://localhost:5000";

    static {
        previousDirectory = new File(".");
    }

    /**
     * Creates new form GuiMain
     */
    public GuiMain() {
        initComponents();
        jMenuBar1.add(new WindowMenu(this));

        if (!GuiMain.isMac()) {
            GuiUtils.replaceMetaByCtrl(jMenuBar1);
//            miLoadIrtg.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
//            miLoadAutomaton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
//            miCloseAllWindows.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
//            miQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        }
    }

    public static GuiMain getApplication() {
        return app;
    }

    public static boolean isMac() {
        return new DefaultApplication().isMac();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spLog = new javax.swing.JScrollPane();
        log = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        miLoadIrtg = new javax.swing.JMenuItem();
        miLoadIrtgFromURL = new javax.swing.JMenuItem();
        miLoadIrtgFromWebDirectory = new javax.swing.JMenuItem();
        miLoadAutomaton = new javax.swing.JMenuItem();
        miLoadTemplateIrtg = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        miCloseAllWindows = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        miAbout = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        miQuit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        miComputeDecompositionAutomaton = new javax.swing.JMenuItem();
        miVisualizeInput = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Alto GUI");

        log.setEditable(false);
        log.setColumns(20);
        log.setRows(5);
        spLog.setViewportView(log);

        jMenu1.setText("File");

        miLoadIrtg.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        miLoadIrtg.setText("Load IRTG ...");
        miLoadIrtg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLoadIrtgActionPerformed(evt);
            }
        });
        jMenu1.add(miLoadIrtg);

        miLoadIrtgFromURL.setText("Load IRTG from URL ...");
        miLoadIrtgFromURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLoadIrtgFromURLActionPerformed(evt);
            }
        });
        jMenu1.add(miLoadIrtgFromURL);

        miLoadIrtgFromWebDirectory.setText("Load IRTG from web directory ...");
        miLoadIrtgFromWebDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLoadIrtgFromWebDirectoryActionPerformed(evt);
            }
        });
        jMenu1.add(miLoadIrtgFromWebDirectory);

        miLoadAutomaton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        miLoadAutomaton.setText("Load Tree Automaton ...");
        miLoadAutomaton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLoadAutomatonActionPerformed(evt);
            }
        });
        jMenu1.add(miLoadAutomaton);

        miLoadTemplateIrtg.setText("Load Template IRTG ...");
        miLoadTemplateIrtg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLoadTemplateIrtgActionPerformed(evt);
            }
        });
        jMenu1.add(miLoadTemplateIrtg);
        jMenu1.add(jSeparator1);

        miCloseAllWindows.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        miCloseAllWindows.setText("Close All Windows");
        miCloseAllWindows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCloseAllWindowsActionPerformed(evt);
            }
        });
        jMenu1.add(miCloseAllWindows);
        jMenu1.add(jSeparator2);

        miAbout.setText("About Alto ...");
        miAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAboutActionPerformed(evt);
            }
        });
        jMenu1.add(miAbout);
        jMenu1.add(jSeparator3);

        miQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        miQuit.setText("Quit");
        miQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miQuitActionPerformed(evt);
            }
        });
        jMenu1.add(miQuit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tools");

        miComputeDecompositionAutomaton.setText("Compute decomposition automaton ...");
        miComputeDecompositionAutomaton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miComputeDecompositionAutomatonActionPerformed(evt);
            }
        });
        jMenu2.add(miComputeDecompositionAutomaton);

        miVisualizeInput.setText("Visualize object of algebra ...");
        miVisualizeInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miVisualizeInputActionPerformed(evt);
            }
        });
        jMenu2.add(miVisualizeInput);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(spLog, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(spLog, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void miLoadIrtgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miLoadIrtgActionPerformed
        loadIrtg(this);
    }//GEN-LAST:event_miLoadIrtgActionPerformed

    private void miLoadAutomatonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miLoadAutomatonActionPerformed
        loadAutomaton(this);
    }//GEN-LAST:event_miLoadAutomatonActionPerformed

    private void miQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miQuitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_miQuitActionPerformed

    private void miCloseAllWindowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miCloseAllWindowsActionPerformed
        closeAllWindows();
    }//GEN-LAST:event_miCloseAllWindowsActionPerformed

    private void miComputeDecompositionAutomatonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miComputeDecompositionAutomatonActionPerformed
        showDecompositionDialog(this);
    }//GEN-LAST:event_miComputeDecompositionAutomatonActionPerformed

    private void miLoadTemplateIrtgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miLoadTemplateIrtgActionPerformed
        loadTemplateIrtg(this);
    }//GEN-LAST:event_miLoadTemplateIrtgActionPerformed

    private void miAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAboutActionPerformed
        handleAbout(null);
    }//GEN-LAST:event_miAboutActionPerformed

    private void miLoadIrtgFromURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miLoadIrtgFromURLActionPerformed
        loadIrtgFromURL(this);
    }//GEN-LAST:event_miLoadIrtgFromURLActionPerformed

    private void miLoadIrtgFromWebDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miLoadIrtgFromWebDirectoryActionPerformed
        try {
            JGrammarFromWebSelector.withSelectedURL(new URL(getGrammarServer() + "/rest/grammars"), this, true, url -> {
                withLoadedObjectFromURL(url.toString(), InterpretedTreeAutomaton.class, "IRTG", this, (result, time) -> {
                    loadIrtg(result, time);
                });
            });
        } catch (Exception ex) {
            showError(ex);
        }
    }//GEN-LAST:event_miLoadIrtgFromWebDirectoryActionPerformed

    private void miVisualizeInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miVisualizeInputActionPerformed
        showVisualizationDialog(this);
    }//GEN-LAST:event_miVisualizeInputActionPerformed

    public static void showDecompositionDialog(java.awt.Frame parent) {
        new DecompositionDialog(parent, true).setVisible(true);
    }
    
    public static void showVisualizationDialog(java.awt.Frame parent) {
        new VisualizeDialog(parent, true).setVisible(true);
    }

    public static void quit() {
        System.exit(0);
    }

    public static void closeAllWindows() {
        for (Window window : WindowList.getWindows(false, false)) {
            if (!(window instanceof GuiMain)) {
                window.setVisible(false);
            }
        }
    }

    /**
     * Allows the user to select a file name for saving a file. If the selected
     * file already exists, the user is queried for whether the file should be
     * overwritten. The method returns a File object for the selected file if
     * the file does not exist, or if it exists and is to be overwritten.
     * Otherwise it returns null.
     *
     * @param filter
     * @param parent
     * @return
     */
    public static File chooseFileForSaving(FileFilter filter, Component parent) {
        JFileChooser fc = new JFileChooser(previousDirectory);
        fc.setFileFilter(filter);

        int returnVal = fc.showSaveDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();

            if (selected.exists()) {
                int response = JOptionPane.showConfirmDialog(parent, "The file " + selected.getName() + " already exists. Do you want to replace the existing file?",
                        "Overwrite file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (response != JOptionPane.YES_OPTION) {
                    log("Canceled writing to file " + selected.getName());
                    return null;
                }
            }

            previousDirectory = selected.getParentFile();
            return selected;
        } else {
            return null;
        }
    }

    private static boolean saveSomething(Object toSave, String objectDescription, FileFilter filter, BiFunction<File, Exception, String> errorMsgCreator, Component parent) {
        File file = chooseFileForSaving(filter, parent);

        try {
            if (file != null) {
                long start = System.nanoTime();

                PrintWriter w = new PrintWriter(new FileWriter(file));
                w.println(toSave.toString());
                w.close();

                log("Wrote " + objectDescription + " to " + file.getName() + ", " + Util.formatTimeSince(start));

                return true;
            }
        } catch (Exception e) {
            showError(parent, errorMsgCreator.apply(file, e));
        }

        return false;
    }

    public static boolean saveAutomaton(TreeAutomaton auto, Component parent) {
        return saveSomething(auto, "automaton",
                new FileNameExtensionFilter("Tree automata (*.auto)", "auto"),
                (file, exc) -> "An error occurred while attempting to save automaton as" + file.getName(),
                parent);
    }

    public static boolean saveIrtg(InterpretedTreeAutomaton irtg, Component parent) {
        return saveSomething(irtg, "IRTG",
                new FileNameExtensionFilter("Interpreted regular tree grammars (*.irtg)", "irtg"),
                (file, exc) -> "An error occurred while attempting to save IRTG as" + file.getName(),
                parent);
    }

    public static Corpus loadAnnotatedCorpus(InterpretedTreeAutomaton irtg, Component parent) {
        File file = chooseFile("Open annotated corpus", new FileNameExtensionFilter("Annotated corpora (*.txt)", "txt"), parent);

        try {
            if (file != null) {
                long start = System.nanoTime();
                Corpus corpus = irtg.readCorpus(new FileReader(file));
                log("Read annotated corpus from " + file.getName() + ", " + Util.formatTimeSince(start));

                if (!corpus.isAnnotated()) {
                    showError(parent, "The file " + file.getName() + " is not an annotated corpus.");
                    return null;
                }

                return corpus;
            }
        } catch (Exception e) {
            showError(new Exception("An error occurred while reading the corpus " + file.getName(), e));
        }

        return null;
    }

    public static void loadMaxentWeights(final MaximumEntropyIrtg irtg, final JFrame parent) {
        final File file = chooseFile("Open maxent weights", new FileNameExtensionFilter("Maxent weights (*.txt)", "txt"), parent);

        try {
            if (file != null) {
                long start = System.nanoTime();
                irtg.readWeights(new FileReader(file));

                log("Read maximum entropy weights from " + file.getName() + ", " + Util.formatTimeSince(start));
            }
        } catch (Exception e) {
            showError(new Exception("An error occurred while reading the maxent weights file " + file.getName(), e));
        }
    }

    public static void withLoadedUnannotatedCorpus(final InterpretedTreeAutomaton irtg, final JFrame parent, final Consumer<Corpus> andThen) {
        final File file = chooseFile("Open unannotated corpus", new FileNameExtensionFilter("Unannotated corpora (*.txt)", "txt"), parent);

        if (file != null) {
            try {
                long start = System.nanoTime();
                final Corpus corpus = irtg.readCorpus(new FileReader(file));
                corpus.setSource(file.getAbsolutePath());
                log("Read unannotated corpus from " + file.getName() + ", " + Util.formatTimeSince(start));

                // here we removed the option of loading a chart
                File chartsFile = null;  //chooseFile("Open precomputed parse charts (or cancel)", new FileNameExtensionFilter("Parse charts (*.zip)", "zip"), parent);

                //currently the we will always use the else clause
                if (chartsFile != null) {
                    ChartAttacher charts = new Charts(new FileInputStreamSupplier(chartsFile));
                    corpus.attachCharts(charts);
                    andThen.accept(corpus);
                } else {
                    corpus.attachCharts(new OnTheFlyCharts(irtg));
                    andThen.accept(corpus);
                }
            } catch (Exception e) {
                showError(new Exception("An error occurred while reading the corpus " + file.getName(), e));
            }
        }
    }

    private static File chooseFile(String title, FileFilter filter, Component parent) {
        List<FileFilter> f = new ArrayList<>();
        f.add(filter);
        return chooseFile(title, f, parent);
    }

    private static File chooseFile(String title, List<FileFilter> filters, Component parent) {
        JFileChooser fc = new JFileChooser(previousDirectory);
        fc.setDialogTitle(title);

        for (FileFilter f : filters) {
            fc.addChoosableFileFilter(f);
        }

        int returnVal = fc.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            previousDirectory = selected.getParentFile();
            return selected;
        } else {
            return null;
        }
    }

    private static class InputCodecFileFilter extends FileFilter {

        private InputCodec ic;

        public InputCodecFileFilter(InputCodec ic) {
            this.ic = ic;
        }

        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(ic.getMetadata().extension()) || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return ic.getMetadata().description() + " (*." + ic.getMetadata().extension() + ")";
        }

    }

    private static class LoadingResult<T> {

        T object;
        String filename;

        public LoadingResult(T object, String filename) {
            this.object = object;
            this.filename = filename;
        }
    }

    private static <T> void withLoadedObjectFromURL(Class<T> objectClass, String objectDescription, Frame parent, ValueAndTimeConsumer<LoadingResult<T>> andThen) {
        JOneStringInputForm.withString("Open " + objectDescription + " from URL", "Enter URL for the " + objectDescription + ":", parent, true, urlString -> {
            withLoadedObjectFromURL(urlString, objectClass, objectDescription, parent, andThen);
        });
    }

    private static <T> void withLoadedObjectFromURL(String urlString, Class<T> objectClass, String objectDescription, Frame parent, ValueAndTimeConsumer<LoadingResult<T>> andThen) {
        try {
            String ext = Util.getFilenameExtension(urlString);
            InputCodec<T> codec = InputCodec.getInputCodecByExtension(ext);

            if (codec == null) {
                showError(parent, "Could not identify input codec for file extension '" + ext + "'");
            } else if (codec.getMetadata().type() != objectClass) {
                showError(parent, "The codec '" + codec.getMetadata().name() + "' is not suitable for reading a " + objectDescription + ".");
            } else {
                String description = "Reading " + codec.getMetadata().description() + " ...";
                ProgressBarWorker<LoadingResult<T>> worker = listener -> {
                    codec.setProgressListener(listener);
                    InputStream r = new URL(urlString).openStream();
                    T result = codec.read(r);

                    if (result == null) {
                        throw new Exception("Error while reading from URL " + urlString);
                    }

                    return new LoadingResult<T>(result, urlString);
                };

                GuiUtils.withProgressBar(parent, "Grammar reading progress", description, worker, andThen);
            }
        } catch (Exception e) {
            showError(new Exception("An error occurred while attempting to load or parse the URL " + urlString, e));
        }
    }

    private static <T> void withLoadedObjectFromFileChooser(Class<T> objectClass, String objectDescription, Frame parent, ValueAndTimeConsumer<LoadingResult<T>> andThen) {
        List<FileFilter> filters = new ArrayList<>();

        for (InputCodec ic : InputCodec.getInputCodecs(objectClass)) {
            filters.add(new InputCodecFileFilter(ic));
        }

        final File file = chooseFile("Open " + objectDescription, filters, parent);

        try {
            if (file != null) {
                String ext = Util.getFilenameExtension(file.getName());
                final InputCodec<T> codec = InputCodec.getInputCodecByExtension(ext);

                if (codec == null) {
                    showError(parent, "Could not identify input codec for file extension '" + ext + "'");
                } else if (codec.getMetadata().type() != objectClass) {
                    showError(parent, "The codec '" + codec.getMetadata().name() + "' is not suitable for reading a " + objectDescription + ".");
                } else {
                    String description = "Reading " + codec.getMetadata().description() + " ...";
                    ProgressBarWorker<LoadingResult<T>> worker = listener -> {
                        codec.setProgressListener(listener);
                        T result = codec.read(new FileInputStream(file));

                        if (result == null) {
                            throw new Exception("Error while reading from file " + file.getName());
                        }

                        return new LoadingResult<>(result, file.getName());
                    };

                    GuiUtils.withProgressBar(parent, "Grammar reading progress", description, worker, andThen);
                }
            }
        } catch (Exception e) {
            showError(new Exception("An error occurred while attempting to parse " + file.getName(), e));
        }
    }

    public static void loadTemplateIrtg(Frame parent) {
        withLoadedObjectFromFileChooser(TemplateInterpretedTreeAutomaton.class, "Template IRTG", parent, (result, time) -> {
            log("Loaded Template IRTG from " + result.filename + ", " + Util.formatTime(time));

            TemplateInterpretedTreeAutomaton tirtg = result.object;

            String modelStr = TextInputDialog.display("Enter model", "Please enter the model over which the Template IRTG should be instantiated:", app);
            if (modelStr != null) {
                CpuTimeStopwatch sw = new CpuTimeStopwatch();
                sw.record(0);
                try {
                    InterpretedTreeAutomaton irtg = tirtg.instantiate(FirstOrderModel.read(new StringReader(modelStr)));
                    sw.record(1);
                    log("Instantiated Template IRTG in " + Util.formatTime(sw.getTimeBefore(1)));

                    JTreeAutomaton jta = new JTreeAutomaton(irtg.getAutomaton(), new IrtgTreeAutomatonAnnotator(irtg));
                    jta.setTitle("Instance of template IRTG from " + result.filename);
                    jta.setIrtg(irtg);
                    jta.setParsingEnabled(true);
                    jta.pack();
                    jta.setVisible(true);
                } catch (Exception ex) {
                    showError(new Exception("An error occurred while instantiating the Template IRTG", ex));
                }
            }
        });
    }

    public static void loadIrtg(Frame parent) {
        withLoadedObjectFromFileChooser(InterpretedTreeAutomaton.class, "IRTG", parent, (result, time) -> {
            loadIrtg(result, time);
        });
    }

    public static void loadIrtgFromURL(Frame parent) {
        withLoadedObjectFromURL(InterpretedTreeAutomaton.class, "IRTG", parent, (result, time) -> {
            loadIrtg(result, time);
        });
    }

    private static void loadIrtg(LoadingResult<InterpretedTreeAutomaton> result, long time) {
        log("Loaded IRTG from " + result.filename + ", " + Util.formatTime(time));

        InterpretedTreeAutomaton irtg = result.object;

        JTreeAutomaton jta = new JTreeAutomaton(irtg.getAutomaton(), new IrtgTreeAutomatonAnnotator(irtg));
        jta.setTitle("IRTG grammar: " + result.filename);
        jta.setIrtg(irtg);
        jta.setParsingEnabled(true);
        jta.pack();
        jta.setVisible(true);
    }

    public static void loadAutomaton(Frame parent) {
        withLoadedObjectFromFileChooser(TreeAutomaton.class, "tree automaton", parent, (result, time) -> {
            log("Loaded tree automaton from " + result.filename + ", " + Util.formatTime(time));
            TreeAutomaton auto = result.object;

            JTreeAutomaton jta = new JTreeAutomaton(auto, null);
            jta.setTitle("Tree automaton: " + result.filename);
            jta.pack();
            jta.setVisible(true);
        });
    }

    public static void log(final String log) {
        final GuiMain x = app;

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                synchronized (x) {
                    String oldLog = x.log.getText();
                    if (!oldLog.endsWith("\n")) {
                        oldLog = oldLog + "\n";
                    }

                    x.log.setText(oldLog + log);
                    Document d = x.log.getDocument();
                    x.log.select(d.getLength(), d.getLength());
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        // enable Mac integration
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Alto");

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // set uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            GuiUtils.showError(exception);
        });

        // tooltips stay visible forever
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        final GuiMain guiMain = new GuiMain();
        GuiMain.app = guiMain;

        Application application = new DefaultApplication();
        application.addApplicationListener(guiMain);
        application.addPreferencesMenuItem();
//        application.removeAboutMenuItem();
        application.setEnabledAboutMenu(true);
        application.setEnabledPreferencesMenu(false);

        // set up logging
        Logging.setUp();
        Logging.get().setLevel(Level.INFO);

        Logging.setHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                String str = getFormatter().format(record);

                if (record.getLevel() == Level.WARNING) {
                    str = "WARNING: " + str;
                } else if (record.getLevel() == Level.SEVERE) {
                    str = "SEVERE: " + str;
                }

                guiMain.log(str);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                guiMain.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JTextArea log;
    private javax.swing.JMenuItem miAbout;
    private javax.swing.JMenuItem miCloseAllWindows;
    private javax.swing.JMenuItem miComputeDecompositionAutomaton;
    private javax.swing.JMenuItem miLoadAutomaton;
    private javax.swing.JMenuItem miLoadIrtg;
    private javax.swing.JMenuItem miLoadIrtgFromURL;
    private javax.swing.JMenuItem miLoadIrtgFromWebDirectory;
    private javax.swing.JMenuItem miLoadTemplateIrtg;
    private javax.swing.JMenuItem miQuit;
    private javax.swing.JMenuItem miVisualizeInput;
    private javax.swing.JScrollPane spLog;
    // End of variables declaration//GEN-END:variables

    /**
     * ** callback methods for macify ***
     */
    public void handleAbout(ApplicationEvent ae) {
        new AboutWindow(this, true).setVisible(true);

        if (ae != null) {
            ae.setHandled(true);
        }
    }

    public void handleOpenApplication(ApplicationEvent ae) {
    }

    public void handleOpenFile(ApplicationEvent ae) {
    }

    public void handlePreferences(ApplicationEvent ae) {
    }

    public void handlePrintFile(ApplicationEvent ae) {
    }

    public void handleQuit(ApplicationEvent ae) {
        System.exit(0);
    }

    public void handleReOpenApplication(ApplicationEvent ae) {
    }
    
    public static String getGrammarServer() {
        return GRAMMAR_SERVER_URL;
    }
}
