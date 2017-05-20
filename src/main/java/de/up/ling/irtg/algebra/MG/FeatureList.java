/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author meaghanfowlie
 */
public class FeatureList {
    // implements lists of features so we can do everything we want to them. Lists are "genereic" and this preculdes some operations, but embedding the list in an object makes them work.
    // Also, now we have a dedicated function for copying feature lists.

    
    ArrayList<Feature> features;
    
    public FeatureList() {
        features = new ArrayList<>();
        
    }
    
    public FeatureList(Feature[] featureArray) {
        
        this.features = new ArrayList<>(Arrays.asList(featureArray));
        
    }
    
    public FeatureList(List<Feature> featureList) {
        this.features = new ArrayList<>(featureList);
        
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }
    
    public void addFeature(Feature f) {
        this.features.add(f);
    }
    
    public FeatureList copy() {
    
        ArrayList<Feature> fs = new ArrayList<>();
        for (Feature f : this.features) {
            fs.add(f);
        }
        return new FeatureList(fs);

    }
    /**
     * Deletes the top feature from the stack.
     * @return the feature stack with the top feature removed
     */
    public FeatureList check() {
        FeatureList checked = this.copy();
        checked.features.remove(0);
        return checked;
    }

    /**
     * Checks if the given feature list is a suffix.
     * @param suf potential suffix
     * @return true if <code>suf</code> is a suffix
     */
    public boolean suffix(FeatureList suf) {
        int len = features.size();
        boolean result;
        if (len < suf.getFeatures().size()) {
            result = false;
        } else {
            //System.out.println(features.subList(len - suf.getFeatures().size(), len));
            //System.out.println(suf.getFeatures());
            result = features.subList(len - suf.getFeatures().size(), len).equals(suf.getFeatures());
        }
        return result;
    }
    
    public int headFeatureIndex(MG g) {
        if (features.get(0).getSet().equals("sel")) {
            return g.getBareSelFeatures().indexOf(features.get(0).getValue());
        } else {
            return g.getBareLicFeatures().indexOf(features.get(0).getValue());
        }
    }
    
    @Override
    public String toString() {
        String s = "";
        for (Feature f : this.features) {
            s = s + f + " " ;
        }
        return s.substring(0,s.length()-1);
    }
    
    
}
