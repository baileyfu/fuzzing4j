package fuzzing4j.core;

import fuzzing4j.core.util.RSUtil;
import fuzzing4j.api.em.RealFuzz;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-08-01 13:22
 * @description
 */
public class FuzzingExecutor {
    private List<String> fuzzClasspaths;

    public FuzzingExecutor(List<String> fuzzClasspaths) {
        this.fuzzClasspaths = fuzzClasspaths;
    }
    private Map<RealFuzz,FuzzingRunner> loadRunners(ClassLoader classLoader)throws Exception{
        Iterator<FuzzingRunner> it= ServiceLoader.load(FuzzingRunner.class,classLoader).iterator();
        Map<RealFuzz,FuzzingRunner> runners=new HashMap<>();
        while(it.hasNext()){
            FuzzingRunner fuzzingRunner=it.next();
            runners.put(fuzzingRunner.getRealFuzz(),fuzzingRunner);
        }
        if(runners.size()==0){
            throw new IllegalStateException("No implementation of FuzzingRunner found!");
        }
        return runners;
    }
    private void before(AnnoResolver annoResolver, ClassLoader classLoader)throws Exception{
        for (String configuratorClassName : annoResolver.configuratorClassNames) {
            Class<?> clazz = Class.forName(configuratorClassName, true, classLoader);
            Object configurator=clazz.newInstance();
            Method initMethod = clazz.getMethod("init");
            initMethod.invoke(configurator);
            RSUtil.put(configurator);
        }
    }
    public void execute(RealFuzz realFuzz,ClassLoader runtimeClassLoader) throws Exception {
        Map<RealFuzz, FuzzingRunner> runners = loadRunners(runtimeClassLoader);
        FuzzingRunner fuzzingRunner = runners.get(realFuzz==RealFuzz.DEFAULT?RealFuzz.JQF:realFuzz);
        if (fuzzingRunner == null) {
            throw new IllegalStateException("No implementation of " + realFuzz + " found!");
        }
        AnnoResolver annoResolver = AnnoResolver.resolve(fuzzClasspaths,runtimeClassLoader);
        fuzzingRunner.initializing(annoResolver, fuzzClasspaths, runtimeClassLoader);
        before(annoResolver, fuzzingRunner.classLoader);
        try {
            fuzzingRunner.run();
        } catch (Exception e) {
            throw e;
        } finally {
            after(annoResolver);
        }
    }
    private void after(AnnoResolver annoResolver) {
        try {
            RSUtil.shutdown();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
