/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.rule_finding.sampling;

import de.up.ling.irtg.util.LogSpaceOperations;
import de.up.ling.tree.Tree;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author christoph
 * @param <Type>
 */
public class TreeSample<Type> {
    /**
     * 
     */
    private final List<Tree<Type>> samplesDrawn = new ArrayList<>();
    
    /**
     * 
     */
    private final DoubleArrayList sampleWeights = new DoubleArrayList();
    
    /**
     * 
     * @param sample
     * @param weight 
     */
    public void addSample(Tree<Type> sample, double weight) {
        this.samplesDrawn.add(sample);
        this.sampleWeights.add(weight);
    }
    
    /**
     * 
     * @param enty
     * @param amount 
     */
    public void addWeight(int enty, double amount) {
        this.sampleWeights.add(enty, amount);
    }
    
    /**
     * 
     * @param entry
     * @param amount 
     */
    public void multiplyWeight(int entry, double amount) {
        this.sampleWeights.set(entry, this.sampleWeights.get(entry)*amount);
    }
    
    /**
     * 
     * @param entry
     * @return 
     */
    public double getWeight(int entry) {
        return this.sampleWeights.get(entry);
    }
    
    /**
     * 
     * @param entry
     * @return 
     */
    public Tree<Type> getSample(int entry) {
        return this.samplesDrawn.get(entry);
    }
    
    /**
     * 
     */
    public void expoNormalize(){
        double sum = 0.0;
        
        for(int i=0;i<this.sampleWeights.size();++i) {
            sum = LogSpaceOperations.addAlmostZero(sum, this.sampleWeights.getDouble(i));
        }
        
        for(int i=0;i<this.sampleWeights.size();++i) {
            this.sampleWeights.set(i, FastMath.exp(this.sampleWeights.get(i)-sum));
        }
    }

    /**
     * 
     * @return 
     */
    public int populationSize() {
        return this.sampleWeights.size();
    }
    
    /**
     * 
     * @param rg 
     */
    public void resampleWithNormalize(RandomGenerator rg) {
        this.expoNormalize();
        this.resample(rg);
    }
    
    /**
     * 
     * @param rg 
     */
    public void resample(RandomGenerator rg) {
        int size = this.populationSize();
        double dsize = (double) size;
        
        List<Tree<Type>> newPop = new ArrayList<>();
        DoubleList newWeights = new DoubleArrayList();
        
        double sum = 0.0;
        for(int i=0;i<size;++i) {
            double amount = roundDown(size*this.getWeight(i)) / dsize;
            
            if(amount > 0.0) {
                newPop.add(this.getSample(i));
                newWeights.add(amount);
                
                sum += amount;
            }
        }
        
        double frac = 1.0 / dsize;
        while(sum < 1.0) {
            double d = rg.nextDouble();
            boolean done = false;
            
            for(int i=0;(i<size && (!done));++i) {
                d -= this.getWeight(i);
                
                if(d <= 0.0) {
                    done = true;
                    
                    newPop.add(this.getSample(i));
                    newWeights.add(frac);
                }
            }
        }
        
        this.samplesDrawn.clear();
        this.samplesDrawn.addAll(newPop);
        
        this.sampleWeights.clear();
        this.sampleWeights.addAll(newWeights);
    }

    /**
     * 
     * @param weight
     * @return 
     */
    private double roundDown(double weight) {
        if(weight >= 0.0) {
            return Math.floor(weight);
        } else {
            return Math.ceil(weight);
        }
    }
}
