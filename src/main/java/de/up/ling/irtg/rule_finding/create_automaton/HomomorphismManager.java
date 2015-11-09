/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.rule_finding.create_automaton;

import de.up.ling.irtg.automata.ConcreteTreeAutomaton;
import de.up.ling.irtg.automata.Rule;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.hom.Homomorphism;
import de.up.ling.irtg.hom.HomomorphismSymbol;
import de.up.ling.irtg.rule_finding.Variables;
import de.up.ling.irtg.signature.Signature;
import de.up.ling.irtg.util.BooleanArrayIterator;
import de.up.ling.irtg.util.NChooseK;
import de.up.ling.irtg.util.SingletonIterator;
import de.up.ling.tree.Tree;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author christoph_teichmann
 */
public class HomomorphismManager {
    /**
     * 
     */
    private static final boolean[] RE_USE_FOR_SHARED = new boolean[] {true,true};
    
    /**
     * 
     */
    private final Signature source1;
    
    /**
     * 
     */
    private final Signature source2;
    
    /**
     * 
     */
    private final IntSet seen1;
    
    /**
     * 
     */
    private final IntSet seen2;
    
    /**
     * 
     */
    private final Homomorphism hom1;
    
    /**
     * 
     */
    private final Homomorphism hom2;

    /**
     * 
     */
    private final Signature sharedSig;
    
    /**
     * 
     */
    private int[] terminationSequence;

    /**
     * 
     */
    private final IntList symbols;

    /**
     * 
     */
    private final IntList insertionPoints;

    /**
     * 
     */
    private final BooleanList isJustInsert;
    
    /**
     * 
     */
    private final RestrictionManager rm;
    
    /**
     * 
     */
    private final IntSet seenAll1;
    
    /**
     * 
     */
    private final IntSet seenAll2;

    /**
     * 
     */
    private final IntSet mapsToVariable;
    
    /**
     * 
     * @param source1
     * @param source2 
     * @param shared 
     */
    public HomomorphismManager(Signature source1, Signature source2, Signature shared){
        this.mapsToVariable = new IntOpenHashSet();
        this.symbols = new IntArrayList();
        this.isJustInsert = new BooleanArrayList();
        this.insertionPoints = new IntArrayList();
        this.sharedSig = shared;
        this.source1 = source1;
        this.source2 = source2;
        this.seen1 = new IntAVLTreeSet();
        this.seen2 = new IntAVLTreeSet();
        this.seenAll1 = new IntAVLTreeSet();
        this.seenAll2 = new IntAVLTreeSet();
        
        this.hom1 = new Homomorphism(sharedSig, source1);
        this.hom2 = new Homomorphism(sharedSig, source2);
        
        this.terminationSequence = null;
        
        this.rm = new RestrictionManager(this.sharedSig);
    }
    
    /**
     * 
     * @param source1
     * @param source2 
     */
    public HomomorphismManager(Signature source1, Signature source2){
        this(source1,source2, new Signature());
    }

    /**
     * 
     * @param k
     * @return 
     */
    public boolean isVariable(int k){
        return this.mapsToVariable.contains(k);
    }
    
    /**
     * 
     * 
     * @return 
     */
    public Homomorphism getHomomorphism1(){
        return this.hom1;
    }
    
    /**
     * 
     * @return 
     */
    public Homomorphism getHomomorphism2(){
        return this.hom2;
    }
    
