/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.gui;

import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.IrtgParser;
import de.up.ling.irtg.ParseException;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.tree.Tree;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author koller
 */
public class JDerivationViewer extends javax.swing.JPanel {
    private InterpretedTreeAutomaton irtg;
    private Interpretation[] interpretationForSelection;
    private JComponent[] cachedComponents;
    private Tree<String> derivationTree;
    private static final String INTERPRETATION_PREFIX = "interpretation: ";
    private static final String DERIVATION_TREE = "derivation tree";

    public static void main(String[] args) throws ParseException, FileNotFoundException, ParserException {
        InterpretedTreeAutomaton irtg = IrtgParser.parse(new FileReader("examples/nesson-shieber.irtg"));

        JFrame f = new JFrame("test");
//        f.add(new JTreeAutomaton(irtg.getAutomaton(), new IrtgTreeAutomatonAnnotator(irtg)));
//        f.pack();
//        f.setVisible(true);

//
//         JDerivationViewer dv = new JDerivationViewer();
//         dv.setInterpretedTreeAutomaton(irtg);
//         f.add(dv);
//
//         Map<String, String> input = new HashMap<String, String>();
//         input.put("string", "john apparently likes mary");
//         TreeAutomaton chart = irtg.parse(input);
//         Tree<String> dt = chart.viterbi();
//
//         f.pack();
//         f.setVisible(true);
//
//         dv.displayDerivation(dt);


        JLanguageViewer lv = new JLanguageViewer();
        lv.setAutomaton(irtg.getAutomaton(), irtg);
        lv.pack();
        lv.setVisible(true);
    }

    /**
     * Creates new form JDerivationViewer
     */
    public JDerivationViewer() {
        initComponents();
    }

    public void setInterpretedTreeAutomaton(InterpretedTreeAutomaton irtg) {
        this.irtg = irtg;
        int N = 1;

        if (irtg != null) {
            N = irtg.getInterpretations().size() + 1;
        }

        String[] possibleViews = new String[N];
        interpretationForSelection = new Interpretation[N - 1];
        cachedComponents = new JComponent[N];

        possibleViews[0] = DERIVATION_TREE;

        if (irtg != null) {
            int i = 0;
            for (String name : irtg.getInterpretations().keySet()) {
                interpretationForSelection[i] = irtg.getInterpretations().get(name);
                possibleViews[i + 1] = INTERPRETATION_PREFIX + name;
                i++;
            }
        }

        componentSelector.setModel(new DefaultComboBoxModel(possibleViews));
        derivationTree = null;
    }

    public void displayDerivation(Tree<String> derivationTree) {
        this.derivationTree = derivationTree;

        for (int i = 0; i < cachedComponents.length; i++) {
            cachedComponents[i] = null;
        }

        redraw();
    }

    private void redraw() {
        if (derivationTree != null) {
            int index = componentSelector.getSelectedIndex();

            content.removeAll();

            if (cachedComponents[index] == null) {
                if (index == 0) {
                    cachedComponents[index] = new JDerivationTree(derivationTree);
                } else {
                    cachedComponents[index] = new JInterpretation(derivationTree, interpretationForSelection[index - 1]);
                }
            }

            content.add(cachedComponents[index]);
            revalidate();
            repaint();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        componentSelector = new javax.swing.JComboBox();
        content = new javax.swing.JPanel();

        jLabel1.setText("View:");

        componentSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                componentSelectorActionPerformed(evt);
            }
        });

        content.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(content, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(componentSelector, 0, 443, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(componentSelector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(content, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void componentSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_componentSelectorActionPerformed
        redraw();
    }//GEN-LAST:event_componentSelectorActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox componentSelector;
    private javax.swing.JPanel content;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
