/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.rule_finding.pruning;

import de.up.ling.irtg.automata.ConcreteTreeAutomaton;
import de.up.ling.irtg.automata.Rule;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.rule_finding.create_automaton.SpecifiedAligner;
import de.up.ling.irtg.rule_finding.create_automaton.AlignedTrees;
import de.up.ling.irtg.rule_finding.create_automaton.StateAlignmentMarking;
import de.up.ling.irtg.util.FunctionIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *
 * @author christoph_teichmann
 * @param <X>
 * @param <Y>
 */
public class PruneOneSideTerminating<X,Y> implements Pruner<X,Y,X,X> {
    /**
     * 
     * @param at
     * @return 
     */
    private AlignedTrees<X> transfer(AlignedTrees<X> at) {
        BitSet bits = new BitSet();
        TreeAutomaton<X> tx = at.getTrees();
        IntIterator iit = tx.getAllStates().iterator();
        while(iit.hasNext()){
            int state = iit.nextInt();
            Iterable<Rule> it = tx.getRulesTopDown(state);
            for(Rule r : it){
                if(r.getArity() == 0){
                    bits.set(state);
                    break;
                }
            }
        }
        
        ConcreteTreeAutomaton<X> cta = new ConcreteTreeAutomaton<>(tx.getSignature());
        SpecifiedAligner<X> spa = new SpecifiedAligner<>(cta);
        StateAlignmentMarking<X> ali = at.getAlignments();
        iit = tx.getAllStates().iterator();
        
        List<X> children = new ArrayList<>();
        while(iit.hasNext()){
            int state = iit.nextInt();
            X id = tx.getStateForId(state);
            
            int alt = cta.addState(id);
            if(tx.getFinalStates().contains(state)){
                cta.addFinalState(alt);
            }
            
            spa.put(id, ali.getAlignmentMarkers(id));
            
            Iterable<Rule> rs = tx.getRulesTopDown(state);
            
            for(Rule r : rs){
                if(r.getArity() > 1){
                    int nonTerm = 0;
                    
                    for(int child : r.getChildren()){
                        nonTerm += bits.get(child) ? 0 : 1;
                    }
                    if(nonTerm > 1){
                        continue;
                    }
                }
                
                children.clear();
                String label = tx.getSignature().resolveSymbolId(r.getLabel());
                for(int child : r.getChildren()){
                    children.add(tx.getStateForId(child));
                }
                
                cta.addRule(cta.createRule(id, label, children));
            }
        }
        
        return new AlignedTrees<>(cta,spa);
    }

    @Override
    public Iterable<AlignedTrees<X>> prePrune(Iterable<AlignedTrees<X>> alignmentFree) {
        return new FunctionIterable<>(alignmentFree, (AlignedTrees<X> at) -> {
            return transfer(at);
        });
    }

    @Override
    public Iterable<AlignedTrees<X>> postPrune(Iterable<AlignedTrees<X>> variablesPushed, Iterable<AlignedTrees<Y>> otherSide) {
        return variablesPushed;
    }
}
