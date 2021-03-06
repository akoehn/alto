/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.util;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author jonas
 */
public class IntSetTupleIterator implements Iterator{
    
    private final int length;
    private final IntSet[] setTuple;
    private final int[] currentValues;
    private final IntIterator[] iterators;
    private final boolean isEmpty;
    
    public IntSetTupleIterator(IntSet[] setTuple) {
        this.setTuple = setTuple;
        length = setTuple.length;
        currentValues = new int[length];
        iterators = new IntIterator[length];
        
        boolean tempIsEmpty = false;
        for (int i = 0; i<length; i++) {
            IntSet set = setTuple[i];
            iterators[i] = set.iterator();
            
            if (i != 0 && iterators[i].hasNext()) {
                currentValues[i] = iterators[i].nextInt();// need i != 0 to get proper behavior when first next() is called.
            }
            if (set.isEmpty()) {
                tempIsEmpty = true;
            }
        }
        isEmpty = tempIsEmpty;
    }
    
    @Override
    public boolean hasNext() {
        boolean ret = false;
        for (IntIterator it : iterators) {
            if (it.hasNext()) {
                ret = true;
            }
        }
        return ret && !isEmpty;
    }
    
    
    @Override
    public int[] next() {
        
        for (int i = 0; i < iterators.length; i++) {
            IntIterator it = iterators[i];
            if (it.hasNext()) {
                currentValues[i] = it.nextInt();
                break;
            } else {
                it = setTuple[i].iterator();
                iterators[i] = it;
                currentValues[i] = it.nextInt();
            }
        }
        
        return Arrays.copyOf(currentValues, currentValues.length);
    }
    
    /*private int[] getCurrent() {
        int[] ret = new int[curPos.length];
        for (int i = 0; i<curPos.length; i++) {
            ret[i] = arrayTuple[i][curPos[i]];
        }
        return ret;
    }*/
            
    
}
