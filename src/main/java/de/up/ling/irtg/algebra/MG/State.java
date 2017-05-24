/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

/**
 * The state of a derivation as defined in Kobele et al 2007
 * @author meaghanfowlie
 */
public class State {
    private FeatureList[] state;
    
    /**
     * Class constructor for a lexical item
     * @param li the LI
     * @param g  the grammar
     */
    public State(Lex li, MG g) {
        // makes a state from a lexical item
        
        // initialise the state to the right length
        this.state = new FeatureList[g.licSize()+1];
        // copy the faetures
        
        // the first element of the state is the features of the lixical item
        this.state[0] = li.getFeatures().copy();
       
    }
    
    /**
     * makes an empty state of correct length based on the grammar
     * @param g 
     */
    public State(MG g) {
        // initialise the state to the right length
        this.state = new FeatureList[g.licSize()+1];
    }    
    
    /**
     * makes an empty state of length <code>n</code>
     * @param n length of state
     */
    public State(int n) {
        this.state = new FeatureList[n];
    }
    
    /**
     * make a state from an expression. Probably don't need this.
     * @param e
     * @param g 
     */
    public State(Expression e, MG g) {
        
        FeatureList[] st;
        int n = g.licSize() +1; // length of array
        st = new FeatureList[n];
        int i = 0;
        while (i<n) {
            st[i] = e.getExpression()[i].getFeatures();
            i++;
        }
        this.state = st;
        
    }
    
    
    public boolean addMover(int i, FeatureList mover) {
        if (this.state[i] == null) { // only add a selected if there's room (SMC)
            this.state[i] = mover;
            return true;
        } else {
            return false;
        }
    }
    
    public Feature head() {
        if (this.state[0].getFeatures().isEmpty()) {
            return null;
        } else {
        // the head feature
        return this.state[0].getFeatures().get(0);
        }
    }

    public Feature head(int i) {
        if (this.state[i].getFeatures().isEmpty()) {
            return null;
        } 
        return this.state[i].getFeatures().get(0);
    }
            
    public FeatureList[] getState() {
        return state;
    }
    
    public int headFeatureIndex(MG g) {
        if (head().getSet().equals("sel")) {
            return g.getBareSelFeatures().indexOf(head().getValue());
        } else {
            return g.getBareLicFeatures().indexOf(head().getValue());
        }
    }
    
   public boolean isComplete(MG g) {
       boolean valid = g.getFinals().contains(this.head().getValue()) && this.state[0].getFeatures().size()==1;
       int i = 1;
       while (i < g.licSize()+1) {
           if (this.state[i] != null) {
               return false;
           }
           i++;
       }
       return valid;
        
      
   }
   
   public void check(int i) {   
       // remove the top feature of the ith element of the state
        this.state[i].getFeatures().remove(0);       
    }
    
 
   public State copy() {
       
       int n = this.state.length;
       //System.out.println(n);
       State cp = new State(n);
       
       int i = 0;
       while (i < n) {
           //System.out.println(i);
           //System.out.println(state[i]);
           if (this.state[i] != null) { // only add it if it's not null
               cp.state[i] = this.state[i].copy();
           }
           i++;
       }

       //cp.state = this.state.clone();
       
       return cp;

   } 
    public boolean equals(State st2) {
        int n = this.state.length;
        //System.out.println(n);
        if (n != st2.state.length) {
            return false;
        }
        int i = 0;
        while (i < n) {
           //System.out.println(i);
            //System.out.println(state[i]);
            if (this.state[i] == null && st2.state[i] == null){
                i++;
            } else if 
                 (this.state[i] == null || st2.state[i] == null) {
                    return false;
                } else if (!this.state[i].equals(st2.state[i])) { 
                    return false;
            } else {
                i++;
            }
        }
        return true;

    }

    @Override
    public String toString() {
        String s = "";
        int i = 0;
        while (i < this.state.length) {
            if (state[i] == null) {
                s += "[]";
            } else {
                s += "[" + state[i] + "]";
            }
            s += " ";
            i++;
        }
        return s;
    }

        /**
     * Stores a mover in position <code>i</code> in the expression, if it's empty.
     * If it's not empty it's an SMC violation.
     * @param li the mover to be stored
     * @param i the index of the place it goes
     * @return true if it worked, false if SMC violation
     */
    public boolean store(FeatureList mover, int i) { // store in position i if it's empty
        if (this.state[i] == null) {
            this.state[i] = mover;
            return true;
        } else {
            //System.out.println("SMC violation: feature slot " + i + " is already filled");
            return false;
            
        }
    }
    
