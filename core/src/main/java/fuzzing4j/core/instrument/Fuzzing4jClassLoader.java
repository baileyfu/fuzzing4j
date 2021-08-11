package fuzzing4j.core.instrument;

import fuzzing4j.core.AnnoResolver;
import fuzzing4j.core.util.PathUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:07
 * @description
 */
public abstract class Fuzzing4jClassLoader extends URLClassLoader {
    private Fuzzing4jTransformer fuzzing4jTransformer;
    protected AnnoResolver annoResolver;
    public List<String> paths;

    public Fuzzing4jClassLoader(List<String> fuzzClassPath, ClassLoader parent, AnnoResolver annoResolver) throws MalformedURLException {
        super(PathUtil.strings2Urls(fuzzClassPath), parent);
        this.annoResolver = annoResolver;
        this.paths = fuzzClassPath;
        fuzzing4jTransformer = new Fuzzing4jTransformer(annoResolver.getInjectField());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;
        if (annoResolver.isFuzzBean(name)) {
            synchronized (getClassLoadingLock(name)) {
                clazz = findLoadedClass(name);
                if (clazz == null) {
                    clazz = findClass(name);
                }
                if (resolve) {
                    resolveClass(clazz);
                }
            }
        }
        return clazz == null ? super.loadClass(name, resolve) : clazz;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes;
        String internalName = name.replace(".", "/");
        String path = internalName.concat(".class");
        try (InputStream in = super.getResourceAsStream(path)) {
            if (in == null) {
                throw new ClassNotFoundException("Cannot find class : " + name);
            }
            BufferedInputStream buf = new BufferedInputStream(in);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = buf.read()) != -1) {
                baos.write(b);
            }
            bytes = baos.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException("I/O exception while loading class", e);
        }
        if (annoResolver.isFuzzBean(name)) {
            byte[] transformed = null;
            try {
                transformed = fuzzing4jTransformer.transform(this, internalName, null, null, bytes);
                transformed = transform(this, internalName, null, null, transformed);
            } catch (IllegalClassFormatException e) {
                e.printStackTrace();
            }
            bytes = transformed == null ? bytes : transformed;
        }
        return defineClass(name, bytes, 0, bytes.length);
    }

    abstract protected byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException;
}
