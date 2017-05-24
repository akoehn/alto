/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Implements a string-generating minimalist grammar.
 * Based on Stabler & Keenan 2003, Fowlie 2015 for the adjunction, and unpublished MSs for the implementation of move types. 
 * Grammar has:
 * - unordered adjunction
 * - overt, covert move, copy, delete
 * - merge to the right, move to the left
 * @author meaghanfowlie
 * 
 */
public class MG {
    
    private ArrayList<String> bareLicFeatures;
    private HashMap<String,Polarity> licPolarities;
    private HashMap<String,Polarity> selPolarities;
    private ArrayList<Feature> features;
    private ArrayList<String> alphabet;
    private ArrayList<Lex> lexicon;
    // these two can't be inferred from a lexicon
    private ArrayList<String> finals; // final categories
    private Map<String,Category> categories;
    


    /**
     * Class constructor.
     */
    public MG() {
        this.bareLicFeatures = new ArrayList<>();
        this.licPolarities = new HashMap<>();
        this.selPolarities = new HashMap<>();
        this.features = new ArrayList<>();
        this.alphabet = new ArrayList<>();
        this.lexicon = new ArrayList<>();
        // these two can't be inferred from a lexicon
        this.finals = new ArrayList<>();
        this.categories = new HashMap<>();
    
        
    }

    public MG(ArrayList<Expression> lexicon, boolean fromCodec) {
        this(); // make an empty MG

        if (fromCodec) { // if the expressions are straight from the codec, they have length 1 and all features have number -1.
            // go through the lexicon and find all the features
            for (Expression expr : lexicon) { // we only have one LI in the exprssion
                for (Feature f : expr.head().getFeatures().getFeatures()) {
                    addFeature(f); // only adds it if we don't already have it. Also adds to bare features.
                    addPolarity(f.getPolarity()); // only adds it if we don't already have it
 
                }
                this.lexicon.add(expr.head()); // add it to the lexion
                addWord(expr.head().getString()); // add it to the alphabet.
            }            
 
        }

    }

 
    public MG(ArrayList<Lex> lexicon) {
        this(); // make an empty MG
        this.lexicon = lexicon;

        // go through the lexicon and find all the features
        for (Lex li : lexicon) { // we only have one LI in the exprssion
            for (Feature f : li.getFeatures().getFeatures()) {
                addFeature(f); // only adds it if we don't already have it. Also adds to bare features.
                addPolarity(f.getPolarity()); // only adds it if we don't already have it

            }
        }

    }

    
    
// gets
    
    public HashMap<String, Polarity> getLicPolarities() {
        return licPolarities;
    }

    public HashMap<String, Polarity> getSelPolarities() {
        return selPolarities;
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }

    public ArrayList<String> getBareLicFeatures() {
        return bareLicFeatures;
    }

    // I'm too lazy to refactor all of this.
    public ArrayList<String> getBareSelFeatures() {
        return new ArrayList<>(categories.keySet());
    }
    
    public ArrayList<String> getAlphabet() {
        return alphabet;
    }

    public ArrayList<Lex> getLexicon() {
        return lexicon;
    }

    public ArrayList<String> getFinals() {
        return finals;
    }

    public Map<String,Category> getCategories() {
        return categories;
    }

    
    // adding things
    
    /**
     * Adds a polarity to the grammar
     * @param pol 
     */
    public void addPolarity(Polarity pol) {
        // add the polarity if new
        switch (pol.getSet()) {
            case "lic":
                this.licPolarities.put(pol.getName(), pol);
                break;
            case "sel":
                this.selPolarities.put(pol.getName(), pol);
                break;
        }
    }

    
    /**
     * Adds a <code>feature</code> name to Sel or Lic feature <code>set</code>s.
     * @param feature The feature name
     * @param set sel or lic
     */
    public void addBareFeature(String feature, String set) {
        switch (set) {
            case "lic": 
                if (!this.bareLicFeatures.contains(feature)) {
                    this.bareLicFeatures.add(feature);
                }
                //System.out.println(feature);
                break;
            
            case "sel": 
                if (!this.categories.keySet().contains(feature)) {                   
                    this.categories.put(feature, new Category(feature));
                }

//            case "sel": 
//                if (!this.bareSelFeatures.contains(feature)) {
//                    this.bareSelFeatures.add(feature);
//                    this.categories.put(feature, new Category(feature));
//                }
                //System.out.println(feature);
                break;            
                       
        }
        
    }
    
    /**
     * Adds a feature to the set of final features, and to the bare features if necessary.
     * @param feature 
     */
    public void addFinal(String feature) {
        this.finals.add(feature);
        this.addBareFeature(feature, "sel");
        
    }

