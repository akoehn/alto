/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.codec;

import de.up.ling.irtg.algebra.MG.Expression;
import de.up.ling.irtg.algebra.MG.Feature;
import de.up.ling.irtg.algebra.MG.FeatureList;
import de.up.ling.irtg.algebra.MG.Lex;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.algebra.MG.MG;
import de.up.ling.irtg.algebra.MG.Polarity;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Input codec corresponding to the default SGraph#toString method.
 * @author groschwitz
 */
public class MGInputCodec extends InputCodec<Expression>{

 
    
    
    @Override
    public Expression read(InputStream is) throws CodecParseException, IOException {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        try {
            return expressionFromString(s.hasNext() ? s.next() : "");
        } catch (ParserException ex) {
            throw new CodecParseException(ex);
        }

    }
    
    /**
     * Generates a unary expression, with all features having number -1.
     * @param stringRep
     * @return expression with one element
     * @throws ParserException 
     */
    private Expression expressionFromString(String stringRep) throws ParserException {
        return new Expression(generateLexicalItem(stringRep), new MG());
    }
    
    /**
     * Converts a string representation of a lexical item to a lexical item (type <code>Lex</code>).
     * @param stringRep format "string with possible spaces::=F +wh =VP C -foc 0acc"
     * A prefix of each feature must be from the string representations of polarities in the grammar.
     * If it's not, it will be assumed that the whole thing is a category feature.
     * The rest is the bare feature. 
     * We give all features number -1, which will be fixed later when we build the grammar.
     * @return the lexical item built from the pieces
     * @throws de.up.ling.irtg.algebra.ParserException if there is no :: between the string and features, or if there are no features
     */
    public Lex generateLexicalItem(String stringRep) throws ParserException {
        
        // we make an MG so we can use the functions that come with it to deal with polarities.
        MG g = new MG();
        
        // polarities. This is where we hard-code the string representations of the polarities; you can change them if you want.
        List<Polarity> pols = Arrays.asList(new Polarity("-","lic",-1),new Polarity("+","lic",+1),new Polarity("","sel",-1),new Polarity("=","sel",+1)
                // special move types
                ,new Polarity("0","lic",-1,true,false,"covert"),new Polarity("(copy)","lic",-1,true,true,"copy"), new Polarity("(del)","lic",-1,true,false,"delete")
        );
        
        // add the polarities to the grammar.
        for (Polarity pol : pols) {
            g.addPolarity(pol);
        }
          
        // now we deal with the string representation of the LI
        String[] parts = stringRep.split("::"); // split into string and features
        if (parts[1] == null) {
            throw new ParserException("No features or no :: separating the string from the features");
        }
        String string = parts[0]; // this is the string of the LI
        String[] fs = parts[1].split(" "); // split the features along spaces
        FeatureList featureList = new FeatureList(); // this will hold the features of the LI.
        for (String f : fs) { // initialise polarity and name of feature.
            Polarity polarity = null;
            String name = null;
            // first look for a licensing feature that matches
            for (String key : g.getLicPolarities().keySet()) {
                if (f.startsWith(key)) {
                    polarity = g.getLicPolarities().get(key);
                    name = f.substring(key.length());
                }
            }
            if (polarity == null) { // if we haven't found the feature yet, look for a positive seletional feature
                for (String key : g.getSelPolarities().keySet()) {
                    if (!key.equals("") && f.startsWith(key)) { // leave the category ("") for last
                        //System.out.println(f + " starts with " + key );
                        polarity = g.getSelPolarities().get(key);
                        name = f.substring(key.length());
                    }
                    if (polarity == null) { // save the category feature for last just to be safe
                        polarity = g.getSelPolarities().get("");
                        name = f;
                    }

                }
            }
 
            // feature number is -1, since we don't have a real grammar. Feature numbers will be fixed when we generate the grammar.
            featureList.addFeature(new Feature(polarity, name, -1)); // add the feature to the feature list for this LI
        }
        return new Lex(string, featureList); // make an LI

    }
}