    /**
     * 
     * @param toDo1
     * @param toDo2 
     */
    public void update(IntSet toDo1, IntSet toDo2){
       toDo1 = new IntAVLTreeSet(toDo1);
       toDo2 = new IntAVLTreeSet(toDo2);
        
       if(this.terminationSequence == null){
           this.terminationSequence = new int[2];
           
           Integer def = findDefault(this.source1,toDo1);
           if(def == null){
                throw new IllegalStateException("We have no 0-ary symbol for signature "+1);
           }
           this.terminationSequence[0] = def;
           
           def = findDefault(this.source2,toDo2);
           if(def == null){
                throw new IllegalStateException("We have no 0-ary symbol for signature "+2);
           }
           this.terminationSequence[1] = def;
           
           ensureTermination();
       }
       
       makeVariables(toDo1,toDo2);
         
       toDo1.removeAll(this.seenAll1);
       IntIterator iit = toDo1.iterator();
       while(iit.hasNext()){
           int symName = iit.nextInt();
           
           int arity = this.source1.getArity(symName);
           
            if(arity == 0){
                handle0Ary(0,symName);
            }else if(!isVariable(0,symName)){
                handleSym(0,symName);                  
                this.seen1.add(symName);
            }
       }
       
       toDo2.removeAll(this.seenAll2);
       iit = toDo2.iterator();
       while(iit.hasNext()){
           int symName = iit.nextInt();
           int arity = this.source2.getArity(symName);
           
            if(arity == 0){
                handle0Ary(1,symName);
            }else if(!isVariable(1,symName)){
                handleSym(1,symName);  
                this.seen2.add(symName);
            }
       }
       
       this.seenAll1.addAll(toDo1);
       this.seenAll2.addAll(toDo2);
    }

    /**
     * Finds a 0-ary symbol in the given set according to the given signature.
     * 
     * @param sig
     * @param toDo
     * @return 
     */
    private Integer findDefault(Signature sig, IntSet toDo) {
        IntIterator iit = toDo.iterator();
        
        while(iit.hasNext()){
            int sym = iit.nextInt();
            
            if(sig.getArity(sym) == 0){
                return sym;
            }
        }
        
        return null;
    }

    /**
     * 
     */
    private void ensureTermination() {
        this.symbols.clear();
        this.insertionPoints.clear();
        this.isJustInsert.clear();
        
        for(int sym : this.terminationSequence){
            symbols.add(sym);
            isJustInsert.add(false);
        }
        
        addMapping(symbols,insertionPoints,isJustInsert, 0);
    }

    /**
     * 
     * @param sigNum
     * @param symName 
     */
    private void handle0Ary(int sigNum, int symName) {
        this.isJustInsert.clear();
        this.symbols.clear();
        this.insertionPoints.clear();
        
        for(int i=0;i<2;++i){
            if(i == sigNum){
                this.isJustInsert.add(false);
                this.symbols.add(symName);
            }else{
                this.isJustInsert.add(true);
                this.symbols.add(-1);
                this.insertionPoints.add(0);
            }
        }
        
        this.addMapping(symbols, insertionPoints, isJustInsert, 1);
    }

    /**
     * 
     * @param sigNum
     * @param symName 
     */
    private void handleSym(int sigNum, int symName) {
        makeSingular(sigNum,symName);
        
        IntIterator iit = (sigNum == 0 ? this.seen2 : this.seen1).iterator();
               
        while(iit.hasNext()){
            int other = iit.nextInt();
            int lSym = (sigNum == 0 ? symName : other);
            int rSym = (sigNum == 1 ? symName : other);
            
            makePairing(lSym,rSym);
        }
    }