    /**
     * Adds a <code>feature</code> to the grammar.
     * @param feature Of class <code>Feature</code>
     */
    private void addFeature(Feature feature) {
        if (!this.features.contains(feature)) {
            features.add(feature); // add the feature
            addBareFeature(feature.getValue(),feature.getSet()); // add the bare feature if necessary
            
        }
        
    }
    
    
    /**
     * Adds an adjunct-adjoinee mapping to the <code>categories</code> of the grammar.
     * Default adjoins to the left
     * @param adjunct
     * @param adjoinedTo 
     */
    public void addAdjunct(String adjunct,String adjoinedTo) {
        this.categories.get(adjunct).addAdjunct(adjoinedTo);
    }
    
    /**
     * Changes the side of the head the adjunct adjoins on.
     * @param adjunct
     * @param left <code>true</code> if adjoins to the left
     */
    public void changeAdjunctSide(String adjunct, boolean left) {
        this.categories.get(adjunct).setLeft(left);
    }
    
    /**
     * Adds a string to the alphabet.
     * @param word 
     */
    public void addWord(String word) {
        if (!this.alphabet.contains(word)) {
            this.alphabet.add(word);
        }
    }
    
    /**
     * Returns the number of licensing features.
     * We need this to know how many mover slots to make in an expression etc.
     * @return 
     */
    public int licSize() {
        // returns number of licensing features in the grammar.
        return this.bareLicFeatures.size();
    }
    
    /**
     * Returns number of selection features in the grammar.
     * @return 
     */
    public int selSize() {
        // returns number of licensing features in the grammar
        return this.categories.size();
    }
    
    
//    public int maxSelNumber() {
//        int k = 1; // find the maximum selectional feature number
//        for (Feature feat : this.features) {
//            int j = feat.getNumber();
//            if (feat.getSet().equals("sel") && j > k) {
//                k = j;
//            }
//        }
//        return k;
//    }
//
//    public int maxLicNumber() {
//        int k = 1; // find the maximum licensing feature number
//        for (Feature feat : this.features) {
//            int j = feat.getNumber();
//            if (feat.getSet().equals("lic") && j > k) {
//                k = j;
//            }
//        }
//        return k;
//    }

    /**
     * Generates all <code>Features</code> based on the bare features and polarities in the grammar.
     * Adds them to the <code>features</code> of the grammar
     */
    public void generateFeatures() {
        // using the polarities and bare features of the grammar, generate the feature set
        ArrayList<Feature> newfs = new ArrayList<>();
        
        // licensing features
        for (String f : this.bareLicFeatures) {
            for (Polarity pol : this.getLicPolarities().values()) {
                newfs.add(new Feature(pol,f));
             
            }
        }       
        // selectional features
        //for (String c : this.bareSelFeatures) {
        for (String c : this.categories.keySet()) {  
        
            for (Polarity pol : this.getSelPolarities().values()) {
                newfs.add(new Feature(pol,c));                
            }
        }
        this.features = newfs;
    }
    
    /**
     * Returns a features based on its index.
     * @param n index
     * @return the feature at that index
     */
    public Feature featureByNumber(int n) {
        return this.getFeatures().get(n);
    } 

    /**
     * Returns a feature based on its polarity and value.
     * @param pol polarity
     * @param value name of feature eg wh
     * @return 
     */
    public Feature featureByValue(Polarity pol, String value) {
        Feature ret = null;
        for (Feature f : this.features) {
            if (f.getValue().equals(value) && f.getPolarity().equals(pol)) {
                ret = f;
            }
        }
        return ret;
        
    }
    /**
     * Adds a lexical item to the lexicon.
     * Features are added by index
     * @param word the string  
     * @param numbers list of indices of features
     * @return the lexical item generated
     */
    public Lex addLexicalItem(String word, Integer[] numbers) { // by index in feature list
        ArrayList<Feature> fs = new ArrayList<>();
        for (int n : numbers) {
            fs.add(featureByNumber(n));    
        }
        Lex li = new Lex(word, new FeatureList(fs));
        this.lexicon.add(li);
        addWord(word);
        return li;
    }
    
    /**
     * Adds a lexical item to the lexicon.
     * Features are listed
     * @param word
     * @param features 
     * @return the lexical item added
     */
    public Lex addLexicalItem(String word, FeatureList features) {
        Lex li = new Lex(word,features);
        this.lexicon.add(li);
        addWord(word);
        return li;
    }
    
    public Lex addLexicalItem(String stringRep) {
        Lex li = generateLexicalItem(stringRep);
        this.lexicon.add(li);
        return li;
    }
    
