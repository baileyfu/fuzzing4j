package fuzzing4j.core;

import fuzzing4j.core.config.FuzzBean;
import fuzzing4j.core.instrument.Fuzzing4jClassLoader;
import fuzzing4j.core.util.ExcelExporter;
import fuzzing4j.api.em.RealFuzz;
import fuzzing4j.core.util.Printable;

import java.io.File;
import java.time.Duration;
import java.util.*;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:05
 * @description
 */
public abstract class FuzzingRunner extends RunningEnv implements Printable {
    protected List<String> fuzzClassPath;
    protected Fuzzing4jClassLoader classLoader;
    protected AnnoResolver annoResolver;

    synchronized void initializing(AnnoResolver annoResolver, List<String> fuzzClassPath, ClassLoader parent) throws Exception {
        this.annoResolver = annoResolver;
        this.fuzzClassPath = fuzzClassPath;
        if (classLoader == null) {
            classLoader = createClassLoader(parent);
        }
    }

    void run() throws Exception {
        long startTime = System.currentTimeMillis();
        Map<String, List<RunResult>> map = new HashMap<>();
        try {
            for (String classFullName : annoResolver.fuzzClass.keySet()) {
                List<RunResult> results = doRun(annoResolver.fuzzClass.get(classFullName), annoResolver.fuzzMethod.get(classFullName));
                map.put(classFullName, results);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            long wholeRanSeconds = Duration.ofMillis(System.currentTimeMillis() - startTime).getSeconds();
            ExcelExporter exporter = new ExcelExporter();
            Map<String, List<ExcelExporter.DetailBean>> data = new HashMap<>();
            int methodCount = 0;
            List<String> failedClass = new ArrayList<>();
            int failedMethodCount = 0;
            int ranTimes = 0;
            int failedTimes = 0;
            for (String classFullName : map.keySet()) {
                List<ExcelExporter.DetailBean> detailList = data.get(classFullName);
                if (detailList == null) {
                    detailList = new ArrayList<>();
                    data.put(classFullName, detailList);
                }
                List<RunResult> r = map.get(classFullName);
                methodCount += r.size();
                for (RunResult rr : r) {
                    ranTimes += rr.ranTimes;
                    failedTimes += rr.failTimes;

                    ExcelExporter.DetailBean detail = new ExcelExporter.DetailBean();
                    detail.setMethod(rr.method);
                    detail.setRanSeconds(Duration.ofMillis(rr.ranMillis).getSeconds());
                    detail.setRanTimes(rr.ranTimes);
                    detail.setFailedTimes(rr.failTimes);
                    if (!rr.success) {
                        failedMethodCount++;
                        failedClass.add(classFullName);

                        List<String[]> failures = new ArrayList<>();
                        for (Object[] args : rr.paramAndStackTrace.keySet()) {
                            failures.add(new String[]{Arrays.toString(args), rr.paramAndStackTrace.get(args)});
                        }
                        detail.setFailures(failures);
                    }
                    detailList.add(detail);
                }
                data.put(classFullName, detailList);
            }
            Object[] overview = new Object[7];
            overview[0] = wholeRanSeconds;
            overview[1] = map.size();
            overview[2] = methodCount;
            overview[3] = failedClass.size();
            overview[4] = failedMethodCount;
            overview[5] = ranTimes;
            overview[6] = failedTimes;
            exporter.writeOverview(overview);
            exporter.writeData(data);
            exporter.export(getOutPath());
        }
    }

    protected String getOutPath() {
        return new File(fuzzClassPath.get(0)).getParent();
    }

    protected File[] getSeedFiles(FuzzBean fuzzClass, FuzzBean fuzzMethod) {
        //TODO
        return null;
    }

    protected abstract Fuzzing4jClassLoader createClassLoader(ClassLoader parent) throws Exception;

    protected abstract List<RunResult> doRun(FuzzBean fuzzClass, Map<String, FuzzBean> fuzzMethods) throws Exception;

    public abstract RealFuzz getRealFuzz();

    protected static class RunResult {
        String method;
        boolean success;
        long ranMillis;
        int ranTimes;
        int failTimes;
        Map<Object[], String> paramAndStackTrace;

        public RunResult(String method) {
            this.method = method;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void setRanMillis(long ranMillis) {
            this.ranMillis = ranMillis;
        }

        public void setRanTimes(int ranTimes) {
            this.ranTimes = ranTimes;
        }

        public void setFailTimes(int failTimes) {
            this.failTimes = failTimes;
        }

        public void setParamAndStackTrace(Map<Object[], String> paramAndStackTrace) {
            this.paramAndStackTrace = paramAndStackTrace;
        }
    }
}
