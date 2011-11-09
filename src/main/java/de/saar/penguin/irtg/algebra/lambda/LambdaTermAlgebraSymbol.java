/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.saar.penguin.irtg.algebra.lambda;

/**
 *
 * @author Anna Melzer
 */
public class LambdaTermAlgebraSymbol {
        public String type;
        public LambdaTerm content;
        
        public static LambdaTermAlgebraSymbol functor(){
            LambdaTermAlgebraSymbol ret = new LambdaTermAlgebraSymbol();
            ret.type = "FUNCTOR";
            return ret;

        }

        public static LambdaTermAlgebraSymbol lterm(LambdaTerm content){
            LambdaTermAlgebraSymbol ret = new LambdaTermAlgebraSymbol();
            ret.type = "LTERM";
            ret.content = content;
            return ret;
        }

        @Override
        public String toString(){
            if(this.type.equals("FUNCTOR")){
                return "<";
            }
            else{
                return content.getTree().toString();
            }
        }

}