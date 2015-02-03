/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.signature;

/**
 * A mapping between the symbol IDs of two interners.<p>
 * 
 * TODO - despite the name, this data structure maps between
 * two interners, not two signatures. We should rename the class
 * at some point.
 *
 * @author koller
 */
public class SignatureMapper {
    protected int[] forward, backward;
    protected Interner input, output;
    
    protected SignatureMapper() {
        
    }

    public SignatureMapper(Interner input, Interner output) {
        this.input = input;
        this.output = output;

        recompute();
    }

    /**
     * Maps a symbolID from the input to the output signature. This means that
     * the symbol with ID "symbolID" in the input signature is the same as the
     * symbol with ID remapForward(symbolID) in the output signature.
     *
     * @param symbolID
     * @return
     */
    public int remapForward(int symbolID) {
        return forward[symbolID];
    }

    /**
     * Maps a symbolID from the output to the input signature. This means that
     * the symbol with ID "symbolID" in the output signature is the same as the
     * symbol with ID remapBackward(symbolID) in the input signature.
     *
     * @param symbolID
     * @return
     */
    public int remapBackward(int symbolID) {
        return backward[symbolID];
    }

    /**
     * Recomputes the mappings. This may become necessary if new symbols were
     * added to one or both signatures since the SignatureMapping was created.
     *
     */
    public void recompute() {
        forward = input.remap(output);
        backward = output.remap(input);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("Forward mappings:\n");
        for (int i = 1; i < forward.length; i++) {
            buf.append("" + i + " -> " + remapForward(i) + " (input=" + input.resolveId(i) + ", output=" + output.resolveId(remapForward(i)) + "\n");
        }

        buf.append("\nBackward mappings:\n");
        for (int i = 1; i < backward.length; i++) {
            buf.append("" + i + " -> " + remapBackward(i) + " (output=" + output.resolveId(i) + ", input=" + input.resolveId(remapBackward(i)) + "\n");
        }

        return buf.toString();
    }

}
