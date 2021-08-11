package fuzzing4j.core.instrument;

import fuzzing4j.core.config.InjectBean;
import fuzzing4j.core.util.Constants;
import fuzzing4j.core.util.RSUtil;
import fuzzing4j.api.Configurator;
import fuzzing4j.api.NONE;
import fuzzing4j.api.annotation.Fuzz;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;

class Fuzzing4jTransformer implements ClassFileTransformer {
    private Map<String, Map<String, InjectBean>> injectFieldInfo;

    public Fuzzing4jTransformer(Map<String, Map<String, InjectBean>> injectFieldInfo) {
        this.injectFieldInfo = injectFieldInfo;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            classReader.accept(new RemoveAnnoVisitor(classWriter, this.injectFieldInfo.get(className.replace("/", "."))), ClassReader.SKIP_FRAMES);
            return classWriter.toByteArray();
        } catch (Exception e) {
            throw new IllegalClassFormatException("transform '" + className + "' throws a exception of " + e);
        }
    }

    private static class RemoveAnnoVisitor extends ClassVisitor {
        static final String FUZZ_ANNO_DESC = "L" + Fuzz.class.getName().replace(".", "/") + ";";
        private Map<String, InjectBean> injectInfo;

        public RemoveAnnoVisitor(ClassWriter classWriter, Map<String, InjectBean> injectInfo) {
            super(Constants.VAR_ASM_API, classWriter);
            this.injectInfo = injectInfo;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (FUZZ_ANNO_DESC.equals(descriptor)) {
                return null;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if ("<init>".equals(name)) {
                int fieldSize = injectInfo == null ? 0 : injectInfo.size();
                if (fieldSize > 0) {
                    return new MethodVisitor(Constants.VAR_ASM_API, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (Opcodes.RETURN == opcode) {
                                for (InjectBean inject : injectInfo.values()) {
                                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                                    String classFullName = inject.getFullClassName();
                                    Class<? extends Configurator> cfgClazz = inject.getCfgClazz();
                                    String cfgName = inject.getName();
                                    String fieldName = inject.getFieldName();
                                    Class fieldClazz = inject.getFieldClazz();
                                    if (cfgClazz != NONE.class && !Objects.equals(cfgName, "")) {
                                        mv.visitLdcInsn(Type.getType(cfgClazz));
                                        mv.visitLdcInsn(cfgName);
                                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, RSUtil.class.getName().replace(".", "/"), "get", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;", false);
                                    } else if (cfgClazz != NONE.class) {
                                        mv.visitLdcInsn(Type.getType(cfgClazz));
                                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, RSUtil.class.getName().replace(".", "/"), "get", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
                                    } else if (!Objects.equals(cfgName, "")) {
                                        mv.visitLdcInsn(cfgName);
                                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, RSUtil.class.getName().replace(".", "/"), "get", "(Ljava/lang/String;)Ljava/lang/Object;", false);
                                    } else {
                                        throw new IllegalStateException("Inject field '" + classFullName + "." + fieldName + "' error ! value or cfg must be specified.");
                                    }
                                    mv.visitTypeInsn(Opcodes.CHECKCAST, fieldClazz.getName().replace(".", "/"));
                                    mv.visitFieldInsn(Opcodes.PUTFIELD, classFullName.replace(".", "/"), fieldName, Type.getDescriptor(fieldClazz));
                                }
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
            return new MethodVisitor(Constants.VAR_ASM_API, mv) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (FUZZ_ANNO_DESC.equals(descriptor)) {
                        return null;
                    }
                    return super.visitAnnotation(descriptor, visible);
                }
            };
        }
    }
}