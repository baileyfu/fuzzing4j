package fuzzing4j.core.util;

import java.util.logging.Level;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-08-17 09:25
 * @description
 */
public interface Printable {
    default void logInfo(String message){
        PrinterManager.logger.log(Level.INFO,message);
    }
    default void logDebug(String message) {
        if (Boolean.getBoolean(Constants.ENV_CORE_LOG_DEBUG_ON)) {
            PrinterManager.logger.log(PrinterManager.DEBUG, message);
        }
    }
    default void println(String message){
        PrinterManager.getPrintStream().println(message);
    }
}