    /**
     * Stores a mover in its rightful place, if it's empty.
     * The mover belongs at the index corresponding to the <code>number</code> of its head feature
     * @param li
     * @return true if it worked; false if SMC violation
     */
    public boolean store(FeatureList mover, MG g) {
        int i = mover.headFeatureIndex(g) +1;
        return this.store(mover,i);
    }

    
     public State merge(State state2, MG g) {
        // merge a non-mover
        State newState;

        newState = this.copy();
        State selected = state2.copy();

        if (newState == null || selected == null) {
            return null;
        }

        if (newState.head().getSet().equals("sel") && this.head().match(selected.head())) {

            // check features
            selected.check(0);
            newState.check(0);

            // combine selected lists
            int i = 1;
            boolean ok = true;
            while (i < g.licSize() + 1 && ok) {
                ok = newState.addMover(i, selected.getState()[i]);
                i++;

            }
            if (!ok) { // SMC violation
                return null;
            } else {
                if (selected.head() == null) { // merge 1
                    //System.out.println("merge 1 gets you " + newState);
                    return newState;
                } else {

                    if (selected.head().isMove()) {
                        State mover = selected.copy();
                        if (newState.addMover(mover.headFeatureIndex(g) +1, mover.getState()[0])) {
                            //System.out.println("merge 2 gets you " + newState);
                            return newState;
                        } else {
                            return null;
                        }

                    }
                }
            }

        } 

            return null; // merge doesn't apply
        

    }

//    public State merge2(State state2, MG g) {
//        // merge a mover
//        
//        // check the features and mege the mover lists
//        State newState = this.merge1(state2, g);
//
//        //copy the selectee and add it to the mover list
//        State mover = state2.copy();        
//        newState.addMover(mover.headFeatureIndex(g), mover.getState()[0]);
//
//        
//        return newState;
//        
//    }
//   
    
    
    public State adjoin(State state2, MG g) {

        // make copies
        State newState = this.copy();
        State adjunct = state2.copy();
        
        if (newState == null || adjunct == null) {
            return null;
        }
        
        // get the category of the adjunct
        Category adjunctCat = g.getCategories().get(adjunct.head().getValue());
        //System.out.println(adjunct);
        
        // if Adjoin even applies
        if (newState.head().getSet().equals("sel") // it's a selectional feature
                && adjunct.head().getSet().equals("sel") // adjunct head is also selectional
                && adjunctCat.getAdjunctOf().contains(newState.head().getValue())) { // adjunct is an adjunct of newState
            //check adjunct feature
            adjunct.check(0);

            // combine mover lists
            int i = 1;
            boolean ok = true;
            while (i < g.licSize() + 1 && ok) {
                ok = newState.addMover(i, adjunct.getState()[i]);
                i++;

            }
            if (!ok) {
                return null; // SMC violation
            } else {

                if (!adjunct.state[0].features.isEmpty()) { // if adjunct is moving
                    if (!newState.addMover(adjunct.headFeatureIndex(g) + 1, adjunct.state[0])) {
                        return null; // SMC violation
                    }

                }
            }

        } else { // if adjoin doesn't apply

            return null;
        }
        //System.out.println("adjoin gets you " + newState);
        return newState;
    }


 
    public State move(MG g) {
        // move and stop

        State newState = this.copy();
        int i = newState.headFeatureIndex(g) +1; // mover #

        if (newState == null) {
            return null;
        }

        if (newState.getState()[i] != null) { // if there's a mover

            if (this.head().licensing(g) && this.head().match(this.head(i))) { // if the feature are right

                //check the features
                
                newState.check(0);
                newState.check(i);
                FeatureList mover = newState.state[i].copy();
                newState.getState()[i] = null; // take the mover out of the list
                if (!mover.features.isEmpty()) {
                    System.out.println("state: " + newState);
                    System.out.println("adding back in " + mover);
                    System.out.println("at index " + (mover.headFeatureIndex(g) +1));
                    System.out.println(newState.addMover(mover.headFeatureIndex(g) +1, mover)); // add back into the mover list
                    System.out.println("resulting in " + newState);
                }
                //System.out.println("applying move");
                return newState;
            } else {
                return null;        // failed b.c no such mover
            }
            //System.out.println("move1 outputs " + newState);
            
        }
        return null;
    }

//    public State move2(MG g) {
//        // check the features
//        
//        int i = this.headFeatureIndex(g);
//        FeatureList mover = this.state[i].check(); // get the mover and check the features
//        
//        State newState = this.move1(g);
//        newState.addMover(mover.headFeatureIndex(g) , mover); // add back into the mover list
//        
//        return newState;
//        
//        
//        
//    }
//    
    public boolean moving(int element,MG g) {
//        System.out.println("Trying moving...");
//        System.out.println("number: " + element);
//        System.out.println("state: " + this.toString());

        FeatureList fs = this.state[element];
//        System.out.println("Features: " +  fs);        
//        System.out.println(fs.getFeatures());
        if ( fs.getFeatures().size()>1)  {
            return  fs.getFeatures().get(1).licensing(g); //fs.getFeatures().get(1).getNumber();
        } else { // if it's not a mover
            return false; //-1;
        }
        
    }
    
 
    
    
}
