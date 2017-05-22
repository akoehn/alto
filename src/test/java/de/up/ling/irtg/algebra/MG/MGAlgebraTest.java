/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.MG;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author meaghanfowlie
 */
public class MGAlgebraTest {
    
    AtomicInteger ai = new AtomicInteger(0);    
    
    @Before
    public void setUp() {
        ai.incrementAndGet();
        System.out.println(ai.get());
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of usesTopDownAutomaton method, of class MGAlgebra.
     */
    @Test
    public void testUsesTopDownAutomaton() {
        
        assertTrue(4+1==5);
        assertEquals(4+1,5);
        
    }

    /**
     * Test of setUseTopDownAutomaton method, of class MGAlgebra.
     */
    @Test
    public void testSetUseTopDownAutomaton() {
        System.out.println("setUseTopDownAutomaton");
        boolean useTopDownAutomaton = false;
        MGAlgebra instance = new MGAlgebra();
        instance.setUseTopDownAutomaton(useTopDownAutomaton);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllConstantLabelInterpretations method, of class MGAlgebra.
     */
    @Test
    public void testGetAllConstantLabelInterpretations() {
        System.out.println("getAllConstantLabelInterpretations");
        MGAlgebra instance = new MGAlgebra();
        Int2ObjectMap<Expression> expResult = null;
        Int2ObjectMap<Expression> result = instance.getAllConstantLabelInterpretations();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of evaluate method, of class MGAlgebra.
     */
    @Test
    public void testEvaluate() {
        System.out.println("evaluate");
        String label = "";
        List<Expression> childrenValues = null;
        MGAlgebra instance = new MGAlgebra();
        Expression expResult = null;
        Expression result = instance.evaluate(label, childrenValues);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isValidValue method, of class MGAlgebra.
     */
    @Test
    public void testIsValidValue() {
        System.out.println("isValidValue");
        Expression value = null;
        MGAlgebra instance = new MGAlgebra();
        boolean expResult = false;
        boolean result = instance.isValidValue(value);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parseString method, of class MGAlgebra.
     */
    @Test
    public void testParseString() throws Exception {
        System.out.println("parseString");
        String representation = "";
        MGAlgebra instance = new MGAlgebra();
        Expression expResult = null;
        Expression result = instance.parseString(representation);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