    /**
     * 
     * @param symbols
     * @param variables
     * @param justVariable
     * @return the newly created symbol
     */
    private String addMapping(IntList symbols, IntList variables, BooleanList justVariable,
            int totalVariables) {
        StringBuilder symbol = new StringBuilder();
        
        int varPos = 0;
        for(int i=0;i<symbols.size();++i){
            if(i != 0){
                    symbol.append(" / ");
            }
            
            if(justVariable.getBoolean(i)){
                symbol.append('x').append(variables.getInt(varPos++)+1);
            }else{
                int varNum = (i == 0 ? this.source1 : this.source2).getArity(symbols.getInt(i));
                String sym  = (i == 0 ? this.source1 : this.source2).resolveSymbolId(symbols.getInt(i));
                
                if(sym == null){
                    throw new IllegalArgumentException("symbol must be known to given signatures");
                }
                
                symbol.append(sym).append('(');
                for(int k=0;k < varNum; ++k){
                    if(k != 0){
                        symbol.append(", ");
                    }
                    
                    symbol.append("x").append(variables.getInt(varPos++)+1);
                }
                symbol.append(')');
            }
        }
        symbol.append(" | ").append(totalVariables);
        
        String sym = symbol.toString();
        varPos = 0;
        
        if(sharedSig.contains(sym)){
            return sym;
        }
        
        this.sharedSig.addSymbol(sym, totalVariables);
        
        ObjectArrayList<Tree<String>> storage = new ObjectArrayList<>();
        for(int i=0;i<symbols.size();++i){
            storage.clear();
            Tree<String> t;
            
            if(justVariable.getBoolean(i)){
                t = Tree.create("?"+(variables.getInt(varPos++)+1));
            }
            else{
                int varNum = (i == 0 ? this.source1 : this.source2).getArity(this.symbols.getInt(i));
                String label = (i == 0 ? this.source1 : this.source2).resolveSymbolId(this.symbols.getInt(i));
                
                for(int k=0;k < varNum; ++k){
                    storage.add(Tree.create("?"+(variables.getInt(varPos++)+1)));
                }
                
                t = Tree.create(label, storage);
            }
            
            (i == 0 ? this.hom1 : this.hom2).add(sym, t);
        }
        
        this.rm.addSymbol(sym, this.hom1.get(sym), this.hom2.get(sym));
        
        return sym;
    }
    
    /**
     * 
     * @return 
     */
    public TreeAutomaton getRestriction(){
        return this.rm.getRestriction();
    }

    /**
     * 
     * @param sigNum
     * @param symName
     * @return 
     */
    private boolean isVariable(int sigNum, int symName) {
        String s = (sigNum == 0 ? this.source1 : this.source2).resolveSymbolId(symName);
        
        return Variables.IS_VARIABLE.test(s);
    }

    
    /**
     * 
     * @param sigNum
     * @param symName 
     */
    private int handleVariablePair(int symName1, int symName2) {
        this.isJustInsert.clear();
        this.symbols.clear();
        this.insertionPoints.clear();
        
        this.isJustInsert.add(false);
        this.isJustInsert.add(false);
        this.insertionPoints.add(0);
        this.insertionPoints.add(0);
        this.symbols.add(symName1);
        this.symbols.add(symName2);
        
        int k = this.sharedSig.getIdForSymbol(this.addMapping(symbols, insertionPoints, isJustInsert, 1));
        this.mapsToVariable.add(k);
        return k;
    }

    /**
     * 
     * @param sigNum
     * @param symName 
     */
    private void makeSingular(int sigNum, int symName) {
        int numVars = (sigNum == 0 ? this.source1.getArity(symName) : this.source2.getArity(symName));
        
        this.isJustInsert.clear();
        this.symbols.clear();
        this.insertionPoints.clear();
        int varPos = -1;
        
        for(int i=0;i<2;++i){
            if(i == sigNum){
                this.isJustInsert.add(false);
                this.symbols.add(symName);
                for(int k=0;k<numVars;++k){
                    this.insertionPoints.add(k);
                }
            }else{
                this.isJustInsert.add(true);
                this.symbols.add(-1);
                varPos = this.insertionPoints.size();
            }
        }
        
        for(int i=0;i<numVars;++i){
            if(i == 0){
                this.insertionPoints.add(varPos, i);
            }else{
                this.insertionPoints.set(varPos, i);
            }
            this.addMapping(symbols, insertionPoints, isJustInsert, numVars);
        }
        
        this.insertionPoints.set(varPos,numVars);
        this.addMapping(symbols, insertionPoints, isJustInsert, numVars+1);
    }

