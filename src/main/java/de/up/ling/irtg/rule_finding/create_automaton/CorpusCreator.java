/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.rule_finding.create_automaton;

import de.saar.basic.Pair;
import de.up.ling.irtg.automata.RuleFindingIntersectionAutomaton;
import de.up.ling.irtg.automata.TopDownIntersectionAutomaton;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.rule_finding.pruning.Pruner;
import de.up.ling.irtg.rule_finding.pruning.RemoveDead;
import de.up.ling.irtg.util.BiFunctionIterable;
import de.up.ling.irtg.util.BiFunctionParallelIterable;

/**
 *
 * @author christoph_teichmann
 */
public class CorpusCreator {
    
    /**
     * 
     */
    private final Pruner firstPruner;
    
    /**
     * 
     */
    private final Pruner secondPruner;
    
    /**
     * 
     */
    private final int maxThreads;

    
    protected CorpusCreator(Pruner firstPruner, Pruner secondPruner, int maxThreads) {
        this.firstPruner = firstPruner;
        this.secondPruner = secondPruner;
        this.maxThreads = maxThreads;
    }

    /**
     * 
     * @return 
     */
    public Pruner getFirstPruner() {
        return firstPruner;
    }

    /**
     * 
     * @return 
     */
    public Pruner getSecondPruner() {
        return secondPruner;
    }
    
    /**
     * 
     * @param automata
     * @param alignments
     * @return 
     */
    public Iterable<TreeAutomaton<String>> pushAlignments(Iterable<TreeAutomaton> automata, Iterable<StateAlignmentMarking> alignments) {
        Propagator prop = new Propagator();
        
        return new BiFunctionIterable<>(automata,alignments,(TreeAutomaton ta, StateAlignmentMarking marks) -> {
            
            return prop.convert(ta, marks);
        });
    }
    
    /**
     * 
     * @param leftInput
     * @param rightInput
     * @return 
     */
    public Iterable<Pair<TreeAutomaton,HomomorphismManager>> getSharedAutomata(Iterable<TreeAutomaton> leftInput, Iterable<TreeAutomaton> rightInput) {
        BiFunctionParallelIterable<TreeAutomaton,TreeAutomaton,Pair<TreeAutomaton,HomomorphismManager>> results =
                new BiFunctionParallelIterable<>(leftInput, rightInput, maxThreads, 
                (TreeAutomaton left, TreeAutomaton right) -> {
                    left = this.firstPruner.apply(left);
                    right = this.secondPruner.apply(right);
                    
                    HomomorphismManager hom = new HomomorphismManager(left.getSignature(),right.getSignature());
                    hom.update(left.getAllLabels(), right.getAllLabels());
                    
                    RuleFindingIntersectionAutomaton rfi = new RuleFindingIntersectionAutomaton(left, right, hom.getHomomorphism1(), hom.getHomomorphism2());
                    TopDownIntersectionAutomaton tdi = new TopDownIntersectionAutomaton(rfi, hom.getRestriction());
                    
                    TreeAutomaton finished = RemoveDead.reduce(tdi);
                    finished = hom.reduceToOriginalVariablePairs(finished);
                    
                    
                    return new Pair<>(finished,hom);
                });
        
        return results;
    }
}