/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.hom;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import static de.up.ling.irtg.hom.HomomorphismSymbol.Type.CONSTANT;
import static de.up.ling.irtg.hom.HomomorphismSymbol.Type.VARIABLE;
import de.up.ling.irtg.signature.Signature;
import de.up.ling.irtg.signature.SignatureMapper;
import de.up.ling.irtg.util.Lazy;
import de.up.ling.irtg.util.Logging;
import de.up.ling.tree.Tree;
import de.up.ling.tree.TreeVisitor;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 *
 * @author koller
 */
public class Homomorphism {

    private static final Pattern HOM_NON_QUOTING_PATTERN = Pattern.compile("([a-zA-Z*+_]([a-zA-Z0-9_*+-]*))|([?]([0-9]+))");
    private final Signature srcSignature, tgtSignature;
    private static final boolean debug = false;

    private final List<Tree<HomomorphismSymbol>> terms;    // Maps an ID to each term
    private final Object2IntMap<Tree<HomomorphismSymbol>> termToId; // maps term to ID
    private final Int2IntMap labelToLabelSet; // Find the labelSet for a given label
    private final List<IntSet> labelSetList;  // List of labelSets. Their index is their labelSet OD

    private Object2IntMap<IntSet> labelSetToLabelID;  // maps a label set to its ID; only computed by need
    private boolean labelSetsDirty;                   // is current value of labelSetToLabelID valid?

//    private final Int2IntMap tgtIDToSrcID;    // maps an int that resolves to an string in the tgt signature
    // to an int, that resolves to the coresponding string in the src signature
    private final ListMultimap<Integer, IntSet> srcSymbolToRhsSymbols;

    private SignatureMapper signatureMapper;

    public Homomorphism(Signature src, Signature tgt) {
        srcSignature = src;
        tgtSignature = tgt;

        terms = new ArrayList<Tree<HomomorphismSymbol>>();
        termToId = new Object2IntOpenHashMap<Tree<HomomorphismSymbol>>();
        labelToLabelSet = new Int2IntOpenHashMap();
        labelSetList = new ArrayList<IntSet>();

//        tgtIDToSrcID = new Int2IntOpenHashMap();
        srcSymbolToRhsSymbols = ArrayListMultimap.create();

        terms.add(null);
        labelSetList.add(null); // dummies to ensure that IDs start at 1 (so 0 can be used for "not found")

        labelSetsDirty = true;
        signatureMapper = null;
    }

    public void add(String label, Tree<String> mapping) {
        add(srcSignature.getIdForSymbol(label), HomomorphismSymbol.treeFromNames(mapping, tgtSignature));
    }

    public void add(int label, Tree<HomomorphismSymbol> mapping) {
        int labelSetID;

        labelSetsDirty = true;

        if (termToId.containsKey(mapping)) {
            // Term is already processed. We only need to add the given label to the proper labelSet
            if (debug) {
                System.err.println("-> " + mapping + " is already known.");
            }

            labelSetID = termToId.get(mapping);  // get existing termID
            addToLabelSet(label, labelSetID);           // put the current label in the labelSet for this term.
            labelToLabelSet.put(label, labelSetID);     // Add the mapping from the label to the corresponding labelSet
        } else {
            // This is the first time we see the term 'mapping'

            terms.add(mapping);      // Create an ID for the term from the term interner

            labelSetID = createNewLabelSet(label);      // Create a new labelSet and the ID for it
            termToId.put(mapping, labelSetID);

            labelToLabelSet.put(label, labelSetID);     // Map the used label to its labelSetID
        }

        // Save a link between the label of the current Tree to the ID in the target signature
        // TODO!!! How do we know the original entry is not overwritten
        // by a different RHS term with the same root symbol?
//        int tgtID = HomomorphismSymbol.getHomSymbolToIntFunction().applyInt(mapping.getLabel());
//        tgtIDToSrcID.put(tgtID, labelSetID);
        IntSet allSymbols = new IntOpenHashSet();
        collectAllSymbols(mapping, allSymbols);
        srcSymbolToRhsSymbols.put(labelSetID, allSymbols);
        // TODO - this might be inefficient, use better data structure
    }

