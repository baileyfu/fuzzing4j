package fuzzing4j.core;

import fuzzing4j.core.config.FuzzBean;
import fuzzing4j.core.config.InjectBean;
import fuzzing4j.core.util.PathUtil;
import fuzzing4j.api.annotation.Fuzz;
import fuzzing4j.api.annotation.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.objectweb.asm.Type;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:05
 * @description
 */
public class AnnoResolver {
    Map<String, FuzzBean> fuzzClass;
    Map<String, Map<String, FuzzBean>> fuzzMethod;
    Map<String, Map<String, InjectBean>> injectField;
    List<String> configuratorClassNames;

    private AnnoResolver() {
        fuzzClass = new HashMap<>();
        fuzzMethod = new HashMap<>();
        injectField = new HashMap<>();
        configuratorClassNames = new ArrayList();
    }

    public boolean isFuzzBean(String classFullName) {
        return fuzzMethod.containsKey(classFullName);
    }

    public boolean isFuzzMethod(String classFullName, String methodNameAndDescriptor) {
        Map<String, FuzzBean> methodInfo = fuzzMethod.get(classFullName);
        return methodInfo != null && methodInfo.containsKey(methodNameAndDescriptor);
    }

    public Map<String, Map<String, InjectBean>> getInjectField() {
        return injectField;
    }

    static AnnoResolver resolve(List<String> fuzzClasspaths, ClassLoader runtimeClassLoader) throws Exception {
        ResolverClassLoader resolverClassLoader = new ResolverClassLoader(PathUtil.strings2Urls(fuzzClasspaths), runtimeClassLoader);
        List<String> fuzzClassNames = new ArrayList<>();
        for (String fuzzClasspath : fuzzClasspaths) {
            File filePath = new File(fuzzClasspath);
            Collection<File> files = FileUtils.listFiles(filePath, new SuffixFileFilter("class"), DirectoryFileFilter.INSTANCE);
            int index = filePath.getAbsolutePath().length();
            for (File file : files) {
                String path = file.getAbsolutePath();
                fuzzClassNames.add(path.substring(index + 1, path.length() - 6).replace(File.separator, "."));
            }
        }
        AnnoResolver instance = new AnnoResolver();
        Class configuratorClazz = Class.forName("fuzzing4j.api.Configurator", false, resolverClassLoader);
        for (String fuzzClassName : fuzzClassNames) {
            if (fuzzClassName.endsWith("Fuzz") || fuzzClassName.endsWith("Configurator")) {
                Class<?> clazz = Class.forName(fuzzClassName, false, resolverClassLoader);
                Fuzz fuzz = clazz.getDeclaredAnnotation(Fuzz.class);
                if (fuzz != null) {
                    FuzzBean fuzzBean = new FuzzBean();
                    fuzzBean.setClassFullName(fuzzClassName);
                    instance.fuzzClass.put(fuzzClassName, fuzzBean);

                    for (Method method : clazz.getDeclaredMethods()) {
                        method.setAccessible(true);
                        Fuzz fuzzMethod = method.getDeclaredAnnotation(Fuzz.class);
                        if (fuzzMethod != null) {
                            FuzzBean fuzzMethodBean = new FuzzBean();
                            fuzzMethodBean.setMethod(method.getName());
                            fuzzMethodBean.setTimes(fuzzMethod.times());
                            fuzzMethodBean.setDuration(fuzzMethod.duration());
                            Map<String, FuzzBean> map = instance.fuzzMethod.get(fuzzClassName);
                            if (map == null) {
                                map = new HashMap<>();
                                instance.fuzzMethod.put(fuzzClassName, map);
                            }
                            map.put(method.getName() + Type.getType(method).getDescriptor(), fuzzMethodBean);
                        }
                    }

                    for (Field field : clazz.getDeclaredFields()) {
                        Inject inject = field.getDeclaredAnnotation(Inject.class);
                        if (inject != null) {
                            InjectBean injectBean = new InjectBean();
                            injectBean.setFullClassName(fuzzClassName);
                            injectBean.setFieldName(field.getName());
                            injectBean.setFieldClazz(field.getType());
                            injectBean.setCfgClazz(inject.cfg());
                            injectBean.setName(inject.value());
                            Map<String, InjectBean> map = instance.injectField.get(fuzzClassName);
                            if (map == null) {
                                map = new HashMap<>();
                                instance.injectField.put(fuzzClassName, map);
                            }
                            map.put(field.getName(), injectBean);
                        }
                    }
                }
                if (configuratorClazz.isAssignableFrom(clazz)) {
                    instance.configuratorClassNames.add(fuzzClassName);
                }
            }
        }
        return instance;
    }

    private static class ResolverClassLoader extends URLClassLoader {
        public ResolverClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
    }
}
