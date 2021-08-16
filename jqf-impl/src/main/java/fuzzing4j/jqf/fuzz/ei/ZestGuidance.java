package fuzzing4j.jqf.fuzz.ei;

import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:13
 * @description
 */
public class ZestGuidance extends edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance {
    private ArrayList<Object[]> failureArgs = new ArrayList<>();
    private Object[] args;
    private int runTimesLimit;
    private int runTimes;

    public ZestGuidance(String testName, Duration duration, File outputDirectory) throws IOException {
        super(testName, duration, outputDirectory);
    }

    public ZestGuidance(String testName, Duration duration, File outputDirectory, File[] seedInputFiles) throws IOException {
        super(testName, duration, outputDirectory, seedInputFiles);
    }

    @Override
    public boolean hasInput() {
        runTimes++;
        if (this.EXIT_ON_CRASH && this.uniqueFailures.size() >= 1) {
            System.out.println("*Abort fuzzing of " + this.testName + " cause by 'abortOnCrush'.");
            return false;
        }
        return runTimesLimit > 0 ? runTimes < runTimesLimit : super.hasInput();
    }

    public void setRunTimesLimit(int runTimesLimit) {
        this.runTimesLimit = runTimesLimit;
    }

    public int getRunTimes() {
        return runTimes;
    }

    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {
        if (result == Result.FAILURE && failureArgs.size() < 3) {
            failureArgs.add(args);
        }
        super.handleResult(result, error);
    }

    @Override
    public void observeGeneratedArgs(Object[] args) {
        this.args = args;
    }

    public ArrayList<Object[]> getFailureArgs() {
        return failureArgs;
    }
}