    /**
     * 
     * @param lSym
     * @param rSym 
     */
    private void makePairing(int lSym, int rSym) {
        int lArity = this.source1.getArity(lSym);
        int rArity = this.source2.getArity(rSym);
        
        int less = Math.min(lArity, rArity);
        if(less == 1){
            return;
        }
        int more = Math.max(lArity, rArity);
        
        this.isJustInsert.clear();
        this.isJustInsert.add(false);
        this.isJustInsert.add(false);
        this.symbols.clear();
        this.symbols.add(lSym);
        this.symbols.add(rSym);
        
        // this set tells us which elements we can pair with elements that do
        // not share a variable
        // note that we need to write no more than less elements into this, since
        // no more will be paired
        IntSortedSet iss = new IntAVLTreeSet();
        
        Iterator<boolean[]> bai;
        if(less == 2){
            bai = new SingletonIterator<>(RE_USE_FOR_SHARED);
        }else{
            bai = new BooleanArrayIterator(less);
        }
        
        int all = (more+less);
        for(int i=more;i<all;++i){
            iss.add(i);
        }
        
        while(bai.hasNext()){
            boolean[] bs = bai.next();
            
            int size = less == 2 ? 2 : findNumberTrue(bs);
            if(size < 2){
                continue;
            }
            
            for (int[] share : new NChooseK(size, more)) {
                int actuallyUsed = more;
                removeAll(iss, share);
                
                IntIterator ibi = iss.iterator();
                this.insertionPoints.clear();
                int posInShared = 0;
                
                if(less == rArity){
                    for(int i=0;i<lArity;++i){
                        this.insertionPoints.add(i);
                    }
                    
                    for(int i=0;i<rArity;++i){
                        if(bs[i]){
                            int var = share[posInShared++];
                            this.insertionPoints.add(var);
                        }else{
                            int var = ibi.nextInt();
                            this.insertionPoints.add(var);
                            ++actuallyUsed;
                        }
                    }
                    
                    this.addMapping(symbols, insertionPoints, isJustInsert, actuallyUsed);
                }else{
                    for(int i=0;i<lArity;++i){
                        if(bs[i]){
                            int var = share[posInShared++];
                            this.insertionPoints.add(var);
                        }else{
                            int var = ibi.nextInt();
                            ++actuallyUsed;
                            this.insertionPoints.add(var);
                        }
                    }
                    
                    for(int i=0;i<rArity;++i){
                        this.insertionPoints.add(i);
                    }
                    
                    
                    this.addMapping(symbols, insertionPoints, isJustInsert, actuallyUsed);
                }
                    
                addAll(iss, share);
            }
        }
    }

    /**
     * 
     * @param s
     * @param share 
     */
    private void removeAll(IntSortedSet s, int[] share) {
        for(int i : share){
            s.rem(i);
        }
    }

    /**
     * 
     * @param s
     * @param share 
     */
    private void addAll(IntSortedSet s, int[] share) {
        for(int i : share){
            s.add(i);
        }
    }

    /**
     * 
     * @param bs
     * @return 
     */
    private int findNumberTrue(boolean[] bs) {
        int ret = 0;
        for(boolean b : bs){
            ret += b ? 1 : 0;
        }
        return ret;
    }

    /**
     * 
     * @return 
     */
    public RestrictionManager getRestrictionManager() {
        return rm;
    }
    
    /**
    *
     * @return 
    */
    public Signature getSignature(){
       return this.sharedSig;
    }

    /**
     * 
     * @param allLabelsHere
     * @param allLabelsThere
     * @return 
     */
    public Homomorphism getHomomorphismRestriction1(IntSet allLabelsHere, IntSet allLabelsThere) {
        Homomorphism here = this.hom1;
        Homomorphism there = this.hom2;
        Signature source = this.source1;
        
        return makeShadow(source, here, allLabelsHere, there, allLabelsThere);
    }

    /**
     * 
     * @param source
     * @param here
     * @param allLabelsHere
     * @param there
     * @param allLabelsThere
     * @return 
     */
    private Homomorphism makeShadow(Signature source, Homomorphism here,
            IntSet allLabelsHere, Homomorphism there, IntSet allLabelsThere) {
        Homomorphism ret = new Homomorphism(this.sharedSig, source);
        
        for(int i=1;i<=this.sharedSig.getMaxSymbolId();++i){
            if(this.sharedSig.getArity(i) == 0){
                ret.add(i, here.get(i));
                continue;
            }
            
            Tree<HomomorphismSymbol> t = here.get(i);
            
            if(!(t.getLabel().isVariable() || allLabelsHere.contains(t.getLabel().getValue()))){
                continue;
            }
            
            t = there.get(i);
            if(!(t.getLabel().isVariable() || allLabelsThere.contains(t.getLabel().getValue()))){
                continue;
            }
            
            ret.add(i, here.get(i));
        }
        
        return ret;
    }

