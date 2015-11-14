/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.rule_finding.pruning.strings;

import de.saar.basic.Pair;
import de.up.ling.irtg.algebra.StringAlgebra;
import de.up.ling.irtg.automata.Rule;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.rule_finding.alignments.SpanAligner;
import de.up.ling.irtg.rule_finding.create_automaton.AlignedTrees;
import de.up.ling.irtg.rule_finding.create_automaton.Propagator;
import de.up.ling.irtg.rule_finding.variable_introduction.JustXEveryWhere;
import static de.up.ling.irtg.util.TestingTools.pt;
import de.up.ling.tree.Tree;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author christoph
 */
public class RightbranchingPunerTest {
    
    /**
     * 
     */
    private AlignedTrees at;
    
    /**
     * 
     */
    private RightbranchingPuner rbp;
    
    
    @Before
    public void setUp() {
        StringAlgebra sal = new StringAlgebra();
        
        TreeAutomaton aut = sal.decompose(sal.parseString("a b c d e"));
        SpanAligner spal = new SpanAligner("0:1:1 1:2:2 2:3:3 3:4:4 4:5:5", aut);
        
        at = new AlignedTrees(aut, spal);
        
        rbp = new RightbranchingPuner();
    }

    /**
     * Test of prePrune method, of class RightbranchingPuner.
     */
    @Test
    public void testPrePrune() {
        List<AlignedTrees<Object>> l = new ArrayList<>();
        l.add(at);
        
        assertEquals(l,this.rbp.prePrune(l));
    }

    /**
     * Test of postPrune method, of class RightbranchingPuner.
     * @throws java.lang.Exception
     */
    @Test
    public void testPostPrune() throws Exception {
        Propagator pg = new Propagator();
        at= new JustXEveryWhere().apply(at);
        AlignedTrees ab = pg.convert(at);
        
        List<AlignedTrees<Object>> l = new ArrayList<>();
        l.add(ab);
        
        l = rbp.postPrune(l,null);
        assertEquals(l.size(),1);
        
        for(Rule r : (Iterable<Rule>) l.get(0).getTrees().getAllRulesTopDown()){
            Pair<Object,Object> p = (Pair<Object,Object>) l.get(0).getTrees().getStateForId(r.getParent());
            assertEquals(l.get(0).getAlignments().getAlignmentMarkers(p),ab.getAlignments().getAlignmentMarkers(p.getLeft()));
        }
        
        assertTrue(l.get(0).getTrees().accepts(pt("*('X{1, 2}_X'('X{1, 2}_X'(*(a,b))),*(c,*(d,e)))")));
        assertFalse(l.get(0).getTrees().accepts(pt("*(*(a,b),*(c,*(d,e)))")));
    }
    
}
