/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.up.ling.irtg.algebra;

import de.up.ling.tree.Tree;

/**
 *
 * @author koller
 */
public class RightMbotAlgebra extends TupleAlgebra<Tree<String>> {
    public RightMbotAlgebra() {
        super(new TreeAlgebra());
    }
    
}
