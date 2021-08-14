package fuzzing4j.plugin;

import fuzzing4j.api.em.RealFuzz;
import fuzzing4j.core.FuzzingExecutor;
import fuzzing4j.core.util.Constants;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.DateTimeException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-30 09:46
 * @description
 */
@Mojo(name = "fuzz", requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.VERIFY)
public class FuzzGoal extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;
    @Parameter(property = "aoc")
    boolean aoc = Constants.VAR_ABORT_ON_CRUSH;
    @Parameter(property = "d")
    String duration;
    @Parameter(property = "t")
    int times;
    @Parameter(property = "i",defaultValue="JQF")
    String realFuzzValue;
    @Parameter(property = "q")
    boolean quiet = Constants.VAR_QUIET;
    @Parameter(property = "ecp",defaultValue = "")
    String excludeClasspath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            System.setProperty(Constants.ENV_CORE_ABORT_ON_CRUSH,Boolean.toString(aoc));
            System.setProperty(Constants.ENV_CORE_QUIET,Boolean.toString(quiet));
            if(duration!=null){
                Duration.parse(duration);
                System.setProperty(Constants.ENV_CORE_DURATION,duration);
            }
            if(times<0){
                throw new IllegalArgumentException("times can not be negative");
            }
            System.setProperty(Constants.ENV_CORE_TIMES,String.valueOf(times));
            RealFuzz realFuzz = RealFuzz.valueOf(realFuzzValue);
            if (realFuzz == null) {
                throw new IllegalArgumentException("unrecognized realFuzz");
            }
            List<String> fuzzClasspaths=new ArrayList<>();
            fuzzClasspaths.add(project.getTestClasspathElements().get(0));
            ClassLoader loader = makeClassLoader(fuzzClasspaths);
            Thread.currentThread().setContextClassLoader(loader);
            FuzzingExecutor fuzzingExecutor = new FuzzingExecutor(fuzzClasspaths);
            fuzzingExecutor.execute(realFuzz,loader);
        } catch (DateTimeException e) {
            throw new MojoExecutionException("incorrect parameter",e);
        }catch (IllegalArgumentException e) {
            throw new MojoExecutionException("incorrect parameter",e);
        }catch(Exception e){
            throw new MojoFailureException("Internal error",e);
        }
    }

    private ClassLoader makeClassLoader(List<String> fuzzClasspaths)throws Exception{
        List<String> excludeClasspathPrefix=new ArrayList<>();
        if (excludeClasspath != null) {
            excludeClasspathPrefix.addAll(Arrays.asList(excludeClasspath.split(",")));
        }
        return new URLClassLoader(project.getTestClasspathElements().stream()
                .filter((classpath)->{
                    if (fuzzClasspaths.contains(classpath)) {
                        return false;
                    } else {
                        for (String prefix : excludeClasspathPrefix) {
                            if (classpath.contains("\\" + prefix) || classpath.contains("/" + prefix)) {
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .map((classPath) -> {
                    try {
                        return new File(classPath).toURI().toURL();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }).collect(Collectors.toList()).toArray(new URL[0]), getClass().getClassLoader());
    }
}