    private void collectAllSymbols(Tree<HomomorphismSymbol> tree, IntSet allSymbols) {
        if (tree.getLabel().isConstant()) {
            allSymbols.add(tree.getLabel().getValue());
        }

        for (Tree<HomomorphismSymbol> sub : tree.getChildren()) {
            collectAllSymbols(sub, allSymbols);
        }
    }

    private IntSet getLabelSet(int labelSetID) {
        IntSet ret = labelSetList.get(labelSetID);
        if (ret != null) {
            return ret;
        } else {
            return new IntOpenHashSet();
        }
    }

    // Adds a label to an existing labelSet.
    private void addToLabelSet(int label, int labelSetID) {
        IntSet labelSet = getLabelSet(labelSetID);  // Get the actual labelset
        labelSet.add(label);                        // Now change the content of the set

        if (debug) {
            System.err.println("labelSet = " + labelSet);
        }

        if (debug) {
            System.err.println("labelSet\\ = " + labelSet);
        }
    }

    // Creates a new labelSet for a new label and returns the labelSetID
    private int createNewLabelSet(int label) {
        IntSet labelSet = new IntOpenHashSet();
        labelSet.add(label);            // put first element in set
        labelSetList.add(labelSet);     // add set to the list
        int labelSetID = labelSetList.size() - 1; // = the position in the list

        if (debug) {
            System.err.println("labelSetID = " + labelSetID);
        }

        return labelSetID;
    }

    /**
     * Returns the value h(label), using symbol IDs.
     *
     * @param label
     * @return
     */
    public Tree<HomomorphismSymbol> get(int label) {
        if (debug) {
            System.err.println("Getting mapping for " + label);
        }

        int termID = labelToLabelSet.get(label);

        if (termID == 0) {
            return null;
        } else {
            return terms.get(termID);
        }
    }

    public Tree<HomomorphismSymbol> getByLabelSetID(int labelSetID) {
        return terms.get(labelSetID);
    }

    public IntSet getLabelSetByLabelSetID(int labelSetID) {
        assert labelSetID < labelSetList.size();
        return labelSetList.get(labelSetID);
    }

    private void ensureCleanLabelSets() {
        if (labelSetsDirty) {
            labelSetToLabelID.clear();

            for (int i = 1; i < labelSetList.size(); i++) {
                labelSetToLabelID.put(labelSetList.get(i), i);
            }

            labelSetsDirty = false;
        }
    }

    public int getLabelSetIDByLabelSet(IntSet labelSet) {
        ensureCleanLabelSets();
        return labelSetToLabelID.getInt(labelSet);
    }

    public int getTermID(int label) {
        return getLabelSetID(label);
    }

    public int getLabelSetID(int label) {
        return labelToLabelSet.get(label);
    }

//    public int getTermIDByLabelSet(int labelSetID) {
//        return labelSetID;
//    }
    public IntSet getLabelSetForLabel(int label) {
        return getLabelSet(labelToLabelSet.get(label));
    }

    /**
     * Returns the value h(label). The label is resolved according to the
     * homomorphism's source signature, and is expected to be known there. The
     * labels in the returned tree are elements of the homomorphism's target
     * signature. If necessary, the returned tree can be converted back to a
     * tree of HomomorphismSymbols using HomomorphismSymbo.treeFromNames and the
     * homomorphism's target signature.
     *
     * @param label
     * @return
     */
    public Tree<String> get(String label) {
        if (debug) {
            System.err.println("Getting for " + label);
        }
        return HomomorphismSymbol.toStringTree(get(srcSignature.getIdForSymbol(label)), tgtSignature);
    }

    public IntSet getLabelsetIDsForTgtSymbols(IntSet tgtSymbols) {
        IntSet ret = new IntOpenHashSet();

//        Logging.get().fine("tgt->src: " + tgtIDToSrcID);
        Logging.get().fine("tgt sig: " + getTargetSignature());
        Logging.get().fine("src sig: " + getSourceSignature());
        Logging.get().fine("labelset IDs: " + labelSetList);
//        for (int tgtSymbol : tgtSymbols) {
//            ret.add(tgtIDToSrcID.get(tgtSymbol));
//        }
//        
//        // labelSetIDs start at 1. If ret contains a 0, then that was
//        // because one of the tgtSymbols was not mentioned in any RHS
//        // of the homomorphism, and it should thus be removed.
//        ret.remove(0);
//        Logging.get().fine("gLT, tgt = " + tgtSymbols);

        outer:
        for (int srcSymbol : srcSymbolToRhsSymbols.keySet()) {
//            Logging.get().fine("  consider " + srcSymbol + " = " + srcSignature.resolveSymbolId(srcSymbol) + "; srcsym=" + srcSymbolToRhsSymbols.get(srcSymbol));

            for (IntSet tgtSymSet : srcSymbolToRhsSymbols.get(srcSymbol)) {
                if (tgtSymbols.containsAll(tgtSymSet)) {
//                    Logging.get().fine("  " + tgtSymSet + " contained in " + tgtSymbols);
                    ret.add(srcSymbol);
                    continue outer;
                } else {
//                    Logging.get().fine("  " + tgtSymSet + " not contained in " + tgtSymbols);
                }
            }
        }

        return ret;
    }

