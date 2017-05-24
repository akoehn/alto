/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

import java.util.ArrayList;

/**
 *
 * @author meaghanfowlie
 */
public class StateSet {
    // make this a dict of state : op
    private ArrayList<State> states;
    
    public StateSet() {
        this.states = new ArrayList<>();
    }
    
    public boolean contains(State st2) {
        for (State st : this.states) {
            if (st.equals(st2)) {
                System.out.println(st + "same as " + st2);
                return true;
            }
        }
        return false;
    }
    
    public boolean add(State st) {
        if (!this.contains(st)) {
            this.states.add(st);
            return true;
        } else {
            return false;
        }
        
    }
    
    public void remove(State st) {
        this.states.remove(st);
    }

    public ArrayList<State> getStates() {
        return states;
    }
    
    
    
}
