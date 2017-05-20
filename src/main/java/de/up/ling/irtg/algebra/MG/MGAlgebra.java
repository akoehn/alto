/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

import com.google.common.collect.Sets;
import de.up.ling.irtg.algebra.EvaluatingAlgebra;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.IsiAmrInputCodec;
import de.up.ling.irtg.codec.MGInputCodec;
import de.up.ling.irtg.codec.TikzSgraphOutputCodec;
import de.up.ling.irtg.codec.isiamr.IsiAmrParser;
import de.up.ling.irtg.laboratory.OperationAnnotation;
import de.up.ling.irtg.signature.Signature;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 * A Minimalist Grammar algebra. The values of this algebra are <i>expressions</i>,
 * i.e. objects of class {@link MG}, which are tuples of (string, feature list) pairs. The algebra interprets
 * the following operations (where expr, expr1, expr2 are expressions):
 * <ul>
 *  <li>The operation <code>merge(expr1,expr2)</code>, <i>merges</i> the
 *      two expressions using {@link Expression#merge(de.up.ling.irtg.algebra.MG.Expression) }. 
 * Merge applies when the "head" features of the two expressions match.</li>
 *  <li>The <i>move</i> operation <code>move(expr)</code> merges one of the tuples in the expression with the first tuple of the expression
 * using {@link Expression#move(de.up.ling.irtg.algebra.MG.Expression) }. Move applies when the head features of the expression matches the head feature of the "moved" tuple. </li>
 *  <li>The operation <code>adjoin(expr1,expr2)</code>, <i>adjoins</i> on expression to another with {@link Expression#adjoin(de.up.ling.irtg.algebra.MG.Expression) } 
 * when one is listed in the grammar as an adjunct of the other .</li>
 * </ul>
 * 
 * 
 * 
 * @author meaghanfowlie
 */



public class MGAlgebra extends EvaluatingAlgebra<Expression> {
    // operation symbols of this algebra
    public static final String OP_MERGE = "merge";
    public static final String OP_ADJOIN = "adjoin";
    public static final String OP_MOVE = "move";
    
    private MG g;
    
    private boolean useTopDownAutomaton = false;

    /**
     * Describes whether this algebra uses top-down or bottom-up decomposition automata.
     * @return 
     */
    public boolean usesTopDownAutomaton() {
        return useTopDownAutomaton;
    }
    
    /**
     * Sets whether this algebra uses top-down or bottom-up decomposition automata.
     * @param useTopDownAutomaton
     */
    public void setUseTopDownAutomaton(boolean useTopDownAutomaton) {
        this.useTopDownAutomaton= useTopDownAutomaton;
    }
    
    
    
    
    
    

    private Int2ObjectMap<Expression> constantLabelInterpretations;
    
    /**
     * the returned map maps all constant labels in this algebra's signature to their corresponding Expressions.
     * This returns the original set stored in the algebra, so it must not be modified.
     * @return
     */
    Int2ObjectMap<Expression> getAllConstantLabelInterpretations() {
        if (constantLabelInterpretations == null) {
            precomputeAllConstants();
        }
        return constantLabelInterpretations;
    }
    
    private Set<String> sources;
    
    
        
    /**
     * Creates an empty MG algebra.
     * This is an EvaluatingAlgebra, so it has a <code>signature</code>
     * We also give it a grammar <code>g</code>
     */
    public MGAlgebra() {
        super(); // this means EvaluatingAlgebra();
        this.g = new MG(); // empty grammar
    }
    
    /**
     * Creates an MG algebra with the given signature.
     * @param signature an algebra signature
     */
    public MGAlgebra(Signature signature) {
        super();
        this.signature = signature;
        this.g = new MG(); // empty grammar
    }
    
    /**
     * Creates an MG algebra with the given signature and grammar
     * @param signature
     * @param g a minimalist grammar
     */
    public MGAlgebra(Signature signature, MG g) {
        super();
        this.signature = signature;
        this.g = g; 
    }
    
    /**
     * Creates an MG algebra with the given grammar
     * @param g a minimalist grammar
     */
    public MGAlgebra(MG g) {
        super();
        this.g = g; 
    }
    
    /**
     * Computes expressions for all constant symbols and stores them for future reference. 
     */
    private void precomputeAllConstants() {
        constantLabelInterpretations = new Int2ObjectOpenHashMap<>();
        for (int id = 1; id <= signature.getMaxSymbolId(); id++) {
            if (signature.getArity(id) == 0) {
                String label = signature.resolveSymbolId(id);
                try {
                    constantLabelInterpretations.put(id, parseString(label));
                } catch (ParserException ex) {
                    throw new IllegalArgumentException("Could not parse operation \"" + label + "\": " + ex.getMessage() + "when initializing constants for algebra");
                }
            }
        }
    }
        
    
//    /**
//     * Returns a bottom-up or a top-down decomposition automaton for the s-graph
//     * {@code value} (which one can be set via {@code setUseTopDownAutomaton}, 
//     * default is bottom-up).
//     * @param value
//     * @return 
//     */
//    @Override
//    public TreeAutomaton decompose(SGraph value) {
//        if (useTopDownAutomaton)
//            return decompose(value, SGraphBRDecompositionAutomatonTopDown.class);
//        else
//            return decompose(value, SGraphBRDecompositionAutomatonBottomUp.class);
//    }
//    
//    /**
//     * Given an SGraph, this returns the corresponding decomposition automaton of class c.
//     * @param value
//     * @param c
//     * @return
//     */
//    public TreeAutomaton decompose(SGraph value, Class c){
//        //try {
//            if (c == SGraphDecompositionAutomaton.class){
//                return new SGraphDecompositionAutomaton(value, this, getSignature());
//                
//            } else if (c == SGraphBRDecompositionAutomatonBottomUp.class) {
//                return new SGraphBRDecompositionAutomatonBottomUp(value, this);
//                
//            } else if (c == SGraphBRDecompositionAutomatonTopDown.class) {
//                return new SGraphBRDecompositionAutomatonTopDown(value, this);
//            }
//            else return null;
//    }
//    
//    @OperationAnnotation(code="decompTopDown")
//    public TreeAutomaton decomposeTopDown(SGraph value) {
//        return decompose(value, SGraphBRDecompositionAutomatonTopDown.class);
//    }
//    
//    /**
//     * Writes (nearly) all the rules in the decomposition automaton of the
//     * SGraph value (with respect to the signature in this algebra) into the
//     * Writer, and does not store the rules in memory.
//     * Iterates through the rules in bottom up order.
//     * To avoid cycles, there are no rename operations on states only reachable
//     * via rename. Rules of the form c-> m(a, b) and c-> m(b,a) are both written
//     * into the writer.
//     * @param value
//     * @param writer
//     * @return
//     * @throws Exception
//     */
//    public boolean writeRestrictedAutomaton(SGraph value, Writer writer) throws Exception{
//        SGraphBRDecompositionAutomatonBottomUp botupAutomaton = new SGraphBRDecompositionAutomatonBottomUp(value, this);
//        return botupAutomaton.writeAutomatonRestricted(writer);
//    }
//    
//    /**
//     * Writes (nearly) all the rules in the decomposition automaton of the
//     * SGraph value (with respect to the incomplete decomposition algebra)
//     * into the Writer, and does not store the rules in memory.
//     * Iterates through the rules in bottom up order.
//     * To avoid cycles, there are no rename operations on states only reachable
//     * via rename. Rules of the form c-> m(a, b) and c-> m(b,a) are both written
//     * into the writer.
//     * @param value
//     * @param sourceCount
//     * @param writer
//     * @return
//     * @throws Exception
//     */
//    public static boolean writeRestrictedDecompositionAutomaton(SGraph value, int sourceCount, Writer writer) throws Exception{
//        GraphAlgebra alg = makeIncompleteDecompositionAlgebra(value, sourceCount);
//        SGraphBRDecompositionAutomatonBottomUp botupAutomaton = new SGraphBRDecompositionAutomatonBottomUp(value, alg);
//        return botupAutomaton.writeAutomatonRestricted(writer);
//    }
//    
//    
//    
    /**
     * Evaluates to expression
     * Not sure I'm handling errors correctly.
     * @return 
     */
    @Override
    public Expression evaluate(String label, List<Expression> childrenValues) {

        if (label == null) {
            return null;
        } else if (label.equals(OP_MERGE)) {
            return this.g.merge(childrenValues.get(0), childrenValues.get(1));
        } else if (label.equals(OP_MOVE)) {
            return this.g.move(childrenValues.get(0));
        } else if (label.equals(OP_ADJOIN)) {
            return this.g.adjoin(childrenValues.get(0), childrenValues.get(1));

        } else { // lexical
            try {
                return parseString(label); // turn the label into an expression
            } catch (ParserException ex) {
                Logger.getLogger(MGAlgebra.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

    }
    
    /**
     * Checks whether "value" is a valid value. The decomposition automata
     * will only contain rules in which the parent and all child states
     * are valid values, as defined by this method.
     * A valid value is one where:
     * <ul>
     * <li>All pairs in the expression have feature stacks that are suffixes of feature stacks in the lexicon</li>
     * <li>The first element is not headed by a move feature</li>
     * <li>All the other elements are headed by move feature</li>
     * <li>All the movers are headed by features whose number correspond to the mover's index </li>
     * </ul>
     * @param value
     * @return 
     */
    @Override
    protected boolean isValidValue(Expression value) {
        boolean valid = true;

        if (value.headFeature().isMove()) {
            valid = false; // expression must not be headed by a negative licesning feature
        }
        if (valid) { // look at head feature stack
            boolean suf = false;
            Lex head = value.head();
            if (!head.isValid(g)) {
                suf = false;

            } else {
                for (Lex li : g.getLexicon()) {
                    if (li.getFeatures().suffix(head.getFeatures())) {
                        suf = true;
                        break; // if we find one LI this is a suffix of, we're okay
                    }
                }
            }
            if (!suf) { // only continue if head was valid
                valid = false;

            } else { // now look at movers

                for (int i = 1; i < g.licSize() + 1; i++) {

                    Lex pair = value.getExpression()[i];
                    if (pair == null) { // empty mover slots are always valid
                        suf = true;
                    } else if (!pair.isValid(g)) {
                        suf = false;
                    } else {
                        if (!pair.head().isMove() // not a mover
                                || (pair.head().isMove() && pair.headFeatureIndex(g) != i)) { // or not in the right slot
                            valid = false;
                            break;
                        } else {
                            for (Lex li : g.getLexicon()) {
                                if (li.getFeatures().suffix(pair.getFeatures())) {
                                    suf = true;
                                    break; // if we find one LI this is a suffix of, we're okay
                                }
                            }
                        }
                    }
                    if (suf == false) { // if even one feature stack is invalid, the whole expression is too
                        valid = false;
                        break;
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Parses a string into an expression, using {@link MG#generateLexicalItem(java.lang.String)  }.
     * Currently only works for correctly formatted string representing a lexical item
     * Format: string part::=F +e +a T -u -r -e -s
     * @param representation
     * @return expression with the right number of mover slots
     * @throws de.up.ling.irtg.algebra.ParserException if not parsable
     */
    @Override
    public Expression parseString(String representation) throws ParserException {
        try {
            return new Expression(new MGInputCodec().read(representation).head(), g);
        } catch (Throwable ex) {

            throw new ParserException("Could not parse " + representation + ": " + ex.toString());
        }
    }


//    
//    /**
//     * Creates a GraphAlgebra based on {@code graph} with {@code nrSources} many
//     * sources (named 1,..,nrSources).
//     * The resulting algebra contains as constants all atomic subgraphs (single
//     * edges, and single labled nodes), with all possible source combinations.
//     * Further, the merge operation and all possible versions of forget and
//     * rename. It is encouraged to use {@code makeIncompleteDecompositionAlgebra}
//     * instead, since its result is equally expressive and smaller (due to
//     * less spurious constants).
//     * @param graph
//     * @param nrSources
//     * @throws Exception
//     * @return
//     */
//    public static GraphAlgebra makeCompleteDecompositionAlgebra(SGraph graph, int nrSources) throws Exception//only add empty algebra!!
//    {
//        Signature sig = new Signature();
//        Set<String> sources = new HashSet<>();
//        for (int i = 0; i < nrSources; i++) {
//            sources.add(String.valueOf(i));
//        }
//        Set<String> seenEdgeLabels = new HashSet<>();
//        Set<String> seenNodeLabels = new HashSet<>();
//        for (String source1 : sources) {
//            sig.addSymbol("f_" + source1, 1);
//            for (String vName : graph.getAllNodeNames()) {
//                String nodeLabel = graph.getNode(vName).getLabel();
//                if (!seenNodeLabels.contains(nodeLabel)){
//                    seenNodeLabels.add(nodeLabel);
//                    sig.addSymbol("(" + vName + "<" + source1 + "> / " + nodeLabel + ")", 0);
//                }
//            }
//            for (String source2 : sources) {
//                if (!source2.equals(source1)) {
//                    sig.addSymbol("r_" + source1 + "_" + source2, 1);
//                    sig.addSymbol("s_" + source1 + "_" + source2, 1);
//                    for (String vName1 : graph.getAllNodeNames()) {
//                        for (String vName2 : graph.getAllNodeNames()) {
//                            if (!vName1.equals(vName2)) {
//                                GraphEdge e = graph.getGraph().getEdge(graph.getNode(vName1), graph.getNode(vName2));
//                                if (e != null) {
//                                    String edgeLabel = e.getLabel();
//                                    if (!seenEdgeLabels.contains(edgeLabel)){
//                                        seenEdgeLabels.add(edgeLabel);
//                                        sig.addSymbol("(" + vName1 + "<" + source1 + "> :" + edgeLabel + " (" + vName2 + "<" + source2 + ">))", 0);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        sig.addSymbol("merge", 2);
//        return new GraphAlgebra(sig);
//    }
//
//    /**
//     * Creates a GraphAlgebra based on {@code graph} with {@code nrSources} many
//     * sources (named 1,..,nrSources).
//     * The resulting algebra contains as constants all atomic subgraphs (single
//     * edges, and single labled nodes), but only one source is used for nodes,
//     * and one more source for edges (both possibilities to name the vertices
//     * incident to the edge with these two sources are included).
//     * Further, the merge operation and all possible versions of forget and 
//     * rename.
//     * @param graph
//     * @param nrSources
//     * @throws Exception
//     * @return
//     */
//    public static GraphAlgebra makeIncompleteDecompositionAlgebra(SGraph graph, int nrSources) throws Exception//only add empty algebra!!
//    {
//        Signature sig = new Signature();
//        Set<String> sources = new HashSet<>();
//        for (int i = 0; i < nrSources; i++) {
//            sources.add(String.valueOf(i));
//        }
//        for (String source1 : sources) {
//            sig.addSymbol("f_" + source1, 1);
//            for (String source2 : sources) {
//                if (!source2.equals(source1)) {
//                    sig.addSymbol("r_" + source1 + "_" + source2, 1);
//                    //sig.addSymbol("s_" + source1 + "_" + source2, 1);
//                }
//            }
//        }
//        Set<String> seenNodeLabels = new HashSet<>();
//        for (String vName : graph.getAllNodeNames()) {
//            String internalVName = "u";//=vName was bad, since we then have a harder time to recognize the same rules again
//            String nodeLabel = graph.getNode(vName).getLabel();
//            if (!seenNodeLabels.contains(nodeLabel)){
//                seenNodeLabels.add(nodeLabel);
//                if (nodeLabel.contains(":")) {
//                    nodeLabel = "\""+nodeLabel+"\"";
//                }
//                sig.addSymbol("(" + internalVName + "<" + sources.iterator().next() + "> / " + nodeLabel + ")", 0);
//            }
//        }
//        Set<String> seenEdgeLabels = new HashSet<>();
//        for (String vName1 : graph.getAllNodeNames()) {
//            for (String vName2 : graph.getAllNodeNames()) {
//                String internalVName1 = "u";
//                String internalVName2 = "v";
//                if (!vName1.equals(vName2)) {
//                    GraphEdge e = graph.getGraph().getEdge(graph.getNode(vName1), graph.getNode(vName2));
//                    if (e != null) {
//                        String edgeLabel = e.getLabel();
//                        if (!seenEdgeLabels.contains(edgeLabel)){
//                            seenEdgeLabels.add(edgeLabel);
//                            Iterator<String> it = sources.iterator();
//                            String s1 = it.next();
//                            String s2 = it.next();
//                            sig.addSymbol("(" + internalVName1 + "<" + s1 + "> :" + edgeLabel + " (" + internalVName2 + "<" + s2 + ">))", 0);
//                            sig.addSymbol("(" + internalVName2 + "<" + s2 + "> :" + edgeLabel + " (" + internalVName1 + "<" + s1 + ">))", 0);
//                        }
//                    }
//                }
//            }
//        }
//        sig.addSymbol("merge", 2);
//        return new GraphAlgebra(sig);
//    }
//
//    
//    /**
//     * Writes an IRTG grammar file based on {@code graph} with {@code nrSources}
//     * many sources (named 1,..,nrSources). The resulting irtg represents a
//     * GraphAlgebra which contains as constants all atomic subgraphs (single
//     * edges, and single labled nodes), but only one source is used for nodes,
//     * and one more source for edges (both possibilities to name the vertices
//     * incident to the edge with these two sources are included).
//     * Further, the merge operation and all possible versions of forget and rename.
//     * The one final state is S, the one nonfinal state is X.
//     * 
//     * @param alg empty GraphAlgebra, carries the result.
//     * @param graph
//     * @param nrSources
//     * @throws Exception
//     */
//    // @todo Keep this only until a more elegant solution based on
//    // makeIncompleteDecompositionAlgebra is found.
//    public static void writeIncompleteDecompositionIRTG(GraphAlgebra alg, SGraph graph, int nrSources, PrintWriter writer) throws Exception//only add empty algebra!!
//    {
//        String terminal = "S!";
//        String nonterminal = "X";
//        String transition = " -> ";
//        String strGraph = "[graph] ";
//
//        writer.println(terminal + transition + "m( " + nonterminal + ", " + nonterminal + ")");
//        writer.println(strGraph + "merge" + "(?1, ?2)");
//        writer.println();
//
//        Signature sig = alg.getSignature();
//        Set<String> sources = new HashSet<>();
//        for (int i = 0; i < nrSources; i++) {
//            sources.add(String.valueOf(i));
//        }
//        for (String source1 : sources) {
//
//            sig.addSymbol("f_" + source1, 1);
//            writer.println(nonterminal + transition + "f" + source1 + "(" + nonterminal + ")");
//            writer.println(strGraph + "f_" + source1 + "(?1)");
//            writer.println();
//
//            for (String source2 : sources) {
//                if (!source2.equals(source1)) {
//                    String algString = "r_" + source1 + "_" + source2;
//                    sig.addSymbol(algString, 1);
//                    writer.println(nonterminal + transition + "r" + source1 + source2 + "(" + nonterminal + ")");
//                    writer.println(strGraph + algString + "(?1)");
//                    writer.println();
//                    
//                    String algString2 = "s_" + source1 + "_" + source2;
//                    sig.addSymbol(algString2, 1);
//                    writer.println(nonterminal + transition + "s" + source1 + source2 + "(" + nonterminal + ")");
//                    writer.println(strGraph + algString2 + "(?1)");
//                    writer.println();
//                }
//            }
//        }
//
//        Set<String> seenNodeLabels = new HashSet<>();
//        
//        for (String vName : graph.getAllNodeNames()) {
//            String nodeLabel = graph.getNode(vName).getLabel();
//            if (!seenNodeLabels.contains(nodeLabel)){
//                seenNodeLabels.add(nodeLabel);
//                String algString = "(" + vName + "<" + sources.iterator().next() + "> / " + nodeLabel + ")";
//                sig.addSymbol(algString, 0);
//                writer.println(nonterminal + transition + nodeLabel + "VERTEX");
//                writer.println(strGraph + "\"" + algString + "\"");
//                writer.println();
//            }
//        }
//
//        Set<String> seenEdgeLabels = new HashSet<>();
//        for (String vName1 : graph.getAllNodeNames()) {
//            for (String vName2 : graph.getAllNodeNames()) {
//                if (!vName1.equals(vName2)) {
//                    GraphEdge e = graph.getGraph().getEdge(graph.getNode(vName1), graph.getNode(vName2));
//                    if (e != null) {
//                        String edgeLabel = e.getLabel();
//                        if (!seenEdgeLabels.contains(edgeLabel)){
//                            seenEdgeLabels.add(edgeLabel);
//                            Iterator<String> it = sources.iterator();
//                            String s1 = it.next();
//                            String s2 = it.next();
//
//                            String algString = "(" + vName1 + "<" + s1 + "> :" + edgeLabel + " (" + vName2 + "<" + s2 + ">))";
//                            sig.addSymbol(algString, 0);
//                            writer.println(nonterminal + transition + edgeLabel + "EDGE");
//                            writer.println(strGraph + "\"" + algString + "\"");
//                            writer.println();
//
//                            algString = "(" + vName1 + "<" + s2 + "> :" + edgeLabel + " (" + vName2 + "<" + s1 + ">))";
//                            sig.addSymbol(algString, 0);
//                            writer.println(nonterminal + transition + edgeLabel + "EDGE2");
//                            writer.println(strGraph + "\"" + algString + "\"");
//                            writer.println();
//                        }
//                    }
//                }
//            }
//        }
//        sig.addSymbol("merge", 2);
//
//        writer.println(nonterminal + transition + "m( " + nonterminal + ", " + nonterminal + ")");
//        writer.println(strGraph + "merge" + "(?1, ?2)");
//        writer.println();
//    }
//    
//    
    private static final String testString1 = "cat::N";
    private static final String testString2 = "which::=N D -nom -wh";
    private static final String testString3 = "whom::D 0acc -wh";
    private static final String testString4 = "::=T +wh C";// null string part
    
    private static final String TESTSET = "_testset_";
    private static final String[] testset = new String[]{testString1, testString2, testString3, testString4};


 
 
    
}
