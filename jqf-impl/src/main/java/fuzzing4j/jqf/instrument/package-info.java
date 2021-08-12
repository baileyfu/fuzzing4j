package fuzzing4j.jqf.instrument;

import edu.berkeley.cs.jqf.fuzz.JQF;
import fuzzing4j.core.util.Constants;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.function.BiFunction;

class JQFAnnoTranformer implements ClassFileTransformer {
    private BiFunction<String, String, Boolean> ifFuzzMethod;

    public JQFAnnoTranformer(BiFunction<String, String, Boolean> ifFuzzMethod) {
        this.ifFuzzMethod = ifFuzzMethod;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        classReader.accept(new AddAnnoVisitor(classWriter, (name, descriptor) -> ifFuzzMethod.apply(className.replace("/", "."), name + descriptor)), ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }

    private static class AddAnnoVisitor extends ClassVisitor {
        static final String JUNIT_RUNWITH_DESC = "Lorg/junit/runner/RunWith;";
        static final String JQF_FUZZ_DESC = "Ledu/berkeley/cs/jqf/fuzz/Fuzz;";
        private BiFunction<String, String, Boolean> ifFuzzMethod;
        private boolean isRunWithPresent;

        public AddAnnoVisitor(ClassVisitor classVisitor, BiFunction<String, String, Boolean> ifFuzzMethod) {
            super(Constants.VAR_ASM_API, classVisitor);
            this.ifFuzzMethod = ifFuzzMethod;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (JUNIT_RUNWITH_DESC.equals(descriptor)) {
                isRunWithPresent = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return ifFuzzMethod.apply(name, descriptor)
                    ? new MethodVisitor(Constants.VAR_ASM_API, mv) {
                        boolean isFuzzPresent;
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            if (visible && JQF_FUZZ_DESC.equals(descriptor)) {
                                isFuzzPresent = true;
                            }
                            return super.visitAnnotation(descriptor, visible);
                        }

                        @Override
                        public void visitEnd() {
                            if (!isFuzzPresent) {
                                AnnotationVisitor av = super.visitAnnotation(JQF_FUZZ_DESC, true);
                                if (av != null) {
                                    av.visitEnd();
                                }
                                isFuzzPresent = true;
                            }
                            super.visitEnd();
                        }
                    }
                    : mv;
        }

        @Override
        public void visitEnd() {
            if (!isRunWithPresent) {
                AnnotationVisitor av = super.visitAnnotation(JUNIT_RUNWITH_DESC, true);
                if (av != null) {
                    av.visit("value", Type.getType(JQF.class));
                    av.visitEnd();
                }
                isRunWithPresent = true;
            }
            super.visitEnd();
        }
    }
}