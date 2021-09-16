package fuzzing4j.core.util;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-08-17 09:34
 * @description
 */
public class PrinterManager {
    static Logger logger = Logger.getLogger(PrinterManager.class.getName());
    static Level DEBUG = Level.parse("600");
    static {
        logger.setLevel(Level.ALL);
    }
    public static PrintStream getPrintStream(){
        return System.out;
    }
}
