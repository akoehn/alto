/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.script;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.saar.basic.Pair;
import de.saar.basic.StringTools;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.TemplateInterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.Algebra;
import de.up.ling.irtg.algebra.SetAlgebra;
import de.up.ling.irtg.algebra.SubsetAlgebra;
import static de.up.ling.irtg.algebra.SubsetAlgebra.SEPARATOR;
import de.up.ling.irtg.automata.IntersectionAutomaton;
import de.up.ling.irtg.automata.Rule;
import de.up.ling.irtg.automata.RuleEvaluatorTopDown;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.automata.condensed.CondensedRule;
import de.up.ling.irtg.automata.pruning.FOM;
import de.up.ling.irtg.codec.TemplateIrtgInputCodec;
import de.up.ling.irtg.semiring.DoubleArithmeticSemiring;
import de.up.ling.irtg.semiring.Semiring;
import de.up.ling.irtg.util.CpuTimeStopwatch;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.irtg.util.Util;
import de.up.ling.tree.Tree;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author koller
 */
public class SurfaceRealizer {

    private static JCommander jc;
    private static CmdLineParameters param = new CmdLineParameters();

    public static void main(String[] args) throws IOException, Exception {
        jc = JCommander.newBuilder().addObject(param).build();

        try {
            jc.parse(args);
        } catch (ParameterException ex) {
            cmdlineUsage(ex.getMessage());
        }

        if (param.help) {
            cmdlineUsage(null);
        }

        if (param.filenames.size() < 2) {
            cmdlineUsage("Please specify both a grammar and a model.");
        }

        FirstOrderModel model = FirstOrderModel.read(new FileReader(param.filenames.get(1)));
        InterpretedTreeAutomaton irtg = null;

        if (param.filenames.get(0).endsWith(".tirtg")) {
            TemplateInterpretedTreeAutomaton tirtg = new TemplateIrtgInputCodec().read(new FileInputStream(args[0]));
            irtg = tirtg.instantiate(model);

            if (param.debugTirtgFilename != null) {
                Files.write(Paths.get(param.debugTirtgFilename), irtg.toString().getBytes());
            }
        } else {
            irtg = InterpretedTreeAutomaton.read(new FileInputStream(param.filenames.get(0)));
        }

        SubsetAlgebra<String> sem = (SubsetAlgebra) irtg.getInterpretation("sem").getAlgebra();
        Interpretation semI = irtg.getInterpretation("sem");

        SetAlgebra ref = (SetAlgebra) irtg.getInterpretation("ref").getAlgebra();
        Interpretation refI = irtg.getInterpretation("ref");

        Algebra str = irtg.getInterpretation("string").getAlgebra();

        // put true facts here
        ref.setModel(model);
        List<String> trueAtoms = Util.mapToList(model.getTrueAtoms(), t -> t.toString());
        sem.setOptions(StringTools.join(trueAtoms, SEPARATOR));

        // put inputs here
        Object refInput = ref.parseString(param.ref);
        BitSet semInput = (BitSet) sem.parseString(param.sem);

        TreeAutomaton<?> chart = null;

        CpuTimeStopwatch totalTime = new CpuTimeStopwatch();
        
        for (int i = 0; i < param.warmupIterations + param.avgIterations; i++) {
            CpuTimeStopwatch sw = new CpuTimeStopwatch();
            sw.record();
            
            // start recording total runtime after warmup iterations
            if( i == param.warmupIterations ) {
                totalTime.record();
                System.err.println("Finished warming up.");
            }

            TreeAutomaton<Pair<String, Set<List<String>>>> afterRef = irtg.getAutomaton().intersect(refI.parse(refInput));

            sw.record();

            afterRef = afterRef.reduceTopDown();
            sw.record();

            Map<Integer, Integer> refDistance = afterRef.evaluateInSemiringTopDown(new DistanceSemiring(), new RuleEvaluatorTopDown<Integer>() {
                                                                               public Integer initialValue() {
                                                                                   return 0;
                                                                               }

                                                                               public Integer evaluateRule(Rule rule, int i) {
                                                                                   return 1;
                                                                               }
                                                                           });

            sw.record();

            /*
            for( int q : refDistance.keySet() ) {
                System.err.printf("dist(%s) = %d\n", afterRef.getStateForId(q), refDistance.get(q));
            }
            
            System.exit(0);
             */
//            Files.write(Paths.get("after-ref.auto"), afterRef.toString().getBytes());
//            System.err.printf("ref done, %s\n", Util.formatTimeSince(start));
            TreeAutomaton<BitSet> invhom = (TreeAutomaton) semI.parse(semInput);
            FOM fom = makeFom(invhom, afterRef, semInput, refDistance, sem);
            IntersectionAutomaton afterSem = new IntersectionAutomaton(afterRef, invhom, fom);
            afterSem.setStopWhenFinalStateFound(true);
            afterSem.makeAllRulesExplicit();

            sw.record();
//            sw.printMilliseconds("ref chart", "ref reduce", "ref distance", "sem chart");

            chart = afterSem;
        }
        
        totalTime.record();
        
        System.err.printf("chart construction time, averaged over %d iterations: %s\n", param.avgIterations, Util.formatTime(totalTime.getTimeBefore(1)/param.avgIterations));

        if (param.printChartFilename != null) {
            Files.write(Paths.get(param.printChartFilename), chart.toString().getBytes());
        }

        System.out.println();

        int count = 1;
        for (Tree<String> dt : chart.languageIterable()) {
            System.out.printf("[%03d] %s\n", count, str.representAsString(irtg.interpret(dt, "string")));
            if (param.printDerivations) {
                System.out.printf("      <deriv = %s>\n\n", dt);
            }

            count++;
        }
    }

