/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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
    private ArrayList<String> bareSelFeatures;
    private HashMap<String,Polarity> licPolarities;
    private HashMap<String,Polarity> selPolarities;

    //private ArrayList<Polarity> licPolarities;
    //private ArrayList<Polarity> selPolarities;
    private ArrayList<Feature> features;
    private ArrayList<String> alphabet;
    private ArrayList<Lex> lexicon;
    private ArrayList<String> finals; // final categories
    private Map<String,Category> categories;
    


    /**
     * Class constructor.
     */
    public MG() {
        this.bareSelFeatures = new ArrayList<>();
        this.bareLicFeatures = new ArrayList<>();
        this.licPolarities = new HashMap<>();
        this.selPolarities = new HashMap<>();
        //this.licPolarities = new ArrayList<>();
        //this.selPolarities = new ArrayList<>();
        this.features = new ArrayList<>();
        this.alphabet = new ArrayList<>();
        this.lexicon = new ArrayList<>();
        this.finals = new ArrayList<>();
        this.categories = new HashMap<>();
    
        
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

    public ArrayList<String> getBareSelFeatures() {
        return bareSelFeatures;
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
                if (!this.bareSelFeatures.contains(feature)) {
                    this.bareSelFeatures.add(feature);
                    this.categories.put(feature, new Category(feature));
                }
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
        //selectional features
        if (!this.features.contains(feature) && feature.getSet().equals("sel") && feature.getNumber() <= selSize() + 1) {
            this.features.add(feature);
            if (!bareSelFeatures.contains(feature.getValue())) {
                this.bareSelFeatures.add(feature.getValue());
            }
            //System.out.println("sel feature added: " + feature);
            
        } else if (!this.features.contains(feature) && feature.getSet().equals("lic") && feature.getNumber() <= licSize() + 1) { 
            //licensing features
            this.features.add(feature);
            if (!bareLicFeatures.contains(feature.getValue())) {
                this.bareLicFeatures.add(feature.getValue());
            }
            //System.out.println("lic feature added: " + feature);

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
        return this.bareSelFeatures.size();
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
        int number = 1; // lic features get numbers
        for (String f : this.bareLicFeatures) {
            for (Polarity pol : this.getLicPolarities().values()) {
                newfs.add(new Feature(pol,f,number));
             
            }
            number++;
        }
        
        // selectional features
        number = 1; // what the heck, let's give sel feaures numbers too just in case that's useful
        for (String c : this.bareSelFeatures) {
            
            for (Polarity pol : this.getSelPolarities().values()) {
                newfs.add(new Feature(pol,c,number));
                
            }
            number++;
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
            Polarity polarity;
            polarity = null;
            String name;
            name=null;
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
                int n = -1;
                if (polarity.getSet().equals("lic")) {
                    n = licSize() + 1;
                    for (Polarity p : licPolarities.values()) {
                        addFeature(new Feature(p, name, n));
                    }
                    feature = new Feature(polarity, name, n);
                } else {
                    n = selSize() + 1;
                    for (Polarity p : selPolarities.values()) {
                        addFeature(new Feature(p, name, n));
                    }
                }
                feature = new Feature(polarity, name, n);

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
                ",\n bareSelFeatures = " + bareSelFeatures + 
                ",\n bareLicFeatures = " + bareLicFeatures + 
                ",\n features = " + features + 
                ",\n categories = " + categories.values() + 
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
                if (!result.store(expr2.head())) { // SMC violation
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
            Integer i = head.getNumber();
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
                        
                        if (!result.store(mover)) { //SMC violation
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
                if (!result.store(expr2.head())) { // SMC violation
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
    
    
    
    
}