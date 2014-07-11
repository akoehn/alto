/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.graph;

import com.google.common.collect.Iterables;
import static de.up.ling.irtg.util.Util.gfun;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 *
 * @author koller
 */
public class SGraph {
    private DirectedGraph<GraphNode, GraphEdge> graph;
    private Map<String, GraphNode> nameToNode;
    private Map<String, String> sourceToNodename;
    private static long nextGensym = 1;

    public SGraph() {
        graph = new DefaultDirectedGraph<GraphNode, GraphEdge>(new GraphEdgeFactory());
        nameToNode = new HashMap<String, GraphNode>();
        sourceToNodename = new HashMap<>();
    }

    public GraphNode addNode(String name, String label) {
        GraphNode u = nameToNode.get(name);
        
        if( u != null ) {
            if (label != null) {
                nameToNode.get(name).setLabel(label);
            }
        } else {
            u = new GraphNode(name, label);
            graph.addVertex(u);
            nameToNode.put(name, u);
        }
        
        return u;
    }

    public GraphNode addAnonymousNode(String label) {
        GraphNode u = new GraphNode(gensym("_u"), label);
        graph.addVertex(u);
        return u;
    }

    public GraphEdge addEdge(GraphNode src, GraphNode tgt, String label) {
        GraphEdge e = graph.addEdge(src, tgt);
        e.setLabel(label);
        return e;
    }
    
    public void addSource(String sourceName, String nodename) {
        sourceToNodename.put(sourceName, nodename);
    }
    
    public GraphNode getNode(String name) {
        return nameToNode.get(name);
    }

    public boolean containsNode(String name) {
        return nameToNode.containsKey(name);
    }
    
    
    public SGraph merge(SGraph other) {
        if( nameToNode.keySet().stream().anyMatch(other.nameToNode::containsKey) ) {
            throw new UnsupportedOperationException("Graphs are not disjoint");
        }
        
        Map<String,String> nodeRenaming = new HashMap<>(); // maps node names of other to node names of this with same source
        for( String source : other.sourceToNodename.keySet() ) {
            assert this.sourceToNodename.containsKey(source);
            nodeRenaming.put(other.sourceToNodename.get(source), this.sourceToNodename.get(source));
        }
        
        SGraph ret = new SGraph();
        copyInto(ret);
        other.copyInto(ret, nodeRenaming::get);
        
        return ret;
    }
    
    public SGraph renameSource(String oldName, String newName) {
        if( ! sourceToNodename.containsKey(oldName)) {
            throw new UnsupportedOperationException("Graph has no node for source " + oldName);
        }
        
        // TODO - old graph and nodename table could be safely shared
        SGraph ret = new SGraph();        
        copyInto(ret);
        
        String nodenameForSource = ret.sourceToNodename.remove(oldName);
        ret.sourceToNodename.put(newName, nodenameForSource);
        
        return ret;
    }
    
    public SGraph withFreshNodenames() {
        Map<String,String> renaming = new HashMap<>();
        
        for( String nodename : nameToNode.keySet() ) {
            renaming.put(nodename, gensym("u"));
        }
        
        SGraph ret = new SGraph();
        copyInto(ret, renaming::get);
        
        return ret;
    }
    
    private void copyInto(SGraph into) {
        copyInto(into, x -> { return x; });
    }
    
    private void copyInto(SGraph into, Function<String,String> nodeRenaming) {
        for( String nodename : nameToNode.keySet() ) {
            into.addNode(nodeRenaming.apply(nodename), nameToNode.get(nodename).getLabel());
        }
        
        for( GraphEdge edge : graph.edgeSet() ) {
            into.addEdge(into.getNode(nodeRenaming.apply(edge.getSource().getName())), 
                         into.getNode(nodeRenaming.apply(edge.getTarget().getName())),
                         edge.getLabel());
        }
        
        for( String source : sourceToNodename.keySet() ) {
            into.addSource(source, nodeRenaming.apply(sourceToNodename.get(source)));
        }
    }
    

    public DirectedGraph<GraphNode, GraphEdge> getGraph() {
        return graph;
    }
    
    public String getSourceLabel(String nodename) {
        for( String source : sourceToNodename.keySet() ) {
            if( sourceToNodename.get(source).equals(nodename) ) {
                return "<" + source + ">";
            }
        }
        
        return "";
    }
    
    private static String gensym(String prefix) {
        return prefix + "_" + (nextGensym++);
    }
    
    
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[-a-zA-z0-9]+");
    
    private static String p(String s) {
        if (TOKEN_PATTERN.matcher(s).matches()) {
            return s;
        } else {
            return "\"" + s + "\"";
        }
    }
    
    private void toAmrVisit(GraphNode u, Set<GraphNode> visitedNodes, StringBuilder ret) {
        if (visitedNodes.contains(u)) {
            ret.append(u.getName());
        } else {
            boolean nameShown = false;

            visitedNodes.add(u);

            if (!u.getName().startsWith("_")) { // suppress anonymous nodes
                ret.append("(");
                ret.append(p(u.getName()));                
                nameShown = true;
            }

            if (u.getLabel() != null) {
                if (nameShown) {
                    ret.append(" / ");
                }
                ret.append(p(u.getLabel()));
            }

            for (GraphEdge e : graph.outgoingEdgesOf(u)) {
                ret.append("  :" + e.getLabel() + " ");
                toAmrVisit(e.getTarget(), visitedNodes, ret);
            }

            if (nameShown) {
                ret.append(")");
            }
        }
    }

    public String toIsiAmrString() {
        final StringBuilder buf = new StringBuilder();
        final Set<GraphNode> visitedNodes = new HashSet<GraphNode>();

        // TODO - make sure all nodes are shown
        toAmrVisit(graph.vertexSet().iterator().next(), visitedNodes, buf);

        return buf.toString();
    }

    @Override
    public String toString() {
//        return sourceToNodename + toIsiAmrString();
        String nodepart = Iterables.transform(graph.vertexSet(), gfun(GraphNode.reprF)).toString();
        String edgepart = Iterables.transform(graph.edgeSet(), gfun(GraphEdge.reprF)).toString();
        
        return sourceToNodename + nodepart + edgepart;
    }
    
    public static void main(String[] args) throws ParseException {
        SGraph want = IsiAmrParser.parse(new StringReader("(u<root> / want-01  :ARG0 (b<subj>)  :ARG1 (g<vcomp>))"));
        SGraph boy = IsiAmrParser.parse(new StringReader("(x<root> / boy)"));
        SGraph go = IsiAmrParser.parse(new StringReader("(g<root> / go-01  :ARG0 (s<subj>))"));
        
//        System.out.println(want);        
//        System.out.println(want.withFreshNodenames());
//        System.out.println(want.renameSource("root", "foo"));

        System.err.println("go: " + go);
        System.err.println("go f: " + go.withFreshNodenames());
        System.err.println("go: " + go.withFreshNodenames().renameSource("root", "vcomp").renameSource("subj", "subj"));
        System.err.println("boy: " + boy.withFreshNodenames().renameSource("root", "subj"));
        
        
        SGraph combined = want.withFreshNodenames().merge(go.withFreshNodenames().renameSource("root", "vcomp").renameSource("subj", "subj"))
                    .merge(boy.withFreshNodenames().renameSource("root", "subj"));
        
        System.err.println("com " + combined);
        
        SGraphDrawer.draw(combined, "SGraph");
    }
}