    private static FOM makeFom(TreeAutomaton<BitSet> semInvhom, TreeAutomaton<Pair<String, Set<List<String>>>> chartAfterRef, BitSet targetSem, Map<Integer, Integer> refDistance, SubsetAlgebra<String> sem) {
        return new FOM() {
            @Override
            public double evaluate(Rule left, CondensedRule right) {
                return 0; // not needed
            }

            @Override
            public double evaluateStates(int leftState, int rightState) {
                BitSet rightBitset = semInvhom.getStateForId(rightState);

                BitSet unrealized = new BitSet();
                unrealized.or(targetSem);
                unrealized.andNot(rightBitset);
                int numUnrealized = unrealized.cardinality();

                BitSet extraSem = new BitSet();
                extraSem.or(rightBitset);
                extraSem.andNot(targetSem);
                int numExtra = extraSem.cardinality();

//                Set<List<String>> ref = chartAfterRef.getStateForId(leftState).getRight();
//                int numDistractors = ref.size() - 1;
                int dist = refDistance.get(leftState);

                double value = 100000 * numUnrealized + 1000 * dist + numExtra; // lexicographic sort by (numUnrealized, numExtra)

//                System.err.printf("evaluate %s + %s -> %f\n", chartAfterRef.getStateForId(leftState), sem.toSet(rightBitset), value);
                return value;
            }
        };
    }

    private static class DistanceSemiring implements Semiring<Integer> {
        @Override
        public Integer add(Integer x, Integer y) {
            return Math.min(x, y);
        }

        @Override
        public Integer multiply(Integer x, Integer y) {
            return x + y;
        }

        @Override
        public Integer zero() {
            return Integer.MAX_VALUE;
        }

    }

    private static void cmdlineUsage(String errorMessage) {
        if (jc != null) {
            if (errorMessage != null) {
                System.out.println(errorMessage);
            }

            jc.setProgramName("java -cp <alto.jar> de.up.ling.irtg.script.SurfaceRealizer <grammar_filename> <model_filename>");
            jc.usage();
        }

        if (errorMessage != null) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    private static class CmdLineParameters {

        @Parameter
        public List<String> filenames = new ArrayList<>();

        @Parameter(names = "--ref", required = true, description = "Set of individual that should be referred to.")
        public String ref = null;

        @Parameter(names = "--sem", required = true, description = "Semantic atoms that should be expressed.")
        public String sem = null;

        @Parameter(names = "--warmup", description = "Number of warmup iterations.")
        public int warmupIterations = 0;
        
        @Parameter(names = "--avg", description = "Number of iterations over which runtime is averaged.")
        public int avgIterations = 1;

        @Parameter(names = "--debug-tirtg", description = "Write instantiated version of the template IRTG to this filename.")
        public String debugTirtgFilename = null;

        @Parameter(names = "--print-chart", description = "Write the chart to this filename.")
        public String printChartFilename = null;

        @Parameter(names = "--print-derivations", description = "Display derivation trees.")
        public boolean printDerivations = false;

        @Parameter(names = "--help", help = true, description = "Prints usage information.")
        private boolean help;
    }

}
