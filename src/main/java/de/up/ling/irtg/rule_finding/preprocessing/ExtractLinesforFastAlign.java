/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.rule_finding.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.function.Function;

/**
 *
 * @author christoph_teichmann
 */
public class ExtractLinesforFastAlign {   
    
    /**
     * 
     * @param sentenceLine
     * @param funqlLine
     * @param in
     * @param out
     * @throws IOException 
     */
    public static void getQueryFunql(int sentenceLine, int funqlLine, InputStream in, OutputStream out)
                                                        throws IOException{
        Function<String,String> sent = (String s) -> s;
        Function<String, String> funql = (String s) -> s.replaceAll("[\\(\\)]", " ");

        getLines(sent, funql, sentenceLine, funqlLine, in, out);
    }

    /**
     *
     * @param processFirst
     * @param processSecond
     * @param firstLine
     * @param secondLine
     * @param in
     * @param out
     * @throws IOException
     */
    public static void getLines(Function<String, String> processFirst,
            Function<String, String> processSecond,
            int firstLine, int secondLine,
            InputStream in, OutputStream out) throws IOException {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(in));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(out))) {

            boolean first = true;
            String line;
            ArrayList<String> list = new ArrayList<>();
            while ((line = input.readLine()) != null) {
                if (line.trim().equals("")) {
                    if (!list.isEmpty()) {
                        if (first) {
                            first = false;
                        } else {
                            output.newLine();
                        }
                        dump(firstLine, secondLine, processFirst, processSecond, list, output);
                    }
                    list.clear();
                } else {
                    list.add(line.trim());
                }
            }

            if (!list.isEmpty()) {
                if (!first) {
                    output.newLine();
                }
                dump(firstLine, secondLine, processFirst, processSecond, list, output);
            }
        }
    }

    /**
     * 
     * @param firstLine
     * @param secondLine
     * @param processFirst
     * @param processSecond
     * @param list
     * @param out 
     */
    private static void dump(int firstLine, int secondLine,
            Function<String, String> processFirst, Function<String, String> processSecond,
            ArrayList<String> list, BufferedWriter out) throws IOException {
        out.write(processFirst.apply(list.get(firstLine)).trim()+" ||| "+processSecond.apply(list.get(secondLine)).trim());
    }
}
