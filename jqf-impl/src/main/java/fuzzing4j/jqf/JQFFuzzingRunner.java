package fuzzing4j.jqf;

import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import fuzzing4j.api.em.RealFuzz;
import fuzzing4j.core.FuzzingRunner;
import fuzzing4j.core.config.FuzzBean;
import fuzzing4j.core.instrument.Fuzzing4jClassLoader;
import fuzzing4j.jqf.fuzz.ei.ZestGuidance;
import fuzzing4j.jqf.instrument.JQFClassLoader;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:10
 * @description
 */
public class JQFFuzzingRunner extends FuzzingRunner {
    public JQFFuzzingRunner() {
        if (this.isQuietMode()) {
            System.setProperty("jqf.ei.QUIET_MODE", Boolean.TRUE.toString());
        }
        if (this.abortOnCrush()) {
            System.setProperty("jqf.ei.EXIT_ON_CRASH", Boolean.TRUE.toString());
        }
        System.setProperty("janala.verbose", Boolean.FALSE.toString());
    }

    @Override
    protected Fuzzing4jClassLoader createClassLoader(ClassLoader parent) throws Exception {
        return new JQFClassLoader(fuzzClassPath, parent, annoResolver);
    }

    @Override
    protected List<RunResult> doRun(FuzzBean fuzzClass, Map<String, FuzzBean> fuzzMethods) throws Exception {
        List<RunResult> results = new ArrayList<>();
        for (FuzzBean fuzzMethod : fuzzMethods.values()) {
            String title = fuzzClass.getClassFullName() + "#" + fuzzMethod.getMethod();
            System.out.println("====================>>>>>>>>>>" + title);
            Duration duration = this.getDuration(fuzzMethod.getDuration());
            File outputDirectory = new File(getOutPath() + File.separator + "jqf-fuzz-results" + File.separator + fuzzClass.getClassFullName() + File.separator + fuzzMethod.getMethod());
            boolean blindFuzzing = false;
            boolean logCoverage = true;
            try {
                File[] seedFiles = this.getSeedFiles(fuzzClass, fuzzMethod);
                ZestGuidance guidance = seedFiles != null
                        ? new ZestGuidance(title, duration, outputDirectory, seedFiles)
                        : new ZestGuidance(title, duration, outputDirectory);
                guidance.setRunTimesLimit(this.getTimes(fuzzMethod.getTimes()));
                guidance.setBlind(blindFuzzing);
                Result res = GuidedFuzzing.run(fuzzClass.getClassFullName(), fuzzMethod.getMethod(), classLoader, guidance, System.out);
                if (logCoverage) {
                    System.out.println(String.format("Coverd %d edges.", guidance.getTotalCoverage().getNonZeroCount()));
                }
                RunResult rr = new RunResult(fuzzMethod.getMethod());
                if (res.wasSuccessful()) {
                    rr.setSuccess(true);
                } else {
                    rr.setFailTimes(res.getFailureCount());
                    ArrayList<Object[]> failureArgs = guidance.getFailureArgs();
                    Map<Object[], String> paramAndStackTrace = new LinkedHashMap<>();
                    List<Failure> failures = res.getFailures();
                    for (int i = 0; i < failureArgs.size(); i++) {
                        paramAndStackTrace.put(failureArgs.get(i), failures.get(i).getTrimmedTrace());
                    }
                    rr.setParamAndStackTrace(paramAndStackTrace);
                }
                System.out.println("run " + guidance.getRunTimes() + " times<<<<<<<<<<<====================");
                rr.setRanMillis(res.getRunTime());
                rr.setRanTimes(guidance.getRunTimes());
                results.add(rr);
            } catch (Exception e) {
                throw new Exception(title + " fuzzing4j error!", e);
            }
        }
        return results;
    }

    @Override
    public RealFuzz getRealFuzz() {
        return RealFuzz.JQF;
    }
}