    /*
     * Applies the homomorphism to the given tree. Returns the homomorphic image
     * of the tree under this homomorphism.
     *
     */
    public Tree<Integer> applyRaw(Tree<Integer> tree) {
//        final Map<String, String> knownGensyms = new HashMap<String, String>();

        return tree.dfs(new TreeVisitor<Integer, Void, Tree<Integer>>() {
            @Override
            public Tree<Integer> combine(Tree<Integer> node, List<Tree<Integer>> childrenValues) {
                Tree<Integer> ret = constructRaw(get(node.getLabel()), childrenValues);
                if (debug) {
                    System.err.println("\n" + node + ":");
                    System.err.println("  " + rhsAsString(get(node.getLabel())));
                    for (Tree<Integer> child : childrenValues) {
                        System.err.println("   + " + child);
                    }
                    System.err.println("  => " + ret);
                }
                return ret;
            }
        });
    }

    public Tree<String> apply(Tree<String> tree) {
        return getTargetSignature().resolve(applyRaw(getSourceSignature().addAllSymbols(tree)));
    }

    /**
     * Applies the homomorphism to a given input tree. Variables are substituted
     * according to the "subtrees" parameter: ?1, ?x1 etc. refer to the first
     * entry in the list, and so on.
     *
     * @param tree
     * @param subtrees
     * @param knownGensyms
     * @return
     */
    private Tree<Integer> constructRaw(final Tree<HomomorphismSymbol> tree, final List<Tree<Integer>> subtrees) {
        final Tree<Integer> ret = tree.dfs(new TreeVisitor<HomomorphismSymbol, Void, Tree<Integer>>() {
            @Override
            public Tree<Integer> combine(Tree<HomomorphismSymbol> node, List<Tree<Integer>> childrenValues) {
                HomomorphismSymbol label = node.getLabel();

                switch (label.getType()) {
                    case VARIABLE:
                        return subtrees.get(label.getValue());
                    case CONSTANT:
                        return Tree.create(label.getValue(), childrenValues);
                    default:
                        throw new RuntimeException("undefined homomorphism symbol type");
                }
            }
        });

        return ret;
    }

//    public Tree<String> construct(Tree<String> tree, List<Tree<)

    /*
     private String gensym(String gensymString, Map<String, String> knownGensyms) {
     int start = gensymString.indexOf("+");
     String prefix = gensymString.substring(0, start);
     String gensymKey = gensymString.substring(start);

     if (!knownGensyms.containsKey(gensymKey)) {
     knownGensyms.put(gensymKey, "_" + (gensymNext++));
     }

     return prefix + knownGensyms.get(gensymKey);
     }
     */
    public String toStringCondensed() {
        StringBuilder buf = new StringBuilder();
        buf.append("Labelsets mapped to terms in Homomorphism:\n");

        for (int labelSetID = 1; labelSetID < labelSetList.size(); labelSetID++) {
            StringBuilder labelSetStrings = new StringBuilder();
            labelSetStrings.append(labelSetID);
            labelSetStrings.append(":{");
            for (int label : getLabelSetByLabelSetID(labelSetID)) {
                labelSetStrings.append(srcSignature.resolveSymbolId(label)).append(",");
            }
            labelSetStrings.setLength(labelSetStrings.length() - 1);
            buf.append(labelSetStrings.toString()).append("} -> ").append(rhsAsString(getByLabelSetID(labelSetID))).append("\n");
        }

        return buf.toString();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (int key : labelToLabelSet.keySet()) {
            buf.append(srcSignature.resolveSymbolId(key)).append(" -> ").append(rhsAsString(get(key))).append("\n");
        }

        return buf.toString();
    }

