/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.maxent;

import de.up.ling.irtg.automata.Rule;
import de.saar.basic.Pair;

/**
 *
 * @author koller
 */
public abstract class FeatureFunction<State> {
    public abstract double evaluate(Rule<State> object);

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    protected State getLabelFor(Object state) {
        if (state instanceof Pair) {
            Pair s = (Pair) state;
            if (s.left instanceof Pair) {
                return this.getLabelFor((Pair) s.left);
            }
            return (State) s.left;
        }
        return (State) state;
    }

    protected String masking(String s) {
        if (s.contains("'")) {
            return "\"" + s + "\"";
        }
        return "'" + s + "'";
    }
}