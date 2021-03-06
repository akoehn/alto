/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.graph;

import de.saar.basic.Pair;
import de.up.ling.irtg.algebra.Algebra;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.IsiAmrInputCodec;
import de.up.ling.irtg.signature.Signature;
import de.up.ling.tree.ParseException;
import de.up.ling.tree.Tree;
import de.up.ling.tree.TreeParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import org.jgrapht.experimental.isomorphism.AdaptiveIsomorphismInspectorFactory;
import org.jgrapht.experimental.isomorphism.GraphIsomorphismInspector;
import org.jgrapht.experimental.isomorphism.IsomorphismRelation;

/**
 *
 * @author Jonas
 */
public class ApplyModifyGraphAlgebra extends Algebra<Pair<SGraph, ApplyModifyGraphAlgebra.Type>> {

    public ApplyModifyGraphAlgebra() {
        this(new Signature());
    }

    public ApplyModifyGraphAlgebra(Signature signature) {
        this.signature = signature;
    }

    public static final String GRAPH_TYPE_SEP = "--TYPE--";

    public static final String OP_APPLICATION = "APP_";
    public static final String OP_MODIFICATION = "MOD_";
    public static final String OP_COREF = "COREF_";
    public static final String OP_COREFMARKER = "MARKER_";

    @Override
    public TreeAutomaton decompose(Pair<SGraph, ApplyModifyGraphAlgebra.Type> value) {
        //first, if the signature is empty, we make one. This is necessary for e.g. the GUI to work
        if (signature.getSymbols().isEmpty()) {
            try {
                signature = AMSignatureBuilder.makeDecompositionSignature(value.left, 0);
            } catch (ParseException ex) {
                Logger.getLogger(ApplyModifyGraphAlgebra.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (value.right.rho.isEmpty()) {
            return new AMDecompositionAutomaton(this, null, value.left);
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public JComponent visualize(Pair<SGraph, Type> object) {
        return new GraphAlgebra().visualize(object.left); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Pair<SGraph, ApplyModifyGraphAlgebra.Type> evaluate(String label, List<Pair<SGraph, ApplyModifyGraphAlgebra.Type>> childrenValues) {
        if (label.startsWith(OP_APPLICATION) && childrenValues.size() == 2) {
            String appSource = label.substring(OP_APPLICATION.length());
            SGraph target = childrenValues.get(1).left;
            Type targetType = childrenValues.get(1).right;
            Type leftType = childrenValues.get(0).right;

            //rename right sources to temps
            List<String> orderedSources = new ArrayList(targetType.keySet());
            if (target.getAllSources().contains("root")) {
                orderedSources.add("root");
            } else {
                System.err.println("target had no root in APP!");
                return null;//target must have root for APP to be allowed.
            }
            for (int i = 0; i < orderedSources.size(); i++) {
                target = target.renameSource(orderedSources.get(i), "temp" + i);
            }

            //rename temps to left sources
            Map<String, String> nestedRole2Unif = leftType.id.get(appSource);
            for (int i = 0; i < orderedSources.size(); i++) {
                String src = orderedSources.get(i);
                if (src.equals("root")) {
                    target = target.renameSource("temp" + i, appSource);
                } else {
                    target = target.renameSource("temp" + i, nestedRole2Unif.get(orderedSources.get(i)));
                }
            }

            //target = target.renameSource("root", appName);
            SGraph leftGraph = childrenValues.get(0).left;
            Set<String> retainedSources = new HashSet<>(leftGraph.getAllSources());
            retainedSources.addAll(target.getAllSources());
            retainedSources.remove(appSource);

            SGraph retGraph = target.merge(childrenValues.get(0).left).forgetSourcesExcept(retainedSources);
            if (retGraph == null) {
                System.err.println("APP merge failed!");
            }
            return new Pair(retGraph, leftType.simulateApply(appSource));

        } else if (label.startsWith(OP_MODIFICATION) && childrenValues.size() == 2) {
            String modSource = label.substring(OP_MODIFICATION.length());
            SGraph target = childrenValues.get(1).left;
            Type targetType = childrenValues.get(1).right;

            if (target.getNodeForSource("root") != null) {
                Set<String> retainedSources = new HashSet<>(target.getAllSources());
                retainedSources.remove("root");
                target = target.forgetSourcesExcept(retainedSources);
            }
            target = target.renameSource(modSource, "root");

            if (!childrenValues.get(0).right.canBeModifiedBy(targetType, modSource)) {
                System.err.println("MOD evaluation failed: invalid types! " + childrenValues.get(0).right + " mod by " + targetType);
                return null;//modifier must have empty type at modName, and rest must be compatible with modifyee
            }

            SGraph leftGraph = childrenValues.get(0).left;

            //then just merge
            SGraph retGraph = leftGraph.merge(target);
            if (retGraph == null) {
                System.err.println("APP merge failed!");
            }
            return new Pair(retGraph, childrenValues.get(0).right);
        } else {
            try {
                return parseString(label);
            } catch (ParserException ex) {
                System.err.println("could not parse label!");
                return null;
            }
        }
    }

    @Override
    public Pair<SGraph, ApplyModifyGraphAlgebra.Type> parseString(String representation) throws ParserException {
        if (representation.contains(GRAPH_TYPE_SEP)) {
            if (representation.startsWith(OP_COREFMARKER)) {
                representation = representation.substring(OP_COREFMARKER.length());
                representation = representation.substring(representation.indexOf("_") + 1);
            }
            String[] parts = representation.split(GRAPH_TYPE_SEP);
            try {
                return new Pair(new IsiAmrInputCodec().read(parts[0]), new Type(parts[1]));
            } catch (ParseException ex) {
                throw new ParserException(ex);
            }
        } else if (representation.startsWith(OP_COREF)) {
            String corefIndex = representation.substring(OP_COREF.length());
            String graphString = "(u<root,COREF" + corefIndex + ">)";
            try {
                return new Pair(new IsiAmrInputCodec().read(graphString), new Type("()"));
            } catch (ParseException ex) {
                throw new ParserException(ex);
            }
        } else {
            try {
                return new Pair(new IsiAmrInputCodec().read(representation), new Type("()"));
            } catch (ParseException ex) {
                throw new ParserException(ex);
            }
        }
    }

    public static class Type {

        final Map<String, Type> rho;
        final Map<String, Map<String, String>> id;

        public static final Type EMPTY_TYPE;

        static {
            Type temp;
            try {
                temp = new Type("()");
            } catch (ParseException ex) {
                temp = null;
                System.err.println("this should really never happen");
            }
            EMPTY_TYPE = temp;
        }

        /**
         * Creates a type from a string representation. Example format: (S,
         * O(O2_UNIFY_S, O_UNIFY_O2), O2(S_UNIFY_S)) Notes: No depth deeper than
         * this example is allowed. The unify target always corresponds to the
         * top level Note that a unify target also needs to be specified if no
         * rename occurs, as in S_UNIFY_S. Differences to the notation in the
         * paper include round brackets instead of square brackets, and
         * '_UNIFY_' instead of '->'.
         *
         * @param typeString
         * @throws de.up.ling.tree.ParseException
         *
         */
        public Type(String typeString) throws ParseException {
            this(TreeParser.parse("TOP" + typeString.replaceAll("\\(\\)", "")));
        }

        private Type(Tree<String> typeTree) {
            this.rho = new HashMap<>();
            this.id = new HashMap<>();
            for (Tree<String> roleTree : typeTree.getChildren()) {
                String keyHere = roleTree.getLabel().split("_UNIFY_")[0];
                rho.put(keyHere, new Type(roleTree));
                Map<String, String> role2Unif = new HashMap<>();
                for (Tree<String> nestedRoleTree : roleTree.getChildren()) {
                    String[] parts = nestedRoleTree.getLabel().split("_UNIFY_");
                    role2Unif.put(parts[0], parts[1]);
                }
                id.put(keyHere, role2Unif);
            }
        }

        public Type(Map<String, Type> rho, Map<String, Map<String, String>> id) {
            if (!rho.keySet().equals(id.keySet())) {

            }
            this.rho = rho;
            this.id = id;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.rho);
            hash = 37 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Type other = (Type) obj;
            if (!Objects.equals(this.rho, other.rho)) {
                return false;
            }
            return Objects.equals(this.id, other.id);
        }

        public Set<String> keySet() {
            return rho.keySet();
        }

        @Override
        public String toString() {
            List<String> roleStrings = new ArrayList();
            for (String role : rho.keySet()) {
                roleStrings.add(role + rho.get(role).toStringWithUnify(id.get(role)));
            }
            roleStrings.sort(Comparator.naturalOrder());
            return "(" + roleStrings.stream().collect(Collectors.joining(", ")) + ")";
        }

        private String toStringWithUnify(Map<String, String> id4Unif) {
            List<String> roleStrings = new ArrayList();
            for (String role : rho.keySet()) {
                roleStrings.add(role + "_UNIFY_" + id4Unif.get(role) + rho.get(role).toStringWithUnify(id.get(role)));
            }
            roleStrings.sort(Comparator.naturalOrder());
            return "(" + roleStrings.stream().collect(Collectors.joining(", ")) + ")";
        }

        /**
         * Creates a copy with r removed from the domain. Does not modify the
         * original type.
         *
         * @param r
         * @return
         */
        public Type remove(String r) {
            Map<String, Map<String, String>> newId = new HashMap<>(id);
            Map<String, Type> newRho = new HashMap<>(rho);
            newId.remove(r);
            newRho.remove(r);
            return new Type(newRho, newId);
        }

        /**
         * Returns the type that we obtain after using APP_s on this type (does
         * not modify this type). Returns null if APP_s is not allowed for this
         * type (i.e. if this type does not contain s, or s is needed for later
         * unification).
         *
         * @param s
         * @return
         */
        public Type simulateApply(String s) {

            if (!rho.containsKey(s)) {
                return null;
            }

            //check if we need this for unification later
            Type closure = closure();
            Set<String> allUnifTargets = new HashSet();
            for (String role : closure.rho.keySet()) {
                allUnifTargets.addAll(closure.id.get(role).values());
            }
            if (allUnifTargets.contains(s)) {
                return null;
            } else {
                Map<String, Map<String, String>> newId = new HashMap<>();
                Map<String, Type> newRho = new HashMap<>();
                for (String r : keySet()) {
                    if (!s.equals(r)) {
                        Type recHere = rho.get(r);
                        newRho.put(r, recHere);
                        Map<String, String> idHere = id.get(r);
                        newId.put(r, idHere);
                    }
                }
                Type recHere = rho.get(s);
                Map<String, String> idHere = id.get(s);
                for (String u : idHere.keySet()) {
                    if (!keySet().contains(idHere.get(u))) {
                        newRho.put(idHere.get(u), recHere.rho.get(u));
                        newId.put(idHere.get(u), recHere.id.get(u));
                    }
                }
                return new Type(newRho, newId);
            }
        }

        /**
         * Checks whether this type is a 'subset' of type 'other', when
         * extending the subset notion to functions.
         *
         * @param other
         * @return
         */
        public boolean isCompatibleWith(Type other) {
            if (!other.keySet().containsAll(keySet())) {
                return false;
            }
            for (String r : keySet()) {
                Type rhoR = rho.get(r);
                Type otherRhoR = other.rho.get(r);
                if (!rhoR.isCompatibleWith(otherRhoR)) {
                    return false;//use stricter version to match with IWCS paper -- EDIT currently using the less strict version again with 'isCompatibleWith' instead of 'equals' in the check.
                }
                Map<String, String> iR = id.get(r);
                Map<String, String> otherIR = other.id.get(r);
                for (String nr : iR.keySet()) {
                    if (!iR.get(nr).equals(otherIR.get(nr))) {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * [] has depth 0, [O] has depth 1, [O[S]] has depth 2, etc.
         *
         * @return
         */
        public int depth() {
            if (keySet().isEmpty()) {
                return 0;
            } else {
                int max = 0;
                for (Type t : rho.values()) {
                    max = Math.max(max, t.depth());
                }
                return max + 1;
            }
        }

        /**
         * Returns the set of all types that are expected at the sources.
         *
         * @return
         */
        public Collection<Type> getNestedTypes() {
            return rho.values();
        }

        /**
         * Returns all source names that the nested types signal to be added
         * through APP, e.g. S in [O[S]] or in O[O->S]. This method is not
         * recursive, so to get all types that will be added through any
         * sequence of applies, us this function on the closure of this type.
         *
         * @return
         */
        public Set<String> getUnificationTargets() {
            Set<String> ret = new HashSet<>();
            for (Map<String, String> i : id.values()) {
                ret.addAll(i.values());
            }
            return ret;
        }

        /**
         * Returns the closure of this type, i.e., this function adds all
         * sources now that can later be added through application, and their
         * target types, to this type. E.g. [O[S]] becomes [O[S], S].
         *
         * @return
         */
        public Type closure() {
            Map<String, Map<String, String>> newId = new HashMap<>();
            Map<String, Type> newRho = new HashMap<>();
            for (String r : keySet()) {
                Type recHere = rho.get(r);
                newRho.put(r, recHere);
                Map<String, String> idHere = id.get(r);
                newId.put(r, idHere);
                for (String u : idHere.keySet()) {
                    if (!keySet().contains(idHere.get(u))) {
                        newRho.put(idHere.get(u), recHere.rho.get(u));
                        newId.put(idHere.get(u), recHere.id.get(u));
                    }
                }
            }
            return new Type(newRho, newId);
        }

        public Type getTargetType(String s) {
            if (rho.containsKey(s)) {
                return rho.get(s);
            } else {
                return null;
            }
        }

        public Set<Type> getAllSubtypes() {
            Set<Type> ret = new HashSet<>();
            ret.add(this);
            //TODO the following check is an arbitrary choice to keep complexity explosion in check. This should be properly fixed.
            if (rho.keySet().size() < 10) {
                for (String s : rho.keySet()) {
                    Type after = simulateApply(s);
                    if (after != null) {
                        ret.addAll(after.getAllSubtypes());
                    }
                }
            }
            return ret;
        }

        /**
         * Checks whether APP_appSource(G_1, G_2) is allowed, given G_1 has this
         * type, and G_2 has type 'argument'.
         *
         * @param argument
         * @param appSource
         * @return
         */
        public boolean canApplyTo(Type argument, String appSource) {
            //check if the type expected here at appSource is equal to the argument type
            Type rhoR = this.rho.get(appSource);
            if (rhoR == null || !rhoR.equals(argument)) {
                return false;
            }
            //check if this removes a unification target that we will need later
            Set<String> allUnifTargets = new HashSet<>();
            for (String role : this.keySet()) {
                allUnifTargets.addAll(this.id.get(role).values());
            }
            return !allUnifTargets.contains(appSource);
        }

        /**
         * Checks whether MOD_modSource(G_1, G_2) is allowed, given G_1 has this
         * type, and G_2 has type argument.
         *
         * @param modifier
         * @param modSource
         * @return
         */
        public boolean canBeModifiedBy(Type modifier, String modSource) {
            Type rhoR = modifier.rho.get(modSource);
            return rhoR != null && rhoR.keySet().isEmpty()
                    && modifier.remove(modSource).isCompatibleWith(this);
        }

        /**
         * Returns the type of operation(left, right) if that is defined, and
         * null otherwise. Objects left and right are not modified.
         *
         * @param left
         * @param right
         * @param operation
         * @return
         */
        public static Type evaluateOperation(Type left, Type right, String operation) {
            String s;
            boolean app = false;
            
            //System.err.println(operation);

            if (operation.startsWith(OP_APPLICATION)) {
                s = operation.substring(OP_APPLICATION.length());
                app = true;
                //System.err.println("apply!");
            } else {
                s = operation.substring(OP_MODIFICATION.length());
            }
            if (app) {
                if (left.canApplyTo(right, s)) {
                    return left.simulateApply(s);
                } else {
                    return null;
                }
            } else {
                if (left.canBeModifiedBy(right, s)) {
                    return left;
                } else {
                    return null;
                }
            }
        }

    }

    /**
     * Checks whether g1 and g2 are isomorphic, taking only sources into account
     * that do not start with 'COREF'.
     *
     * @param g1
     * @param g2
     * @return
     */
    public static boolean isomExceptCOREFs(SGraph g1, SGraph g2) {
        GraphIsomorphismInspector iso
                = AdaptiveIsomorphismInspectorFactory.createIsomorphismInspector(
                        g1.getGraph(),
                        g2.getGraph(),
                        new GraphNode.NodeLabelEquivalenceComparator(),
                        new GraphEdge.EdgeLabelEquivalenceComparator());

        if (!iso.isIsomorphic()) {
            return false;
        } else {
            while (iso.hasNext()) {
                final IsomorphismRelation<GraphNode, GraphEdge> ir = (IsomorphismRelation<GraphNode, GraphEdge>) iso.next();

                boolean foundSourceMismatch = false;

                for (String source : g1.getAllSources()) {
                    if (!source.startsWith("COREF")) {
                        GraphNode newNode = ir.getVertexCorrespondence(g1.getNode(g1.getNodeForSource(source)), true);
                        Collection<String> sourcesHere = g2.getSourcesAtNode(newNode.getName());
                        if (sourcesHere == null || !sourcesHere.contains(source)) {
                            foundSourceMismatch = true;
                        }
                    }
                }

                if (!foundSourceMismatch) {
                    return true;
                }
            }

            return false;
        }
    }

}