    public String rhsAsString(Tree<HomomorphismSymbol> t) {
        Tree<String> resolvedTree = HomomorphismSymbol.toStringTree(t, tgtSignature);
//
//
//         resolvedTree = t.dfs(new TreeVisitor<HomomorphismSymbol, Void, Tree<String>>() {
//            @Override
//            public Tree<String> combine(Tree<HomomorphismSymbol> node, List<Tree<String>> childrenValues) {
//                switch(node.getLabel().getType()) {
//                    case CONSTANT:
//                        return Tree.create(tgtSignature.resolveSymbolId(node.getLabel().getValue()), childrenValues);
//                    case VARIABLE:
//                        return Tree.create("?" + (node.getLabel().getValue()+1));
//                    default:
//                        return Tree.create("***");
//                }
//            }
//        });
//
        resolvedTree.setCachingPolicy(false);

        try {
            return resolvedTree.toString(HOM_NON_QUOTING_PATTERN);
        } catch (Exception e) {
            return null;
        }
    }

    public Signature getSourceSignature() {
        return srcSignature;
    }

    public Signature getTargetSignature() {
        return tgtSignature;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Homomorphism) {
            Homomorphism other = (Homomorphism) obj;

            int[] sourceRemap = srcSignature.remap(other.srcSignature);
            int[] targetRemap = tgtSignature.remap(other.tgtSignature);

            if (labelToLabelSet.size() != other.labelToLabelSet.size()) {
                return false;
            }

            for (int srcSym : labelToLabelSet.keySet()) {
                if (sourceRemap[srcSym] == 0) {
                    return false;
                }

                Tree<HomomorphismSymbol> thisRhs = get(srcSym);
                Tree<HomomorphismSymbol> otherRhs = other.get(sourceRemap[srcSym]);

                if (!equalRhsTrees(thisRhs, otherRhs, targetRemap)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean equalRhsTrees(Tree<HomomorphismSymbol> thisRhs, Tree<HomomorphismSymbol> otherRhs, int[] targetRemap) {
        if (thisRhs.getLabel().getType() != otherRhs.getLabel().getType()) {
            return false;
        }

        switch (thisRhs.getLabel().getType()) {
            case CONSTANT:
                if (targetRemap[thisRhs.getLabel().getValue()] != otherRhs.getLabel().getValue()) {
                    return false;
                }
                break;

            case VARIABLE:
                if (thisRhs.getLabel().getValue() != otherRhs.getLabel().getValue()) {
                    return false;
                }
        }

        if (thisRhs.getChildren().size() != otherRhs.getChildren().size()) {
            return false;
        }

        for (int i = 0; i < thisRhs.getChildren().size(); i++) {
            if (!equalRhsTrees(thisRhs.getChildren().get(i), otherRhs.getChildren().get(i), targetRemap)) {
                return false;
            }
        }

        return true;
    }

    private Lazy<Boolean> nondeleting = new Lazy(new Supplier<Boolean>() {
        @Override
        public Boolean get() {
            for (int label : labelToLabelSet.keySet()) {
                Tree<HomomorphismSymbol> rhs = Homomorphism.this.get(label);
                Set<HomomorphismSymbol> variables = new HashSet<HomomorphismSymbol>();
                for (HomomorphismSymbol l : rhs.getLeafLabels()) {
                    if (l.isVariable()) {
                        variables.add(l);
                    }
                }

                if (variables.size() < srcSignature.getArity((int) label)) {
                    return false;
                }
            }

            return true;
        }

    });

    public boolean isNonDeleting() {
        return nondeleting.getValue();
    }

    /**
     * Returns a mapper that translates symbols of the source signature into
     * symbols of the target signature (and back). The mapper is computed by
     * need, when this method is called for the first time, and then reused.
     * Note that if one of the underlying signatures changes, you need to call
     * {@link SignatureMapper#recompute() } to update the mapper.
     *
     * @return
     */
    public SignatureMapper getSignatureMapper() {
        if (signatureMapper == null) {
            signatureMapper = new SignatureMapper(srcSignature, tgtSignature);
        }

        return signatureMapper;
    }
}