    /**
     * 
     * @param allLabelsHere
     * @param allLabelsThere
     * @return 
     */
    public Homomorphism getHomomorphismRestriction2(IntSet allLabelsHere, IntSet allLabelsThere) {
        Homomorphism here = this.hom2;
        Homomorphism there = this.hom1;
        Signature source = this.source2;
        
        return makeShadow(source, here, allLabelsHere, there, allLabelsThere);
    }

    /**
     * 
     * @param done
     * @return 
     */
    public TreeAutomaton reduceToOriginalVariablePairs(TreeAutomaton done) {
        ConcreteTreeAutomaton cta = new ConcreteTreeAutomaton(done.getSignature());
        
        IntSet seen = new IntOpenHashSet(done.getFinalStates());
        IntList toDo = new IntArrayList(done.getFinalStates());
        
        List<Object> children = new ArrayList<>();
        
        for(int i=0;i<toDo.size();++i){
            int org = toDo.getInt(i);
            Object state = done.getStateForId(org);
            int stan = cta.addState(state);
            
            if(done.getFinalStates().contains(org)){
                cta.addFinalState(stan);
            }
            
            Iterator<Rule> rules = done.getRulesTopDown(org).iterator();
            while(rules.hasNext()){
                children.clear();
                Rule rul = rules.next();
                String label = makeReducedLabel(rul.getLabel());
                for(int child : rul.getChildren()){
                    children.add(done.getStateForId(org));
                    
                    if(!seen.contains(child)){
                        seen.add(child);
                        toDo.add(child);
                    }
                }
                
                cta.addRule(cta.createRule(state, label, children, rul.getWeight()));
            }
        }
        
        return cta;
    }

    /**
     * 
     * @param toDo1
     * @param toDo2 
     */
    private void makeVariables(IntSet toDo1, IntSet toDo2) {
        Object2ObjectMap<String,IntList> alignMap1 = new Object2ObjectOpenHashMap<>();
        IntIterator iit = toDo1.iterator();
        
        while(iit.hasNext()){
            int sym = iit.nextInt();
            String name = this.source1.resolveSymbolId(sym);
            
            String align = Propagator.getAlignments(name);
            IntList list = alignMap1.get(align);
            if(list == null){
                list = new IntArrayList();
                alignMap1.put(align, list);
            }
            
            list.add(sym);
        }
        
        iit = toDo2.iterator();
        while(iit.hasNext()){
            int sym = iit.nextInt();
            String name = this.source2.resolveSymbolId(sym);
            
            String align = Propagator.getAlignments(name);
            IntList options = alignMap1.get(align);
            
            for(int i=0;i<options.size();++i){
                int osym = options.get(i);
                
                this.handleVariablePair(osym, sym);
            }
        }
    }

    /**
     * 
     * @param label
     * @return 
     */
    public String makeReducedLabel(int label) {
        if(!this.isVariable(label)){
            return this.sharedSig.resolveSymbolId(label);
        }
        
        String varName = this.sharedSig.resolveSymbolId(label);
        String[] leftRight = varName.split("|")[0].split("/");
        leftRight[0] = leftRight[0].trim();
        leftRight[1] = leftRight[1].trim();
        
        String left = Propagator.getOriginalVariable(leftRight[0]);
        String right = Propagator.getOriginalVariable(leftRight[1]);
        
        int l = this.source1.addSymbol(left, 1);
        int r = this.source2.addSymbol(right, 1);
        
        int sym = this.handleVariablePair(l, r);
        
        return this.sharedSig.resolveSymbolId(sym);
    }
}