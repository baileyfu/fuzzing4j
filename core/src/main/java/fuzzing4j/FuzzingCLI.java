package fuzzing4j;

import fuzzing4j.core.FuzzingExecutor;
import fuzzing4j.core.util.Constants;
import fuzzing4j.api.em.RealFuzz;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-08-10 11:36
 * @description
 */
@Command(name = "FuzzingCLI", mixinStandardHelpOptions = true)
public class FuzzingCLI implements Runnable {
    @Option(names = {"-aoc", "--abort-on-crash"})
    private boolean abortOnCrash = Constants.VAR_ABORT_ON_CRUSH;
    @Option(names = {"-d", "--duration"})
    private Duration duration;
    @Option(names = {"-t", "--times"})
    private int times;
    @Option(names = {"-q", "--quiet"})
    private boolean quiet = Constants.VAR_QUIET;
    @Option(names = {"-debug", "--debug-on"})
    private boolean debugOn = Constants.VAR_LOG_DEBUG_ON;
    private String testClassPath;

    public FuzzingCLI(String testClassPath) {
        Objects.nonNull(testClassPath);
        this.testClassPath = testClassPath.endsWith("\\.") || testClassPath.endsWith("/.")
                ? testClassPath.substring(0, testClassPath.length() - 1)
                : testClassPath;
    }

    @Override
    public void run() {
        System.setProperty(Constants.ENV_CORE_ABORT_ON_CRUSH, Boolean.toString(abortOnCrash));
        System.setProperty(Constants.ENV_CORE_LOG_DEBUG_ON, Boolean.toString(debugOn));
        System.setProperty(Constants.ENV_CORE_QUIET, Boolean.toString(quiet));
        if (duration != null) {
            System.setProperty(Constants.ENV_CORE_DURATION, duration.toString());
        }
        if (times > 0) {
            System.setProperty(Constants.ENV_CORE_TIMES, Integer.toString(times));
        }
        List<String> fuzzClasspaths = new ArrayList<>();
        fuzzClasspaths.add(new File(testClassPath).getAbsolutePath());
        FuzzingExecutor fuzzingExecutor = new FuzzingExecutor(fuzzClasspaths);
        try {
            fuzzingExecutor.execute(RealFuzz.JQF, getClass().getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String testClassPath = args.length > 0 ? args[0] : ".";
        int exitCode = new CommandLine(new FuzzingCLI(new File(testClassPath).getAbsolutePath())).execute(args);
        System.exit(exitCode);
    }
}
