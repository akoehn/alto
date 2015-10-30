/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra;

import de.saar.basic.Pair;
import de.up.ling.irtg.automata.SingletonAutomaton;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.util.Evaluator;
import de.up.ling.tree.Tree;
import de.up.ling.tree.TreePanel;
import de.up.ling.tree.TreeParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JComponent;

/**
 * The tree algebra. The elements of this algebra are the ranked trees over a
 * given signature. Any string f can be used as a tree-combining operation of an
 * arbitrary arity; the term f(t1,...,tn) evaluates to the tree f(t1,...,tn).
 * Care must be taken that only ranked trees can be described; the parseString
 * method will infer the arity of each symbol f that you use, and will throw an
 * exception if you try to use f with two different arities.
 *
 * @author koller
 */
public class TreeAlgebra extends Algebra<Tree<String>> {
//    protected final Signature signature = new Signature();

    @Override
    public Tree<String> evaluate(Tree<String> t) {
        return t;
    }

    @Override
    protected Tree<String> evaluate(String label, List<Tree<String>> childrenValues) {
        return Tree.create(label, childrenValues);
    }

    @Override
    public TreeAutomaton decompose(Tree<String> value) {
        return new SingletonAutomaton(value);
    }

    @Override
    public JComponent visualize(Tree<String> object) {
        return new TreePanel(object);
    }

    @Override
    public Tree<String> parseString(String representation) throws ParserException {
        try {
            Tree<String> ret = TreeParser.parse(representation);
            signature.addAllSymbols(ret);
            return ret;
        } catch (Exception e) {
            throw new ParserException(e);
        }
    }

    @Override
    public Class getClassOfValues() {
        return Tree.class;
    }

    @Override
    public List<Evaluator> getEvaluationMethods() {
        List<Evaluator> ret = new ArrayList<>();
        ret.add(new Evaluator<Tree<String>>("Recall") {
            
            @Override
            public Pair<Double, Double> evaluate(Tree<String> result, Tree<String> gold) {
                List<Bracket> resultBrackets = getBrackets(result, 0);
                List<Bracket> goldBrackets = getBrackets(gold, 0);
                double weight = goldBrackets.size();
                int found = 0;
                for (Bracket goldBracket : goldBrackets) {
                    if (resultBrackets.contains(goldBracket)) {
                        found ++;
                    }
                }
                double score = found/weight;
                return new Pair(score, weight);
            }
        });
        ret.add(new Evaluator<Tree<String>>("Precision") {
            
            @Override
            public Pair<Double, Double> evaluate(Tree<String> result, Tree<String> gold) {
                List<Bracket> resultBrackets = getBrackets(result, 0);
                List<Bracket> goldBrackets = getBrackets(gold, 0);
                double weight = resultBrackets.size();
                int found = 0;
                for (Bracket resultBracket : resultBrackets) {
                    if (goldBrackets.contains(resultBracket)) {
                        found ++;
                    }
                }
                double score = (weight > 0) ? found/weight : 1;
                return new Pair(score, weight);
            }
        });
        return ret;
    }
    
    
    
    
    
    
    
    
    
    
    private static List<Bracket> getBrackets(Tree<String> tree, int nrWordsToTheLeft) {
        List<Bracket> ret = new ArrayList<>();
        List<Tree<String>> children = tree.getChildren();
        if (children.isEmpty()) {
            ret.add(new Bracket(nrWordsToTheLeft, nrWordsToTheLeft+1, tree.getLabel()));
            return ret;
        } else {
            int additionalWordsToTheLeft = 0;
            for (Tree<String> child : children) {
                List<Bracket> bracketsHere = getBrackets(child, nrWordsToTheLeft+additionalWordsToTheLeft);
                additionalWordsToTheLeft+= bracketsHere.get(bracketsHere.size()-1).getWidth();
                ret.addAll(bracketsHere);
            }
            ret.add(new Bracket(nrWordsToTheLeft, nrWordsToTheLeft+additionalWordsToTheLeft, tree.getLabel()));//notice how the last bracket is always the last in the list
            return ret;
        }
    }
    
    
    private static class Bracket {
        private int start;
        private int stop;
        private String label;
        
        int getWidth() {
            return stop-start;
        }
        
        
        public Bracket (int start, int stop, String label) {
            this.start = start;
            this.stop = stop;
            this.label = label;
        }

        @Override
        public String toString() {
            return "["+start + ", " + stop + "," + label + ']';
        }
        
        

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + this.start;
            hash = 47 * hash + this.stop;
            hash = 47 * hash + Objects.hashCode(this.label);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Bracket other = (Bracket) obj;
            if (this.start != other.start) {
                return false;
            }
            if (this.stop != other.stop) {
                return false;
            }
            if (!Objects.equals(this.label, other.label)) {
                return false;
            }
            return true;
        }
        
        
    }
    
}
