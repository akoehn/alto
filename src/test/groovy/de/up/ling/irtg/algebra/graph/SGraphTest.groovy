/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.up.ling.irtg.algebra.graph


import org.junit.*
import java.util.*
import java.io.*
import de.up.ling.irtg.automata.*
import static org.junit.Assert.*
import static org.hamcrest.CoreMatchers.*;
import de.saar.chorus.term.parser.*;
import de.up.ling.tree.*;
import de.up.ling.irtg.algebra.*;
import de.up.ling.irtg.hom.*;
import static de.up.ling.irtg.util.TestingTools.*;
import de.up.ling.irtg.*

import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.graph.*;


/**
 *
 * @author koller
 */
class SGraphTest {
    @Test
    public void testIso() {
        SGraph g1 = pg("(v / want-01  :ARG0 (b)  :ARG1 (g))");        
        SGraph g2 = pg("(w / want-01  :ARG0 (b)  :ARG1 (g))");
        assertEquals(g1, g2);
    }
    
    @Test
    public void testIso2() {
        SGraph g1 = pg("(w / want-02  :ARG0 (b)  :ARG1 (g))");
        SGraph g2 = pg("(w / want-01  :ARG0 (b)  :ARG1 (g))");
        assertThat(g1, is(not(g2)))
    }
    
    //changed first graph from (w / want-01  :ARG0 (w)  :ARG1 (g))
    //to (w / want-01  :ARG0 (g)  :ARG1 (g)), since loops are not allowed. --JG
    @Test
    public void testIso3() {
        SGraph g1 = pg("(w / want-01  :ARG0 (g)  :ARG1 (g))");
        SGraph g2 = pg("(w / want-01  :ARG0 (b)  :ARG1 (g))");
        assertThat(g1, is(not(g2)))
    }
    
    @Test
    public void testIso4() {
        SGraph g1 = pg("(u_273 / want :ARG1 (u_274 / sleep :ARG0 (u_272 / boy)) :ARG0 u_272)")
        SGraph g2 = pg("(u_27<root> / want :ARG0 (u_26 / boy) :ARG1 (u_28 / sleep :ARG0 u_26))")
        assertThat(g1, is(not(g2))) // because of sources, see also Iso5
    }
    
    @Test
    public void testIso5() {
        SGraph g1 = pg("(u_273 / want :ARG1 (u_274 / sleep :ARG0 (u_272 / boy)) :ARG0 u_272)")
        SGraph g2 = pg("(u_27 / want :ARG0 (u_26 / boy) :ARG1 (u_28 / sleep :ARG0 u_26))")
        assertEquals(g1, g2);
    }
    
    //tests that node names do not mess up iso
    public void testIso6() {
        SGraph g1 = pg("(v / want-01  :ARG0 (w)  :ARG1 (b))");        
        SGraph g2 = pg("(w / want-01  :ARG0 (b)  :ARG1 (g))");
        assertEquals(g1, g2);
    }
    
    @Test
    public void testDisjointMerge() {
        SGraph g1 = pg("(w<root> / want-01  :ARG0 (b<left>)  :ARG1 (g<right>))")
        SGraph g2 = pg("(bb<left> :ARG0 (gg<right>))")
        SGraph gold = pg("(w<root> / want-01 :ARG0 (b<left> :ARG0 (g<right>)) :ARG1 (g))")
        
        assertEquals(gold, g1.merge(g2))
    }
    
    // Node names should not influence the merging process. I.e. the nodes with name b in g1 and g2 should
    // be separate nodes after the merge.
    @Test
    public void testMergeCommonNodeNameOnDisjointNodes() {
        SGraph g1 = pg("(w<root> / want-01  :ARG0 (b/b1)  :ARG1 (g/g))")
        SGraph g2 = pg("(g<root> :ARG2 (b/b2))")
        SGraph gold = pg("(w<root> / want-01 :ARG0 (b/b1)  :ARG1 (g/g) :ARG2 (b2/b2))")
        
        assertEquals(gold, g1.merge(g2))
    }
    
    //We want to allow merging of completely disjoint graphs.
    @Test
    public void testMergeNoCommonSource() {
        SGraph g1 = pg("(a/a  :ARG0 (b/b))")
        SGraph g2 = pg("(c/c)")
        SGraph gold = new SGraph()
        GraphNode a = gold.addNode("a", "a")
        GraphNode b = gold.addNode("b", "b")
        gold.addNode("c", "c")
        gold.addEdge(a, b, "ARG0")
        
        assertEquals(gold, g1.merge(g2))
    }
    
    //We want to allow merging of completely disjoint graphs. A common node name
    //should not interfere with that.
    @Test
    public void testMergeCommonNodeNameNoCommonSource() {
        SGraph g1 = pg("(a :ARG0 (b/b))")
        SGraph g2 = pg("(a :ARG1 (d/d))")
        SGraph gold = new SGraph()
        GraphNode a = gold.addNode("a", null)
        GraphNode b = gold.addNode("b", "b")
        GraphNode c = gold.addNode("c", null)
        GraphNode d = gold.addNode("d", "d")
        gold.addEdge(a, b, "ARG0")
        gold.addEdge(c, d, "ARG1")
        
        assertEquals(gold, g1.merge(g2))
    }
    
    @Test
    public void testFreshNames() {
        SGraph g1 = pg("(w<root> / want-01  :ARG0 (b<left>)  :ARG1 (g<right>))")
        SGraph fresh = g1.withFreshNodenames()
        
        assertEquals(g1, fresh)
        assert Collections.disjoint(g1.getAllNodeNames(), fresh.getAllNodeNames())
    }
    