    /**
     * Converts a string representation of a lexical item to a lexical item (type <code>Lex</code>).
     * @param stringRep format "string with possible spaces::=F +wh =VP C -foc 0acc"
     * A prefix of each feature must be from the string representations of polarities in the grammar.
     * If it's not, it will be assumed that the whole thing is a category feature.
     * The rest is the bare feature. If that feature is not already in the grammar, it is added, including all relevant polarities.
     * @return the lexical item built from the pieces
     */
    public Lex generateLexicalItem(String stringRep) {
        String[] parts = stringRep.split("::"); // split into string and features
        String string = parts[0]; // this is the string of the LI
        String[] fs = parts[1].split(" "); // split the features along spaces
        FeatureList featureList = new FeatureList();
        for (String f : fs) {
            Polarity polarity = null;
            String name = null;
            for (String key : licPolarities.keySet()) {
                if (f.startsWith(key)) {
                    polarity = licPolarities.get(key);
                    name = f.substring(key.length());
                }
            }
            if (polarity == null) { // if we haven't found the feature yet
                for (String key : selPolarities.keySet()) {
                    if (!key.equals("") && f.startsWith(key)) {
                        //System.out.println(f + " starts with " + key );
                        polarity = selPolarities.get(key);
                        name = f.substring(key.length());
                    }
                    if (polarity == null) { // save the category feature for last just to be safe
                        polarity = selPolarities.get("");
                        name = f;
                    }

                }
            }
            Feature feature = featureByValue(polarity, name); // get the feature from the grammar
            if (feature == null) { // if it's not in the grammar, add it
                if (polarity.getSet().equals("lic")) {
                    for (Polarity p : licPolarities.values()) {
                        addFeature(new Feature(p, name));
                    }
                    feature = new Feature(polarity, name);
                } else {
                    for (Polarity p : selPolarities.values()) {
                        addFeature(new Feature(p, name));
                    }
                }
                feature = new Feature(polarity, name);
            }
            featureList.addFeature(feature); // add the feature to the feature list for this LI
        }
        return new Lex(string, featureList); // make an LI

    }


    

    
    
    /**
     * Removes a lexical item from the lexicon
     * @param i index of item to be removed
     */
    public void removeLexicalItem(int i) {
        this.lexicon.remove(i);
    }
    
    @Override
    public String toString() {
        return "MG{" + "\n licPolarities = " + licPolarities.keySet() + 
                ",\n selPolarities = " + selPolarities.keySet() + 
                //",\n bareSelFeatures = " + bareSelFeatures + 
                ",\n bareLicFeatures = " + bareLicFeatures + 
                ",\n categories = " + categories.values() + 
                ",\n features = " + features + 
                ",\n final categories = " + finals + 
                ",\n alphabet = " + alphabet +
                ",\n lexicon = " + lexicon + "\n }";
    }
    
    public void printLexicon() {
        System.out.println("\n** Lexicon **\n");
        int i=0;
        while (i<this.getLexicon().size()) {
            System.out.println(i + ". " + this.getLexicon().get(i));
            i++;
        }
    }
    
