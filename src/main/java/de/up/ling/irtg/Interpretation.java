/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg;

import de.up.ling.irtg.algebra.Algebra;
import de.up.ling.irtg.algebra.NullFilterAlgebra;
import de.up.ling.irtg.automata.Intersectable;
import de.up.ling.irtg.automata.InverseHomAutomaton;
import de.up.ling.irtg.automata.NondeletingInverseHomAutomaton;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.automata.condensed.CondensedNondeletingInverseHomAutomaton;
import de.up.ling.irtg.automata.condensed.CondensedTreeAutomaton;
import de.up.ling.irtg.automata.condensed.PMFactoryRestrictive;
import de.up.ling.irtg.automata.condensed.PatternMatchingInvhomAutomatonFactory;
import de.up.ling.irtg.hom.Homomorphism;
import de.up.ling.irtg.laboratory.OperationAnnotation;
import de.up.ling.irtg.siblingfinder.SiblingFinderInvhom;
import de.up.ling.irtg.util.Logging;
import de.up.ling.tree.Tree;
import java.io.Serializable;

/**
 * This class represents an interpretation, a standard component of an IRTG which
 * maps a derivation tree to a value.
 * 
 * An interpretation consists of a homomorphism and an algebra. The Homomorphism
 * maps a derivation tree to an expression in the algebra (also represented as
 * a tree) and the algebra evaluates it.
 * 
 * 
 * @author koller
 * @param <E> Defines the object generated by the interpretation. This is the same
 * type as the objects over which the algebra for this interpretation is defined.
 */
public class Interpretation<E> implements Serializable {

    private Algebra<E> algebra;
    private Homomorphism hom;
    public final String name;
    private PatternMatchingInvhomAutomatonFactory pmFactory;

    /**
     * Constructs  new instance with the given algebra and homomorphism.
     * 
     * @param algebra
     * @param hom 
     */
	@Deprecated
    public Interpretation(Algebra<E> algebra, Homomorphism hom) {
        this.algebra = algebra;
        this.hom = hom;
        this.name = "<unnamed>";
        pmFactory = null;
    }

    /**
     * Constructs  new instance with the given algebra, homomorphism and name.
     *
     * @param algebra
     * @param hom
     * @param name
     */
    public Interpretation(Algebra<E> algebra, Homomorphism hom, String name) {
        this.algebra = algebra;
        this.hom = hom;
        this.name = name;
        pmFactory = null;
    }

    /**
     * Applies the homomorphism to the derivation tree "t" and evaluates it in
     * the algebra.
     *
     * @param t
     * @return
     */
    @OperationAnnotation(code = "interpret")
    public E interpret(Tree<String> t) {
        if (t == null) {
            return null;
        } else {
            return algebra.evaluate(hom.apply(t));
        }
    }

    /**
     * Obtains the algebra used by the interpretation.
     * 
     * @return 
     */
    @OperationAnnotation(code = "alg")
    public Algebra<E> getAlgebra() {
        return algebra;
    }

    /**
     * Obtains the homomorphism used by the interpretation.
     * 
     * @return 
     */
    @OperationAnnotation(code = "hom")
    public Homomorphism getHomomorphism() {
        return hom;
    }

