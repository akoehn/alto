/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.script;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.Algebra;
import de.up.ling.irtg.algebra.BinarizingTagTreeAlgebra;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.algebra.TagStringAlgebra;
import de.up.ling.irtg.binarization.BinarizingAlgebraSeed;
import de.up.ling.irtg.binarization.BinaryRuleFactory;
import de.up.ling.irtg.binarization.BkvBinarizer;
import de.up.ling.irtg.binarization.IdentitySeed;
import de.up.ling.irtg.binarization.RegularSeed;
import de.up.ling.irtg.codec.tag.ChenTagInputCodec;
import de.up.ling.irtg.codec.tag.ElementaryTree;
import de.up.ling.irtg.codec.tag.TagGrammar;
import de.up.ling.irtg.corpus.AbstractCorpusWriter;
import de.up.ling.irtg.corpus.Corpus;
import de.up.ling.irtg.corpus.CorpusWriter;
import de.up.ling.irtg.corpus.Instance;
import de.up.ling.irtg.util.GuiUtils;
import de.up.ling.tree.Tree;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author koller
 */
public class ChenTagTreebankConverter {
    private static JCommander jc;
    private static InterpretedTreeAutomaton irtg;

    public static void main(String[] args) throws FileNotFoundException, IOException, ParserException, Exception {
        CmdLineParameters param = new CmdLineParameters();
        jc = new JCommander(param, args);

        if (param.help) {
            usage(null);
        }

        if (param.inputFiles.isEmpty()) {
            usage("No input files specified.");
        }

        ChenTagInputCodec ic = new ChenTagInputCodec();
        TagGrammar tagg = ic.readUnlexicalizedGrammar(new FileReader(param.inGrammarFilename));
        List<Tree<String>> rawDerivationTrees = ic.lexicalizeFromCorpus(tagg, new FileReader(param.inputFiles.get(0))); // TODO multiple input corpora

        PrintWriter pw = new PrintWriter("tagg.txt");
        pw.println(tagg);
        pw.flush();
        pw.close();

        pw = new PrintWriter("lexicalized-tagg.txt");
        pw.println("\n\n");
        for (String word : tagg.getWords()) {
            pw.println("\nword: " + word + "\n==================\n");
            for (ElementaryTree et : tagg.lexicalizeElementaryTrees(word)) {
                pw.println("   " + et);
            }
        }
        pw.flush();
        pw.close();

        // convert TAG grammar to IRTG
        tagg.setTracePredicate(s -> s.contains("-NONE-"));
        irtg = tagg.toIrtg();
        
//        pw = new PrintWriter("xxxx.irtg");
//        pw.println(irtg);
//        pw.flush();
//        pw.close();
        
        // convert raw derivation trees into ones for this IRTG
        System.err.println("\nMaximum likelihood estimation ...");
        Corpus corpus = new Corpus();
        for( Tree<String> dt : rawDerivationTrees ) {
            Instance inst = new Instance();
            inst.setDerivationTree(irtg.getAutomaton().getSignature().mapSymbolsToIds(dt));
//            System.err.println(dt);
//            System.err.println("  -> " + inst.getDerivationTree());
        }
        
        // maximum likelihood estimation
        irtg.trainML(corpus);

        // binarize grammar if requested
        if (param.binarize) {
            System.err.println("\nBinarizing IRTG ...");

            Map<String, Algebra> newAlgebras = ImmutableMap.of("string", new TagStringAlgebra(), "tree", new BinarizingTagTreeAlgebra());
            Map<String, RegularSeed> seeds = ImmutableMap.of(
                    "string", new IdentitySeed(irtg.getInterpretation("string").getAlgebra(), newAlgebras.get("string")),
                    "tree", new BinarizingAlgebraSeed(irtg.getInterpretation("tree").getAlgebra(), newAlgebras.get("tree")));

            Function<InterpretedTreeAutomaton, BinaryRuleFactory> rff = PennTreebankConverter.makeRuleFactoryFactory(param.binarizationMode);
            BkvBinarizer binarizer = new BkvBinarizer(seeds, rff);

            InterpretedTreeAutomaton binarized = GuiUtils.withConsoleProgressBar(60, System.out, listener -> {
                                                                             return binarizer.binarize(irtg, newAlgebras, listener);
                                                                         });

            irtg = binarized;
        }

        pw = new PrintWriter(param.outGrammarFilename);
        pw.println(irtg);
        pw.flush();
        pw.close();
    }

    private static class CmdLineParameters {

        @Parameter
        public List<String> inputFiles = new ArrayList<>();

        @Parameter(names = {"--out-corpus", "-oc"}, description = "Filename to which the corpus will be written.")
        public String outCorpusFilename = "out.txt";

        @Parameter(names = {"--out-grammar", "-og"}, description = "Filename to which the grammar will be written.")
        public String outGrammarFilename = "out.irtg";
        
        @Parameter(names = {"--in-grammar", "-ig"}, description = "Filename of the TAG grammar we will read.", required = true)
        public String inGrammarFilename = null;

        @Parameter(names = {"--out-automaton", "-oa"}, description = "Filename to which the tree automaton will be written.")
        public String outAutomatonFilename = "out.auto";
        
        @Parameter(names = {"--lowercase"}, description = "Convert all words to lowercase.")
        public boolean lowercase = false;

//        
//        @Parameter(names = "--strip-annotations", description = "Convert NP-SBJ to NP etc.")
//        public boolean stripAnnotations = false;
//
//        @Parameter(names = "--remove-none", description = "Remove empty elements such as -NONE-.")
//        public boolean removeNone = false;
//
//        @Parameter(names = "--add-top", description = "Add a TOP symbol on top of every parse tree.")
//        public boolean addTop = false;
//
//        @Parameter(names = "--pos", description = "Remove all leaves, yielding strings of POS tags.")
//        public boolean removeLeaves = false;

        @Parameter(names = "--binarize", description = "Binarize the output grammar.")
        public boolean binarize = false;

        @Parameter(names = "--binarization-mode", description = "Binarization mode (complete or xbar).",
                validateWith = PennTreebankConverter.BinarizationStyleValidator.class)
        public String binarizationMode = "complete";

        @Parameter(names = "--verbose", description = "Print some debugging output.")
        public boolean verbose = false;

        @Parameter(names = "--help", help = true, description = "Prints usage information.")
        private boolean help;

        @Parameter(names = "--len", description = "Maximum length of an input")
        public int maxLen = 10000;

        AbstractCorpusWriter corpusWriterFromFilename(String[] args) throws IOException {
            Writer w = new FileWriter(outCorpusFilename);
            String joinedArgs = Joiner.on(" ").join(args);
            AbstractCorpusWriter cw = new CorpusWriter(irtg, "Converted on " + new Date().toString() + "\nArgs = " + joinedArgs, "/// ", w);
            return cw;
        }
    }

    private static void usage(String errorMessage) {
        if (jc != null) {
            if (errorMessage != null) {
                System.out.println("No input files specified.");
            }

            jc.setProgramName("java -cp <alto.jar> de.up.ling.irtg.script.ChenTagTreebankConverter <inputfiles>");
            jc.usage();

            if (errorMessage != null) {
                System.exit(1);
            } else {
                System.exit(0);
            }
        }
    }
}
