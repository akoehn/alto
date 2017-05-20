

package de.up.ling.irtg.codec;

import de.up.ling.irtg.algebra.MG.Expression;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 *
 * @author meaghanfowlie
 */
@CodecMetadata(name = "mg-out", description = "encodes an expression as a string", type = Expression.class)
public class MGOutputCodec extends OutputCodec<Expression> {
    @Override
    public void write(Expression expr, OutputStream ostream) throws IOException, UnsupportedOperationException {
        
        
        PrintWriter w = new PrintWriter(new OutputStreamWriter(ostream));
        w.write(expr.toString());
        w.flush();
    }
}