    @Test
    public void testRename() {
        SGraph g1 = pg("(w<root> / want-01  :ARG0 (b<left>)  :ARG1 (g<right>))")
        SGraph renamed = g1.renameSource("left", "foo")
        SGraph gold = pg("(w<root> / want-01  :ARG0 (b<foo>)  :ARG1 (g<right>))")
        
        assertEquals(gold, renamed)
    }
    
    @Test
    public void testMergeComplex() {
        SGraph want = pg("(u<root> / want-01  :ARG0 (b<subj>)  :ARG1 (g<vcomp>))");
        SGraph boy = pg("(x<root> / boy)");
        SGraph go = pg("(g<root> / go-01  :ARG0 (s<subj>))");
        
        SGraph combined = want.withFreshNodenames().merge(go.withFreshNodenames().renameSource("root", "vcomp").renameSource("subj", "subj"))
                    .merge(boy.withFreshNodenames().renameSource("root", "subj"));
                    
        SGraph gold = pg("(u<root> / want-01  :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01  :ARG0 (b)))")
        
        assertEquals(gold, combined)
    }
    
    @Test
    public void testMergeExtraSourcesInArgument() {
        SGraph g1 = pg("(u_15<root> :ARG0 (u_16<obj>) :ARG1 (u_17<xcomp>))")
        SGraph g2 = pg("(u_11<xcomp> / want-01  :ARG0 (u_12<subj> / boy)  :ARG1 (u_13<vcomp> / go-01  :ARG0 (u_14<obj> / girl)))")
        SGraph gold = pg("(u_15<root>  :ARG0 (u_16<obj> / girl)  :ARG1 (u_17<xcomp> / want-01  :ARG0 (u_12<subj> / boy) :ARG1 (u_13<vcomp> / go-01 :ARG0 (u_16))))")
        // u12  u13
        
        assertThat(g1.merge(g2), is(gold));        
    }
    
    @Test
    public void testMergeRenamingUnknownSource() {
        SGraph g1 = pg("(u_15<root> :ARG0 (u_16<obj>) :ARG1 (u_17<xcomp>))")
        assertThat(g1.renameSource("foo", "bar"), nullValue())
    }
    
    @Test
    public void testEqualsHashcode() {
        SGraph g1 = pg("   (w<root> / want-01  :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01 :ARG0 b))")
        SGraph g2 = pg("(u_42<root> / want-01  :ARG0 (u_41<subj> / boy)  :ARG1 (u_43<vcomp> / go-01 :ARG0 u_41))")
        
        assert g1.equals(g2) : "not equals"
        assert g1.hashCode() == g2.hashCode() : "different hashcodes"
    }
    
    @Test
    public void testToString() {
        SGraph g1 = pg("(x<root> / boy)")
        assertThat(g1.toString(), is("[x<root>/boy]"))
    }
    
    @Test
    public void testIdentical() {
        SGraph g1 = pg("   (w<root> / want-01  :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01 :ARG0 b))")
        SGraph g2 = pg("   (w<root> / want-01  :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01 :ARG0 b))")
        
        assert g1.isIdentical(g2)
        assert g2.isIdentical(g1)
    }
    
    @Test
    public void testNotIdentical() {
        SGraph g1 = pg("   (w<root> / want-01  :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01 :ARG0 b))")
        
        SGraph g2 = pg("   (ww<root> / want-01  :ARG0 (bb<subj> / boy)  :ARG1 (gg<vcomp> / go-01 :ARG0 bb))")
        assert ! g1.isIdentical(g2)
        assert ! g2.isIdentical(g1)
        
        SGraph g3 = pg("   (w<root> / want-01  :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01 ))")
        assert ! g1.isIdentical(g3)
        assert ! g3.isIdentical(g1)
        
        SGraph g4 = pg("   (w<root>   :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01 :ARG0 b))")
        assert ! g1.isIdentical(g4)
        assert ! g4.isIdentical(g1)
    }
    
    @Test
    public void testMatchingSubgraphsOneLabeledNode() {
        SGraph g1 = pg("   (w<root> / want-01  :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01 :ARG0 b))")
        SGraph g2 = pg("(w<root> / boy)")
        List<SGraph> matches = g1.getMatchingSubgraphs(g2);
        
        assert matches.size() == 1
        assert matches.get(0).isIdentical(pg("(b<root>/boy)"))
    }
    
    @Test
    public void testMatchingSubgraphsOneEdge() {
        SGraph g1 = pg("   (w<root> / want-01  :ARG0 (b<subj> / boy)  :ARG1 (g<vcomp> / go-01 :ARG0 b))")
        SGraph g2 = pg("(u<1> :ARG0 (v<2>))")
        List<SGraph> matches = g1.getMatchingSubgraphs(g2);
        
        assert matches.size() == 2
        
        assert matches.get(0).isIdentical(pg("(w<1> :ARG0 (b<2>))")) || matches.get(0).isIdentical(pg("(g<1> :ARG0 (b<2>))"))
        assert matches.get(1).isIdentical(pg("(w<1> :ARG0 (b<2>))")) || matches.get(1).isIdentical(pg("(g<1> :ARG0 (b<2>))"))
    }
    
    @Test
    public void testMultiedge() {
        SGraph g = pg("(l<root> / like-01 :ARG0 (h / he) :ARG1 h)")
        assert g.getGraph().edgeSet().size() == 2
        assert g.getGraph().vertexSet().size() == 2
    }
    
    @Test
    public void testMultiedge2() {
        SGraph g1 = pg("(r<root> / rt :a (o / other) :b o :c o)")
        SGraph g2 = pg("(r2<root> / rt :b (o2 / other) :c o2 :a o2)")
        assert g1.getGraph().edgeSet().size() == 3
        assert g1.getGraph().vertexSet().size() == 2
        assert g1.equals(g2)
    }
    
}

