/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.rule_finding.pruning;

import de.up.ling.irtg.rule_finding.create_automaton.AlignedTrees;
import java.util.List;

/**
 *
 * @author christoph_teichmann
 * @param <State1>
 * @param <State2>
 */
public interface Pruner<State1,State2> {
    
    /**
     * @param alignmentFree
     * 
     * @return 
     */
    public List<AlignedTrees<State1>> prePrune(List<AlignedTrees<State1>> alignmentFree);
    
    
    /**
     * 
     * @param variablesPushed
     * @param otherSide
     * @return 
     */
    public List<AlignedTrees<State1>> postPrune(
            List<AlignedTrees<State1>> variablesPushed, List<AlignedTrees<State2>> otherSide);
    
    /**
     * 
     */
    public Pruner DEFAULT_PRUNER = new Pruner<Object, Object>(){

        @Override
        public List<AlignedTrees<Object>> prePrune(List<AlignedTrees<Object>> alignmentFree) {
            return alignmentFree;
        }

        @Override
        public List<AlignedTrees<Object>> postPrune(List<AlignedTrees<Object>> toPrune, List<AlignedTrees<Object>> otherSide) {
            return toPrune;
        }
    };
}