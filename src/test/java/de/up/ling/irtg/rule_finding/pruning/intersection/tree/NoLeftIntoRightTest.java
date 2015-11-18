/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.rule_finding.pruning.intersection.tree;

import de.up.ling.irtg.algebra.MinimalTreeAlgebra;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.rule_finding.alignments.AddressAligner;
import de.up.ling.irtg.rule_finding.create_automaton.AlignedTrees;
import de.up.ling.irtg.rule_finding.pruning.intersection.IntersectionPruner;
import static de.up.ling.irtg.util.TestingTools.pt;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author christoph_teichmann
 */
public class NoLeftIntoRightTest {
    
    /**
     * 
     */
    private IntersectionPruner ip;
    
    /**
     * 
     */
    private AlignedTrees at;
    
    @Before
    public void setUp() throws ParserException {
        ip = new IntersectionPruner((TreeAutomaton ta) -> new NoLeftIntoRight(ta.getSignature(), ta.getAllLabels()));
        
        MinimalTreeAlgebra mta = new MinimalTreeAlgebra();
        
        TreeAutomaton ta = mta.decompose(mta.parseString("answer(count(state(intersection(next_to_2(stateid('colorado')) , next_to_2(stateid('new mexico'))))))"));
        AddressAligner aa = new AddressAligner(ta, "0-0-0-0:1 0-0-0-0-0-0-0:2 0-0-0-0-0-0-0-0-0:3 0-0-0-0-0-0-0-0:4 0-0-0-0-0-0-1:5 0-0-0-0-0-0-1-0-0:6");
        
        at = new AlignedTrees(ta, aa);
    }

    @Test
    public void testSomeMethod() throws Exception {
        List<AlignedTrees<Object>> l = new ArrayList<>();
        l.add(at);
        
        List<AlignedTrees<Object>> l2 = ip.prePrune(l);
        assertTrue(l==l2);
        
        l2 = ip.postPrune(l, null);
        
        TreeAutomaton ta = l2.get(0).getTrees();
        
        assertEquals(ta.language().size(),1);
        assertTrue(ta.language().contains(pt("__RIGHT__INTO__LEFT__(answer,__RIGHT__INTO__LEFT__(count,__RIGHT__INTO__LEFT__(state,__RIGHT__INTO__LEFT__(__RIGHT__INTO__LEFT__(intersection,__RIGHT__INTO__LEFT__(next_to_2,__RIGHT__INTO__LEFT__(stateid,colorado))),__RIGHT__INTO__LEFT__(next_to_2,__RIGHT__INTO__LEFT__(stateid,'new mexico'))))))")));
    }
}