    public void printFeatures() {
        System.out.println("\n** Features **\n");
        int i=0;
        while (i<this.features.size()) {
            System.out.println(i + ". " + this.features.get(i));
            i++;
        }
    }
            
    
    
    
    //MERGE
    /**
     * Merges two expressions if their head features match.
     * @param expr1 the selector
     * @param expr2 the selectee
     * @return a new expression with both the expressions together
     */
    public Expression merge(Expression expr1,Expression expr2) {
        
        // make a copy of expr1 where we'll make our new guy
        Expression result = expr1.copy(this);
        // if Merge even applies
        if (result.headFeature().getSet().equals("sel") // it's a selectional feature
                && result.headFeature().match(expr2.headFeature()))  { // features match and result is +ve
            //check features
            result.expression[0].check(); 
            expr2.expression[0].check();
            // merge 1: merge and stay
            if (expr2.getExpression()[0].getFeatures().getFeatures().isEmpty()) {
                // combine strings to the right
                result.getExpression()[0].combine(expr2.getExpression()[0].getString(),false);
              
            // Merge 2: merge a mover    
            } else {
                // if +combine
                if (expr2.headFeature().getPolarity().isCombine()) {
                    //combine string on left (spec)
                    result.getExpression()[0].combine(expr2.getExpression()[0].getString(),false);                    
                }
                
                // if -store, remove string part
                if (!expr2.headFeature().getPolarity().isStore()) {
                    expr2.head().setString("");
                }
                // store whereever it belongs
                if (!result.store(expr2.head(),this)) { // SMC violation
                    return null;
                }
    
            }
            
            // combine mover lists
            result.combineMovers(expr2, this.licSize()+1);
            
            
        } else {
            System.out.println("\n*** Merge error *** : features don't match or head feature isn't a selectional feature");
            return null;
        }
        
        return result;
    }
    
    
    // MOVE
    /**
     * Moves a waiting mover.
     * Based on the head feature, takes the corresponding mover out of storage and (internally) merges it 
     * @param expr an expression
     * @return the expression with Move applied
     */
    public Expression move(Expression expr) {
        Expression result = expr.copy(this);
        
        // if top feature is a poitive licensing feature
        Feature head = result.headFeature();
        if (head.getSet().equals("lic")) {
            Integer i = result.head().headFeatureIndex(this);
            Lex mover = result.getExpression()[i];
            if (mover == null) {
                System.out.println("Move error: no matching mover");
                return null;
            } else {
                // check features
                if (head.match(mover.getFeatures().getFeatures().get(0))) {
                    result.head().check();
                    mover.check();
                    
                    // remove mover
                    result.getExpression()[i] = null;
                    
                    // move 1: move and stay
                    if (mover.getFeatures().getFeatures().isEmpty()) {
                        // combine on left
                        result.getExpression()[0].combine(mover.getString(),true);
                        return result;
 
                    // move 2 : move and keep moving    
                    } else {
                        //combine
                        if (mover.getFeatures().getFeatures().get(0).getPolarity().isCombine()) {
                            result.getExpression()[0].combine(mover.getString(),true);
                        }
                        //store
                        // if -store, remove string part
                        if (!mover.getFeatures().getFeatures().get(0).getPolarity().isStore()) {
                            mover.setString("");
                        }
                        
                        if (!result.store(mover,this)) { //SMC violation
                            return null;
                        }
                        return result; // otherwise, we're good.
                    }
                    
                } else {
                    System.out.println("Move error: features don't match. This shouldn't happen if Move is working correctly.");
                    return null;
                }
                
            }
            
        } else {
            System.out.println("Move error: not a licensing feature");
            return null;
        }
    }
    
 
    //ADJOIN
    /**
     * Adjoins based only on whether the category of the adjoinee is in the set of categories the adjunct is defined to adjoin to.
     * this info is stored in the <code>categories</code>, along with whether we adjoin on the left or right
     * @param expr1 the adjoin-ee
     * @param expr2 the adjunct
     * @return a new expression with the adjunct added
     */
    public Expression adjoin(Expression expr1,Expression expr2) {
        
        // make a copy of expr1 where we'll make our new guy
        Expression result = expr1.copy(this);
        // get the category of the adjunct
        Category adjunct = this.categories.get(expr2.headFeature().getValue());
        //System.out.println(adjunct);
        // if Adjoin even applies
        if (result.headFeature().getSet().equals("sel") // it's a selectional feature
                && expr2.headFeature().getSet().equals("sel") // expr2 head is also selectional
                && adjunct.getAdjunctOf().contains(result.headFeature().getValue()) ){ // expr2 is an adjunct of expr1
            //check adjunct feature
            expr2.expression[0].check();
            // adjoin 1: adjoin and stay
            if (expr2.getExpression()[0].getFeatures().getFeatures().isEmpty()) {
                // combine strings
                result.getExpression()[0].combine(expr2.getExpression()[0].getString(), adjunct.isLeft());
              
            // Merge 2: merge a mover    
            } else {
                // if +combine
                if (expr2.headFeature().getPolarity().isCombine()) {
                    //combine string 
                    result.getExpression()[0].combine(expr2.getExpression()[0].getString(), adjunct.isLeft());                    
                }
                
                // if -store, remove string part
                if (!expr2.headFeature().getPolarity().isStore()) {
                    expr2.head().setString("");
                }
                // store whereever it belongs
                if (!result.store(expr2.head(),this)) { // SMC violation
                    return null;
                }
    
            }
            
            // combine mover lists
            result.combineMovers(expr2, this.licSize()+1);
            
            
        } else {
            System.out.println("\n*** Adjoin error *** : not an adjunct");
            return null;
        }
        
        return result;
    }
    
    
    
