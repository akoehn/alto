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
import de.up.ling.irtg.corpus.Charts;
import de.up.ling.irtg.corpus.Corpus;
import de.up.ling.irtg.corpus.FileInputStreamSupplier;
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
import static de.up.ling.irtg.util.Util.stripExtension;
import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;

/**
 *
 * @author koller
 */
public class GuiMain extends javax.swing.JFrame implements ApplicationListener {

    private static File previousDirectory;
    private static GuiMain app;
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);

    static {
        previousDirectory = new File(".");
    }

    /**
     * Creates new form GuiMain
     */
    public GuiMain() {
        initComponents();
        jMenuBar1.add(new WindowMenu(this));
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
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IRTG GUI");

        log.setEditable(false);
        log.setColumns(20);
        log.setRows(5);
        spLog.setViewportView(log);

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        jMenuItem1.setText("Load IRTG ...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        jMenuItem2.setText("Load Tree Automaton ...");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem6.setText("Load Template IRTG ...");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem6);
        jMenu1.add(jSeparator1);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        jMenuItem4.setText("Close All Windows");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);
        jMenu1.add(jSeparator2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        jMenuItem3.setText("Quit");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tools");

        jMenuItem5.setText("Compute decomposition automaton ...");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

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

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        loadIrtg(this);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        loadAutomaton(this);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        closeAllWindows();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        showDecompositionDialog(this);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        loadTemplateIrtg(this);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    public static void showDecompositionDialog(java.awt.Frame parent) {
        new DecompositionDialog(parent, true).setVisible(true);
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

    public static File chooseFileForSaving(FileFilter filter, Component parent) {
        JFileChooser fc = new JFileChooser(previousDirectory);
        fc.setFileFilter(filter);

        int returnVal = fc.showSaveDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            previousDirectory = selected.getParentFile();
            return selected;
        } else {
            return null;
        }
    }

    public static boolean saveAutomaton(TreeAutomaton auto, Component parent) {
        File file = chooseFileForSaving(new FileNameExtensionFilter("Tree automata", "auto"), parent);

        try {
            if (file != null) {
                PrintWriter w = new PrintWriter(new FileWriter(file));
                w.println(auto.toString());
                w.close();
                return true;
            }
        } catch (Exception e) {
            showError(parent, "An error occurred while attempting to save automaton as" + file.getName() + ": " + e.getMessage());
        }

        return false;
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
            showError(parent, "An error occurred while reading the corpus " + file.getName() + ": " + e.getMessage());
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
            showError(parent, "An error occurred while reading the maxent weights file " + file.getName() + ": " + e.getMessage());
        }
    }

    public static void withLoadedUnannotatedCorpus(final InterpretedTreeAutomaton irtg, final JFrame parent, final Consumer<Corpus> andThen) {
        final File file = chooseFile("Open unannotated corpus", new FileNameExtensionFilter("Unannotated corpora (*.txt)", "txt"), parent);

        if (file != null) {
            try {
                long start = System.nanoTime();
                final Corpus corpus = irtg.readCorpus(new FileReader(file));
                log("Read unannotated corpus from " + file.getName() + ", " + Util.formatTimeSince(start));

                File chartsFile = chooseFile("Open precomputed parse charts (or cancel)", new FileNameExtensionFilter("Parse charts (*.zip)", "zip"), parent);

                if (chartsFile != null) {
                    Charts charts = new Charts(new FileInputStreamSupplier(chartsFile));
                    corpus.attachCharts(charts);
                    andThen.accept(corpus);
                } else {
                    GuiUtils.withProgressBar(parent, "Chart computation", "Computing charts ...",
                            listener -> {
                                File f = new File(file.getParent(), stripExtension(file.getName()) + "-charts.zip");
                                OutputStream fos = new FileOutputStream(f);
                                Charts.computeCharts(corpus, irtg, fos, listener);
                                return f;
                            },
                            (f, time) -> {
                                log("Wrote parse charts to " + f + ", " + Util.formatTime(time));
                                Charts charts = new Charts(new FileInputStreamSupplier(f));
                                corpus.attachCharts(charts);
                                andThen.accept(corpus);
                            });
                }
            } catch (Exception e) {
                showError(parent, "An error occurred while reading the corpus " + file.getName() + ": " + e.getMessage());
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
        File filename;

        public LoadingResult(T object, File filename) {
            this.object = object;
            this.filename = filename;
        }
    }

    private static <T> void withLoadedObject(Class<T> objectClass, String objectDescription, Component parent, ValueAndTimeConsumer<LoadingResult<T>> andThen) {
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
                    showError(parent, "The codec '" + codec.getMetadata().name() + "' is not suitable for reading a" + objectDescription + ".");
                } else {
                    String title = "Reading " + codec.getMetadata().description() + " ...";
                    ProgressBarWorker<LoadingResult<T>> worker = listener -> {
                        codec.setProgressListener(listener);
                        T result = codec.read(new FileInputStream(file));

                        if (result == null) {
                            throw new Exception("Error while reading from file " + file.getName());
                        }

                        return new LoadingResult<>(result, file);
                    };

                    GuiUtils.withProgressBar(app, title, title, worker, andThen);
                }
            }
        } catch (Exception e) {
            showError(parent, "An error occurred while attempting to parse " + file.getName() + ": " + e.getMessage());
        }
    }

    public static void loadTemplateIrtg(Component parent) {
        withLoadedObject(TemplateInterpretedTreeAutomaton.class, "Template IRTG", parent, (result, time) -> {
            log("Loaded Template IRTG from " + result.filename.getName() + ", " + Util.formatTime(time));

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
                    jta.setTitle("Instance of template IRTG from " + result.filename.getName());
                    jta.setIrtg(irtg);
                    jta.setParsingEnabled(true);
                    jta.pack();
                    jta.setVisible(true);
                } catch (Exception ex) {
                    showError(parent, "An error occurred while instantiating the Template IRTG: " + ex.getMessage());
                }
            }
        });
    }

    public static void loadIrtg(Component parent) {
        withLoadedObject(InterpretedTreeAutomaton.class, "IRTG", parent, (result, time) -> {
            log("Loaded IRTG from " + result.filename.getName() + ", " + Util.formatTime(time));

            InterpretedTreeAutomaton irtg = result.object;

            JTreeAutomaton jta = new JTreeAutomaton(irtg.getAutomaton(), new IrtgTreeAutomatonAnnotator(irtg));
            jta.setTitle("IRTG grammar: " + result.filename.getName());
            jta.setIrtg(irtg);
            jta.setParsingEnabled(true);
            jta.pack();
            jta.setVisible(true);
        });
    }

    public static void loadAutomaton(Component parent) {
        withLoadedObject(TreeAutomaton.class, "tree automaton", parent, (result, time) -> {
            log("Loaded tree automaton from " + result.filename.getName() + ", " + Util.formatTime(time));
            TreeAutomaton auto = result.object;

            JTreeAutomaton jta = new JTreeAutomaton(auto, null);
            jta.setTitle("Tree automaton: " + result.filename.getName());
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
                    if( ! oldLog.endsWith("\n")) {
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
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IRTG GUI");

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
        // tooltips stay visible forever
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        final GuiMain guiMain = new GuiMain();
        GuiMain.app = guiMain;

        Application application = new DefaultApplication();
        application.addApplicationListener(guiMain);
        application.addPreferencesMenuItem();
        application.setEnabledPreferencesMenu(false);

        // set up logging
        Logging.setUp();
        Logging.get().setLevel(Level.INFO);
        
        Logging.setHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                String str = getFormatter().format(record);
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
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTextArea log;
    private javax.swing.JScrollPane spLog;
    // End of variables declaration//GEN-END:variables

    /**
     * ** callback methods for macify ***
     */
    public void handleAbout(ApplicationEvent ae) {
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
}
