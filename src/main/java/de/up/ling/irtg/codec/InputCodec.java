/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.codec;

import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.util.ProgressListener;
import de.up.ling.irtg.util.Util;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * An <i>input codec</i> for reading objects of
 * class <code>E</code> from an input stream.
 * 
 * @author koller
 */
public abstract class InputCodec<E> {
    private static Map<String, InputCodec> codecByName = null;
    private static Map<String, InputCodec> codecByExtension = null;
    
    protected ProgressListener progressListener;

    /**
     * Reads an object from an input stream.
     * 
     * @param is
     * @return
     * @throws ParseException if an error occurred while decoding
     * the input stream into an object
     * @throws IOException if an error occurred while reading
     * data from the input stream
     */
    public abstract E read(InputStream is) throws ParseException, IOException;

    /**
     * Reads an object from a string representation.
     * This method uses {@link String#getBytes() } to decode
     * the string into a sequence of bytes using the
     * platform's default encoding, and then reads
     * an object from this byte stream using {@link #read(java.io.InputStream) }.
     * It is provided as a convenience method for the
     * frequent special case where the object is represented
     * as a string of some kind.
     * 
     * @param s
     * @return
     * @throws ParseException
     * @throws IOException 
     */
    public E read(String s) throws ParseException, IOException {
        return read(new ByteArrayInputStream(s.getBytes()));
    }

    /**
     * Returns the metadata for this input codec.
     * 
     * @return 
     */
    public CodecMetadata getMetadata() {
        return (CodecMetadata) getClass().getAnnotation(CodecMetadata.class);
    }

    /**
     * Returns all registered input codecs. You can register
     * a new input codec by adding its fully qualified class name
     * to the file <code>META-INF/services/de.up.ling.irtg.codec.InputCodec</code>.
     * You can find this file under <code>src/main/resources</code>
     * in the IRTG source code repository.
     * 
     * @return 
     */
    private static Iterable<InputCodec> getAllInputCodecs() {
        return ServiceLoader.load(InputCodec.class);
    }

    /**
     * Returns all registered input codecs that can produce objects
     * of class <code>T</code> (or a subtype). For instance, call
     * <code>InputCodec.getInputCodecs(Tree.class)</code> to obtain
     * all input codecs for trees.
     * The method takes the
     * information about what class an input codec reads from the
     * <code>type</code> field in the codec's metadata annotation.
     * 
     * @param <T>
     * @param forClass
     * @return 
     */
    public static <T> List<InputCodec<T>> getInputCodecs(Class<T> forClass) {
        List<InputCodec<T>> ret = new ArrayList<>();

        for (InputCodec codec : getAllInputCodecs()) {
            if (forClass.isAssignableFrom(codec.getMetadata().type())) {
                ret.add(codec);
            }
        }

        return ret;
    }

    /**
     * Returns the registered input codec with the given name (as per
     * the codec metadata's <code>name</code> field). If
     * no codec with this name can be found, returns null.
     * 
     * @param name
     * @return 
     */
    public static InputCodec getInputCodecByName(String name) {
        if (codecByName == null) {
            codecByName = new HashMap<>();
            for (InputCodec ic : getAllInputCodecs()) {
                codecByName.put(ic.getMetadata().name(), ic);
            }
        }

        return codecByName.get(name);
    }

    /**
     * Returns the registered input codec for the given
     * filename extension (as per the codec metadata's
     * <code>extension</code> field). If no codec for
     * this extension can be found, returns null.
     * 
     * @param extension
     * @return 
     */
    public static InputCodec getInputCodecByExtension(String extension) {
        if (codecByExtension == null) {
            codecByExtension = new HashMap<>();
            for (InputCodec ic : getAllInputCodecs()) {
                codecByExtension.put(ic.getMetadata().extension(), ic);
            }
        }

        return codecByExtension.get(extension);
    }
    
    /**
     * Sets the progress listener for this codec. If the
     * input codec takes a long time to decode an input
     * stream, it may inform the user about its progress
     * by sending updates to the progress listener.
     * You can call this method with a null argument
     * to disable progress updates.
     * 
     * @param listener 
     */
    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }
    
    protected void notifyProgressListener(int currentValue, int maxValue, String string) {
        if( progressListener != null ) {
            progressListener.accept(currentValue, maxValue, string);
        }
    }

    public static void main(String[] args) throws Exception {
        List<InputCodec<InterpretedTreeAutomaton>> irtgCodecs = InputCodec.getInputCodecs(InterpretedTreeAutomaton.class);
        for (InputCodec i : irtgCodecs) {
            System.err.println(i.getMetadata());
        }
    }
}
