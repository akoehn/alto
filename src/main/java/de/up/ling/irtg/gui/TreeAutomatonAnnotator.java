/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.gui;

import de.up.ling.irtg.automata.Rule;
import java.util.Collection;

/**
 *
 * @author koller
 */
public interface TreeAutomatonAnnotator {
    Collection<String> getAnnotationIdentifiers();
    String getAnnotation(Rule rule, String annotationIdentifier);
}
