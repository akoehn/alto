/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.featstruct;

import de.up.ling.irtg.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An empty feature structure that has no data in it.
 * Placeholder feature structures are useful when multiple
 * placeholders with the same index are used in different places,
 * enforcing reentrancy.
 * 
 * @author koller
 */
public class PlaceholderFeatureStructure extends FeatureStructure {
    public PlaceholderFeatureStructure(String index) {
        setIndex(index);
    }
    
    
    
    private static final List<String> EMPTY_PATH = new ArrayList<>();
    
    @Override
    public List<List<String>> getAllPaths() {
        return Arrays.asList(EMPTY_PATH);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    protected FeatureStructure get(List<String> path, int pos) {
        if (pos == path.size()) {
            return this;
        } else {
            return null;
        }
    }

    
    @Override
    protected int calculateHashCode() {
        return getIndexMarker().hashCode();
    }
    
    @Override
    protected void forAllChildren(Consumer<FeatureStructure> fn) {
    }


    
    /***************************************************************************
     * Tomabechi unification
     **************************************************************************/
    
    @Override
    protected FeatureStructure makeCopyWithCompArcs(long currentTimestamp) {
        FeatureStructure ret = new PlaceholderFeatureStructure(getIndex());
        setCopy(ret, currentTimestamp);
        return ret;
    }
    
    
    /***************************************************************************
     * Subsumption checking
     **************************************************************************/
    
    @Override
    protected int checkSubsumptionValues(FeatureStructure other, long timestamp, int resultSoFar) {
        return resultSoFar;
    }
    
    
    /***************************************************************************
     * Printing
     **************************************************************************/
    
    @Override
    protected void appendValue(Set<FeatureStructure> visitedIndexedFs, boolean printedIndexMarker, Map<FeatureStructure,String> reentrantFsToIndex, StringBuilder buf) {
        // don't print anything -- we just printed the #index before calling this method
//        buf.append("PH" + getIndexMarker());
    }

    @Override
    protected void appendRawToString(StringBuilder buf, int indent) {
        String prefix = Util.repeat(" ", indent);
        
        int id = findPreviousId();
        if( id > -1 ) {
            buf.append(String.format("%splaceholder #%d\n", prefix, id));
        } else {
            id = makeId();
            buf.append(String.format("%splaceholder #%d, index=%s\n", prefix, id, getIndex()));
            appendForwardAndCopy(buf, indent);
        }
    }


    
}
