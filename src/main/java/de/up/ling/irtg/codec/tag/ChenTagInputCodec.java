/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.codec.tag;

import de.saar.basic.Pair;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.codec.CodecMetadata;
import de.up.ling.irtg.codec.CodecParseException;
import de.up.ling.irtg.codec.InputCodec;
import de.up.ling.irtg.util.MutableInteger;
import de.up.ling.tree.Tree;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author koller
 */
@CodecMetadata(name = "chen-tag", description = "Tree-adjoining grammar (Chen format)", type = InterpretedTreeAutomaton.class)
public class ChenTagInputCodec extends InputCodec<InterpretedTreeAutomaton> {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        ChenTagInputCodec ic = new ChenTagInputCodec();
        TagGrammar tagg = ic.readUnlexicalizedGrammar(new FileReader(args[0]));
        ic.lexicalizeFromCorpus(tagg, new FileReader(args[1]));
        System.out.println(tagg);
        
        System.out.println("\n\n");
        for(String word : tagg.getWords()) {
            System.out.println("\nword: " + word + "\n==================\n");
            for( ElementaryTree et : tagg.lexicalizeElementaryTrees(word)) {
                System.out.println("   " + et);
            }
        }
    }

    @Override
    public InterpretedTreeAutomaton read(InputStream is) throws CodecParseException, IOException {
        TagGrammar tagg = readUnlexicalizedGrammar(new InputStreamReader(is));

        // todo - read lexicon
        return tagg.toIrtg();
    }

    public void lexicalizeFromCorpus(TagGrammar tagg, Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        String line = null;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\s+");

            if (parts.length >= 2) {
                String word = parts[1];
                String treename = parts[7];
                LexiconEntry lex = new LexiconEntry(word, treename);
                
                for( int i = 10; i < parts.length; i++ ) {
                    assert parts[i].contains("=");
                    String[] split = parts[i].split("=");
                    lex.addFeature(split[0], split[1]);
                }
                
                tagg.addLexiconEntry(word, lex);
            }
        }
    }

    public TagGrammar readUnlexicalizedGrammar(Reader r) throws IOException {
        TagGrammar tagg = new TagGrammar();

        BufferedReader br = new BufferedReader(r);
        String line = null;

        try {
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                Tree<Pair<String, NodeType>> t = decodeElementaryTree(parts, new MutableInteger(1));
                boolean isAuxiliary = t.some(p -> p.getRight().equals(NodeType.FOOT));
                tagg.addElementaryTree(parts[0], new ElementaryTree(t, isAuxiliary ? ElementaryTreeType.AUXILIARY : ElementaryTreeType.INITIAL));
            }

            return tagg;
        } catch (CodecParseException e) {
            throw new CodecParseException("Error while parsing line: " + line + ": " + e.getMessage());
        }
    }

    private String[] splitNodeDescriptor(String nodeDescriptor) {
        String[] pparts = nodeDescriptor.split("#", -1); // including trailing empty string
        String[] ret = null;

        if (pparts.length == 5) {
            return pparts;
        } else {
            // has shape like "###2#l#h"

            assert pparts.length == 6;
            ret = new String[5];
            ret[0] = "#";
            ret[1] = pparts[2];
            ret[2] = pparts[3];
            ret[3] = pparts[4];
            ret[4] = pparts[5];
            return ret;
        }
    }

    private Tree<Pair<String, NodeType>> decodeElementaryTree(String[] parts, MutableInteger pos) {
        String[] pparts = splitNodeDescriptor(parts[pos.incValue()]);
        String nt = pparts[0];
        NodeType nodeType = decodeNodetype(pparts[4]);
        String nodename = pparts[2];
        List<Tree<Pair<String, NodeType>>> children = new ArrayList<>();

        assert "l".equals(pparts[3]);

        while (pos.getValue() < parts.length) {
            pparts = splitNodeDescriptor(parts[pos.getValue()]);
            if (pparts[2].equals(nodename)) {
                pos.incValue();
                break;
            }

            children.add(decodeElementaryTree(parts, pos));
        }

        return Tree.create(new Pair(nt, nodeType), children);
    }

    private NodeType decodeNodetype(String marker) {
        if ("".equals(marker)) {
            return NodeType.DEFAULT;
        } else if ("f".equals(marker)) {
            return NodeType.FOOT;
        } else if ("h".equals(marker)) {
            return NodeType.HEAD;
        } else if ("c".equals(marker)) {
            return NodeType.SECONDARY_LEX;
        } else if ("s".equals(marker)) {
            return NodeType.SUBSTITUTION;
        } else {
            throw new CodecParseException("Unknown node type: " + marker);
        }
    }
}
