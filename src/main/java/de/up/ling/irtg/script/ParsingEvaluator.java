/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.script;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.Algebra;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.AlgebraStringRepresentationOutputCodec;
import de.up.ling.irtg.codec.InputCodec;
import de.up.ling.irtg.codec.OutputCodec;
import de.up.ling.irtg.corpus.Corpus;
import de.up.ling.irtg.corpus.CorpusReadingException;
import de.up.ling.irtg.corpus.Instance;
import de.up.ling.irtg.util.Util;
import de.up.ling.tree.Tree;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author koller
 */
public class ParsingEvaluator {

    private static JCommander jc;

    public static void main(String[] args) throws IOException, CorpusReadingException, Exception {
        CmdLineParameters param = new CmdLineParameters();
        jc = new JCommander(param, args);

        if (param.help) {
            usage(null);
        }

        if (param.inputFiles.isEmpty()) {
            usage("No input files specified.");
        }

        if (param.grammarName == null) {
            usage("No grammar specified.");
        }

        if (param.inputInterpretations == null) {
            usage("No input interpretations specified.");
        }
        
        InputCodec<InterpretedTreeAutomaton> icGrammar = InputCodec.getInputCodecByNameOrExtension(param.grammarName, null);
        InterpretedTreeAutomaton irtg = icGrammar.read(new FileInputStream(param.grammarName)); // generalize to arbitrary input codecs  InterpretedTreeAutomaton.read(new FileInputStream(param.grammarName));
        List<String> interpretations = Arrays.asList(param.inputInterpretations.split(","));
        List<String> outputInterpretations = new ArrayList<>(param.outputCodecs.keySet());
        String firstInterp = interpretations.get(0); // will be used to display instance
        Algebra firstAlgebra = irtg.getInterpretation(firstInterp).getAlgebra();
        PrintWriter out = new PrintWriter(new FileWriter(param.outCorpusFilename));
        
        Map<String,OutputCodec> ocForInterpretation = new HashMap<>();
        for( String interp : outputInterpretations ) {
            String ocName = param.outputCodecs.get(interp);
            OutputCodec oc = null;
            
            if( "alg".equals(ocName)) {
                Interpretation i = irtg.getInterpretation(interp);
                oc = new AlgebraStringRepresentationOutputCodec(i.getAlgebra());
            } else {
                oc = OutputCodec.getOutputCodecByName(ocName);
            }
            
            if( oc == null ) {
                System.err.println("Could not resolve output codec '" + ocName + "' for interpretation '" + interp + "'.");
                System.exit(1);
            } else {
                ocForInterpretation.put(interp, oc);
            }
        }

        long overallStart = System.nanoTime();
        
        for (String filename : param.inputFiles) {
            Corpus corpus = irtg.readCorpus(new FileReader(filename));
            System.err.println("Processing " + filename + " (" + corpus.getNumberOfInstances() + " instances) ...");
            int width = (int) (Math.ceil(Math.log10(corpus.getNumberOfInstances())));
            String formatString = "%0" + width + "d [%-50.50s] ";
            int pos = 1;

            for (Instance inst : corpus) {
                System.err.printf(formatString, pos++, firstAlgebra.representAsString(inst.getInputObjects().get(firstInterp)));

                long start = System.nanoTime();
                TreeAutomaton chart = irtg.parseInputObjects(inst.getRestrictedInputObjects(interpretations));
                Tree<String> dt = chart.viterbi();
                System.err.println(Util.formatTimeSince(start));

                out.println(dt);

                Map<String, Object> results = irtg.interpret(dt);
                for (String interp : outputInterpretations) {
                    if (dt == null) {
                        out.println("<null>");
                    } else {
                        OutputCodec oc = ocForInterpretation.get(interp);
                                //OutputCodec.getOutputCodecByName(param.outputCodecs.get(interp));
                        out.println(oc.asString(results.get(interp)));
                    }
                }

                if (param.blankLinkes) {
                    out.println();
                }
                
                out.flush();
            }
        }

        out.flush();
        out.close();
        
        System.err.println("Done, total time: " + Util.formatTimeSince(overallStart));
    }

    private static class CmdLineParameters {

        @Parameter
        public List<String> inputFiles = new ArrayList<>();

        @Parameter(names = {"--grammar", "-g"}, description = "IRTG to be used in parsing.")
        public String grammarName = null;

        @Parameter(names = {"--input-interpretations", "-I"}, description = "Comma-separated list of interpretations from which inputs are taken.")
        public String inputInterpretations = null;

        @Parameter(names = {"--out-corpus", "-o"}, description = "Filename to which the parsed corpus will be written.")
        public String outCorpusFilename = "out.txt";

        @DynamicParameter(names = "-O", description = "Output interpretations with their output codecs (e.g. -Ostring=toString). As special case, use -Ostring=alg to use the algebra's default string representation.")
        public Map<String, String> outputCodecs = new HashMap<String, String>();

        @Parameter(names = {"--blank-lines", "-b"}, description = "Insert a blank line between any two output instances.")
        public boolean blankLinkes = false;

        @Parameter(names = "--verbose", description = "Print some debugging output.")
        public boolean verbose = false;

        @Parameter(names = "--help", help = true, description = "Prints usage information.")
        private boolean help;
    }

    private static void usage(String errorMessage) {
        if (jc != null) {
            if (errorMessage != null) {
                System.out.println("No input files specified.");
            }

            jc.setProgramName("java -cp <alto.jar> de.up.ling.irtg.script.ParsingEvaluator <inputfiles>");
            jc.usage();

            if (errorMessage != null) {
                System.exit(1);
            } else {
                System.exit(0);
            }
        }
    }
}
