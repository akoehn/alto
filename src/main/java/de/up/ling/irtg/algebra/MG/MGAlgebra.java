/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

import de.up.ling.irtg.algebra.EvaluatingAlgebra;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.codec.MGInputCodec;
import de.up.ling.irtg.signature.Signature;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public Int2ObjectMap<Expression> getAllConstantLabelInterpretations() {
        if (constantLabelInterpretations == null) {
            precomputeAllConstants();
        }
        return constantLabelInterpretations;
    }
    
    
           
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

    
    
    
    


    
    private static final String testString1 = "cat::N";
    private static final String testString2 = "which::=N D -nom -wh";
    private static final String testString3 = "whom::D 0acc -wh";
    private static final String testString4 = "::=T +wh C";// null string part
    
    private static final String TESTSET = "_testset_";
    private static final String[] testset = new String[]{testString1, testString2, testString3, testString4};


 
 
    
}
