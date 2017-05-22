/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

/**
 *
 * @author meaghanfowlie
 */
public class Feature {
    
    private Polarity polarity; // eg +,-,=
    private String value;    // eg wh

    public Feature(Polarity polarity, String value) {
        this.polarity = polarity;
        this.value = value;
        
    }

    public Polarity getPolarity() {
        return polarity;
    }

    public String getValue() {
        return value;
    }
    
    public boolean isMove() {
        
        return  this.polarity.getSet().equals("lic") && this.polarity.getIntValue() == -1;
        
    }
    
    @Override
    public String toString() {
        return polarity+value;
    }

    public boolean licensing(MG g) {
        return g.getLicPolarities().containsValue(this.polarity);
    }
    
    public boolean selectional(MG g) {
        return g.getSelPolarities().containsValue(this.polarity);
    }
  
    public int getIntValue() {
        return this.polarity.getIntValue();
    }
    
    public String getSet() {
        return this.polarity.getSet();
    }
    
    public boolean match(Feature otherFeature) { // two features match if they have the same barefeature and their integer values sum to 0, because one is -1 and the other is +1
        return (this.polarity.getIntValue()==1  // this is a positive feature
                && this.value.equals(otherFeature.value)  // the features are the same
                && otherFeature.getIntValue() == -1) ; // the polarities are opposite
    }
}
