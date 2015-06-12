/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.up.ling.irtg.codec;

import de.up.ling.irtg.algebra.graph.IsiAmrParser;
import de.up.ling.irtg.algebra.graph.SGraph;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads an AMR in the style of the ISI AMR-banks. An example for
 * an AMR in this format is:
 * 
 * <code>(b / blink-01<br/>
 *     :ARG0 (i / i)<br/>
 *     :ARG1 (e / eye<br/>
 *           :part-of i)<br/>
 *     :manner (h / hard))</code>
 * 
 * 
 * @author koller
 */
@CodecMetadata(name = "isi-amr", description = "ISI-style AMRs", type = SGraph.class)
public class IsiAmrInputCodec extends InputCodec<SGraph> {
    @Override
    public SGraph read(InputStream is) throws ParseException, IOException {
        try {
            return IsiAmrParser.parse(new InputStreamReader(is));
        } catch (de.up.ling.irtg.algebra.graph.ParseException ex) {
            throw new ParseException(ex);
        }
    }
}