    /**
     * Returns the image under inverse homomorphism of the given automaton.
     *
     * This is based on the homomorphism of this interpretation. 
     * 
     * @param auto
     * @return
     */
    @OperationAnnotation(code = "invhom")
    public Intersectable invhom(TreeAutomaton auto) {
        if (hom.isNonDeleting()) {
            if (!auto.supportsBottomUpQueries()) {
                if (pmFactory == null) {
                    pmFactory = new PMFactoryRestrictive(hom);
                }
                Logging.get().info(() -> "Using condensed inverse hom automaton via pattern matching and top-down queries only.");
                return pmFactory.invhom(auto);
            } else {
                if (!auto.supportsTopDownQueries()) {
                    if (auto.useSiblingFinder()) {
                        Logging.get().info(() -> "Using sibling finder inverse hom automaton.");
                        return new SiblingFinderInvhom(auto, hom);
                    } else {
                        Logging.get().info(() -> "Using basic bottom up inverse hom automaton.");
                        return new NondeletingInverseHomAutomaton(auto, hom);
                    }
                } else {
                    Logging.get().info(() -> "Using condensed inverse hom automaton.");
                    return new CondensedNondeletingInverseHomAutomaton(auto, hom);
                }
            }

            //return new CondensedNondeletingInverseHomAutomaton(decompositionAutomaton, hom);//this works only using top down queries.
        } else {
            if (auto.supportsTopDownQueries()) {

                Logging.get().info(() -> "Using inverse hom automaton for deleting homomorphisms.");
                return new InverseHomAutomaton(auto, hom);
            } else {
                Logging.get().info(() -> "Using non-condensed inverse hom automaton.");
                return auto.inverseHomomorphism(hom);
            }
        }

    }

    /**
     * This returns an inverse homomorphism automaton with the added assumption
     * that the underlying homomorphism is non-deleting.
     * 
     * If the homomorphism is in fact deleting, then behavior is not well defined.
     * 
     * @param auto
     * @return 
     */
    @OperationAnnotation(code = "basicNonDelInvHom")
    public <T> TreeAutomaton<T> basicNonDelInvHom(TreeAutomaton<T> auto) {
        return new NondeletingInverseHomAutomaton<>(auto, hom);
    }

    /**
     * This method takes the given object and attempts to compute its inverse
     * homomorphism automaton.
     * 
     * This means that the decomposition automaton under the algebra of this
     * interpretation is found and then inverse of the homomorphism is applied.
     * 
     * @param object
     * @return 
     */
    public Intersectable<E> parse(E object) {
        TreeAutomaton<E> decompositionAutomaton = algebra.decompose(object);
        // It is much preferable to return a condensed automaton for the
        // inverse homomorphism, if that is possible. Pattern matching works for both top down
        // and bottom up queries.
        return invhom(decompositionAutomaton);

    }

    /**
     * This method takes the given object and attempts to compute its inverse
     * homomorphism automaton in the form of an condensed automaton.
     * 
     * This means that the decomposition automaton under the algebra of this
     * interpretation is found and then inverse of the homomorphism is applied.
     * 
     * @param object
     * @return 
     */
    public CondensedTreeAutomaton parseToCondensed(E object) {
        return algebra.decompose(object).inverseCondensedHomomorphism(hom);
    }

    /**
     * Returns a string containing the algebra class and homomorphism used
     * by this interpretation.
     * 
     * @return 
     */
    @Override
    public String toString() {
        return algebra.getClass() + "\n" + hom.toString();
    }

    /**
     * Equality holds if obj is also an interpretation, which has the same
     * class of algebra and an homomorphism that is equals to the homomorphism
     * for this interpretation.
     * 
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Interpretation<E> other = (Interpretation<E>) obj;
        if (this.algebra.getClass() != other.algebra.getClass() && (this.algebra == null || !this.algebra.getClass().equals(other.algebra.getClass()))) {
            return false;
        }
        return this.hom == other.hom || (this.hom != null && this.hom.equals(other.hom));
    }

    /**
     * This method allows the user to set the name under which feedback from 
     * the pattern matching factory is logged.
     * 
     * 
     * @param name 
     */
    public void setPmLogName(String name) {
        pmFactory.logTitle = name;
    }
    
    /**
     * Applies the null-filter of the algebra to the given parse chart.
     * This is only supported if the algebra in this interpretation
     * implements {@link NullFilterAlgebra}.
     * 
     * @param chart
     * @return 
     */
    public TreeAutomaton filterNull(TreeAutomaton chart) {
        if (algebra instanceof NullFilterAlgebra) {
            NullFilterAlgebra nf = (NullFilterAlgebra) algebra;
            return chart.intersect(nf.nullFilter().inverseHomomorphism(hom));
        } else {
            throw new UnsupportedOperationException("Can only filterNull if the algebra implements NullFilterAlgebra");
        }
    }
}
