/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.align.rule_markers;

import de.up.ling.irtg.align.RuleMarker;
import de.up.ling.irtg.automata.Rule;
import de.up.ling.irtg.automata.TreeAutomaton;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 *
 * @author christoph_teichmann
 */
public class SimpleRuleMarker implements RuleMarker {
    /**
     * 
     */
    private final String prefix;
    
    /**
     * 
     */
    private final Pattern patt;
    
    /**
     * 
     */
    private final AtomicInteger num = new AtomicInteger(0);
    
    /**
     * 
     */
    private final Object2ObjectMap<Rule,IntSet> firstRules = new Object2ObjectOpenHashMap<>();
    
    /**
     * 
     */
    private final Object2ObjectMap<Rule,IntSet> secondRules = new Object2ObjectOpenHashMap<>();

    /**
     * 
     * @param prefix 
     */
    public SimpleRuleMarker(String prefix) {
        this.prefix = prefix+"_";
        this.patt = Pattern.compile(prefix+"_.*");
    }

    /**
     * 
     * @param r1
     * @param r2 
     */
    public void addPair(Rule r1, Rule r2){
        int marker = num.getAndIncrement();
        
        IntSet is = firstRules.get(r1);
        if(is == null){
            is = new IntOpenHashSet();
            firstRules.put(r1, is);
        }
        is.add(marker);
        
        is = secondRules.get(r2);
        if(is == null){
            is = new IntOpenHashSet();
            secondRules.put(r2, is);
        }
        is.add(marker);
    }

    @Override
    public IntSet evaluateRule(Rule r) {
        if(this.firstRules.containsKey(r)){
            return this.firstRules.get(r);
        }else if(this.secondRules.containsKey(r)){
            return this.secondRules.get(r);
        }
        
        throw new IllegalArgumentException("Unknown rule: "+r);
    }

    @Override
    public String makeCode(IntSet alignments, TreeAutomaton original, int state) {
        String code = this.prefix+alignments.toString();
        return code;
    }

    @Override
    public boolean checkCompatible(String label1, String label2) {
        return label1.equals(label2);
    }

    @Override
    public boolean isFrontier(String label) {
        return this.patt.matcher(label).matches();
    }

    @Override
    public String getCorresponding(String variable) {
        return variable;
    }
}