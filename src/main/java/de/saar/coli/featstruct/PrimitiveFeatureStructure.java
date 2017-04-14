/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.featstruct;

import java.util.Set;

/**
 *
 * @author koller
 */
public class PrimitiveFeatureStructure<E> extends FeatureStructure {
    private E value;

    public PrimitiveFeatureStructure(E value) {
        this.value = value;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }

    @Override
    protected void appendValue(Set<FeatureStructure> visitedIndexedFs, StringBuilder buf) {
        buf.append(value.toString());
    }

    @Override
    protected Object getValue(String[] path, int pos) {
        if( pos == path.length ) {
            return value;
        } else {
            return null;
        }
    }
    
}
