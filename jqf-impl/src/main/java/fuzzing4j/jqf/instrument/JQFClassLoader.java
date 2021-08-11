package fuzzing4j.jqf.instrument;

import fuzzing4j.core.AnnoResolver;
import fuzzing4j.core.instrument.Fuzzing4jClassLoader;
import janala.instrument.SnoopInstructionTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:13
 * @description
 */
public class JQFClassLoader extends Fuzzing4jClassLoader {
    private JQFAnnoTranformer jqfAnnoTranformer;
    private ClassFileTransformer jqfTransformer;

    public JQFClassLoader(List<String> fuzzClassPath, ClassLoader parent, AnnoResolver annoResolver) throws MalformedURLException {
        super(fuzzClassPath, parent, annoResolver);
        jqfAnnoTranformer = new JQFAnnoTranformer(annoResolver::isFuzzMethod);
        jqfTransformer = new SnoopInstructionTransformer();
    }

    @Override
    protected byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = jqfAnnoTranformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        return jqfTransformer.transform(loader, className, classBeingRedefined, protectionDomain, byteCode);
    }
}
