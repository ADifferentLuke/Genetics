package net.lukemcomber.dev.ai.genetics.service;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

//Copied from https://stackoverflow.com/questions/6995946/log4j-how-do-i-redirect-an-outputstream-or-writer-to-loggers-writers
public class LoggerOutputStream extends OutputStream {

    /**
     * The logger where to log the written bytes.
     */
    private final Logger logger;

    /**
     * The level.
     */
    private final Level level;

    /**
     * The internal memory for the written bytes.
     */
    private String mem;

    /**
     * Creates a new log output stream which logs bytes to the specified logger with the specified
     * level.
     *
     * @param logger the logger where to log the written bytes
     * @param level  the level
     */
    public LoggerOutputStream(final Logger logger,final Level level) {
        this.logger = logger;
        this.level = level;
        mem = "";
    }

    /**
     * Writes a byte to the output stream. This method flushes automatically at the end of a line.
     *
     * @param b DOCUMENT ME!
     */
    public void write(int b) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (b & 0xff);
        mem = mem + new String(bytes);

        if (mem.endsWith("\n")) {
            mem = mem.substring(0, mem.length() - 1);
            flush();
        }
    }

    /**
     * Flushes the output stream.
     */
    public void flush() {
        logger.log(level, mem);
        mem = "";
    }
}
