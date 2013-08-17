/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.gui;

import de.up.ling.irtg.Interpretation;
import de.up.ling.tree.Tree;
import de.up.ling.tree.TreePanel;

/**
 *
 * @author koller
 */
public class JInterpretation extends javax.swing.JPanel {
    /**
     * Creates new form JInterpretation
     */
    public JInterpretation(Tree<String> derivationTree, Interpretation interp) {
        initComponents();
        
        Tree<String> term = interp.getHomomorphism().apply(derivationTree);
        termPanel.add(new TreePanel(term));
        valuePanel.add(interp.getAlgebra().visualize(interp.getAlgebra().evaluate(term)));
        revalidate();
        repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        termPanel = new javax.swing.JPanel();
        valuePanel = new javax.swing.JPanel();

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        termPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Term"));
        termPanel.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setBottomComponent(termPanel);

        valuePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Value"));
        valuePanel.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setLeftComponent(valuePanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel termPanel;
    private javax.swing.JPanel valuePanel;
    // End of variables declaration//GEN-END:variables
}
