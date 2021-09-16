package fuzzing4j.core;

import fuzzing4j.core.util.Constants;

import java.time.Duration;
import java.util.Objects;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:05
 * @description
 */
public abstract class RunningEnv {
    protected boolean abortOnCrush() {
        return Boolean.getBoolean(Constants.ENV_CORE_ABORT_ON_CRUSH);
    }

    protected Duration getDuration(String duration) {
        if (!Objects.equals("", duration)) {
            try {
                return Duration.parse(duration);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        String globalDuration = System.getProperty(Constants.ENV_CORE_DURATION);
        if (!Objects.equals("", globalDuration)) {
            try {
                return Duration.parse(globalDuration);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        return null;
    }

    protected int getTimes(int times) {
        return times > 0 ? times : Integer.getInteger(Constants.ENV_CORE_TIMES, 0);
    }

    protected boolean isQuietMode() {
        return Boolean.getBoolean(Constants.ENV_CORE_QUIET);
    }
}