    /**
     * Makes a list of IRTG categories for this specific MG. That is, for this
     * specific lexicon.
     *
     * @return
     */
    public Set<String> makeIRTGRules() {

        Set<String> irtgRules = new HashSet<>();
        StateSet sel = new StateSet(); // for selectors
        StateSet cat = new StateSet(); // for categories
        StateSet mv = new StateSet(); // when move will apply
        State result; // to store the result of an operation

        // just get the states of the lexicon. We're building a set so we'll have no duplicates.
        for (Lex li : this.lexicon) {
            State state = new State(li, this); // get the state
            irtgRules.add(state.toString() + " -> " + li.toString()); // add lexical rule to IRTG rules

            if (state.head().selectional(this)) {
                if (state.head().getPolarity().getIntValue() == 1) {
                    sel.add(state);
                } else {
                    cat.add(state);
                }
            }
        }

        // now we compute the closure of the states under the operations
        boolean changed = true; // if we added anything to a set
        Set<State> newStuff = new HashSet<>();

        while (changed) {
            // reset
            changed = false;
            newStuff.clear();
            System.out.println("sel: " + sel.getStates());
            System.out.println("cat: " + cat.getStates());
            System.out.println("mv: " + mv.getStates());
            // try move
            for (State st0 : mv.getStates()) {
                result = st0.move(this);
                //System.out.println("move result " + result);
                if (result != null) {

                    if (result.isComplete(this)) { // start category
                        //System.out.println("**Start!**");
                        if (irtgRules.add("mv:" + result + "! -> " + st0.toString())) {
                            newStuff.add(result);
                        }
                    } else {
                        if (irtgRules.add("mv:" + result + " -> " + st0.toString())) {
                            newStuff.add(result);
                        }
                    }
                }

            }
            // empty mv
            mv = new StateSet();
            // try merge

            for (State st0 : sel.getStates()) {
                // reset
                result = null;

                // try merge with everything in states
                for (State state : cat.getStates()) {
                    //System.out.println("trying to merge " + st0 + state);

                    result = st0.merge(state, this);

                    if (result != null) { // if merge worked
                        if (result.isComplete(this)) { // start category
                            //System.out.println("**Start!**");
                            if (irtgRules.add("mg:" + result + "! -> " + st0.toString() + "," + state.toString())) {
                                //System.out.println("merged: " + result);
                                //System.out.println("added to new: " + 
                                newStuff.add(result);
                            } 
                        } else {

                                if (irtgRules.add("mg:" + result + " -> " + st0.toString() + "," + state.toString())) {
                                    //System.out.println("merged: " + result);
                                    //System.out.println("added to new: " + 
                                    newStuff.add(result);
                                }
                            }
                        }

                    }
                }
            

            for (State st : cat.getStates()) {
                for (State state : cat.getStates()) {
                    //adjoin 
                    result = st.adjoin(state, this);
                    if (result != null) {
                        if (result.isComplete(this)) { // start category
                            //System.out.println("**Start!**");

                            if (irtgRules.add("adj:" + result + "! -> " + st.toString() + "," + state.toString())) {
                                newStuff.add(result);
                            }
                        } else {

                            if (irtgRules.add("adj:" + result + " -> " + st.toString() + "," + state.toString())) {
                                newStuff.add(result);
                            }

                        }
                    }

                }
            }
            // add new stuff to appropriate sets
            for (State s : newStuff) {
                changed = true; // if there's something here, we changed the list.
                // add to the right set of states to continue
                if (s.head().selectional(this)) {
                    if (s.head().getPolarity().getIntValue() == 1) {
                        //System.out.println("adding to sel: " + s);
                        sel.add(s);

                    } else {
                        //System.out.println("adding to cat: " + s);
                        cat.add(s);

                    }
                } else {
                    if (s.head().getPolarity().getIntValue() == 1) {
                        //System.out.println("adding to mv: " + s);
                        mv.add(s);

                    }
                }

            }

        }
        return irtgRules;

    }

    public void printIRTG() {
        Set<String> irtg = makeIRTGRules();
        try {
            File file = new File("mg1.irtg");
            FileWriter writer = new FileWriter(file);
            PrintWriter pw = new PrintWriter(writer);
            pw.println("interpretation expr: de.up.ling.irtg.algebra.graph.MGAlgebra\n");

            for (String rule : irtg) {
                writer.write(rule + "\n");
                writer.write("[expr]\t");

                if (rule.startsWith("mg:")) {
                    pw.println("merge(?1, ?2)");
                } else if (rule.startsWith("mv:")) {
                    pw.println("merge(?1)");
                } else if (rule.startsWith("adj:")) {
                    pw.println("adjoin(?1, ?2)");

                } else { // lexical

                    String[] parts = rule.split("-> ");
                    pw.print(parts[1]);
                }
                pw.println("");
                pw.println("");

            }
            writer.flush();
            writer.close();
            System.out.printf("File is located at %s%n", file.getAbsolutePath());
            
        } catch (IOException e) {
            System.out.println(e);

        }
            

    }


    
}
