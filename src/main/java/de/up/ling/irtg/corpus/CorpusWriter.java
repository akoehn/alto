/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.corpus;

import de.up.ling.irtg.InterpretedTreeAutomaton;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author koller
 */
public class CorpusWriter implements Consumer<Instance> {

    private Writer writer;
    private boolean isAnnotated;
    private InterpretedTreeAutomaton irtg;
    private List<String> interpretationsInOrder;

    public CorpusWriter(InterpretedTreeAutomaton irtg, List<String> interpretationsInOrder, boolean isAnnotated, Writer writer) throws IOException {
        this.writer = writer;
        this.isAnnotated = isAnnotated;
        this.irtg = irtg;
        this.interpretationsInOrder = interpretationsInOrder;

        writer.write(makeHeader(irtg, interpretationsInOrder, isAnnotated) + "\n");
    }

    public void writeInstance(Instance inst) throws IOException {
        if( inst.getComment() != null ) {
            writer.write("# " + inst.getComment() + "\n");
        }
        
        for (String interp : interpretationsInOrder) {
            writer.write(inst.getInputObjects().get(interp).toString() + "\n");
        }

        if (isAnnotated) {
            writer.write(irtg.getAutomaton().getSignature().resolve(inst.getDerivationTree()) + "\n");
        }

        writer.write("\n");
    }

    private static String makeHeader(InterpretedTreeAutomaton irtg, List<String> interpretationsInOrder, boolean annotated) {
        StringBuffer buf = new StringBuffer();

        buf.append("# IRTG " + (annotated ? "" : "un") + "annotated corpus file, v" + Corpus.CORPUS_VERSION + "\n");
        buf.append("# \n");

        for (String interp : interpretationsInOrder) {
            buf.append("# interpretation " + interp + ": " + irtg.getInterpretations().get(interp).getAlgebra().getClass() + "\n");
        }

        return buf.toString();
    }

    @Override
    public void accept(Instance t) {
        try {
            writeInstance(t);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
