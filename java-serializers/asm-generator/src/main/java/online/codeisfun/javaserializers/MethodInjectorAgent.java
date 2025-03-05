package online.codeisfun.javaserializers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class MethodInjectorAgent {

//    // This premain method is called before the application’s main method when run with -javaagent.
//    public static void premain(String agentArgs, Instrumentation inst) {
//        inst.addTransformer(new ClassFileTransformer() {
//            @Override
//            public byte[] transform(Module module, ClassLoader loader, String className,
//                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
//                try {
//                    // We use ASM to read and potentially modify the class bytecode.
//                    ClassReader cr = new ClassReader(classfileBuffer);
//                    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
//                    ExtraMethodClassVisitor visitor = new ExtraMethodClassVisitor(cw);
//                    cr.accept(visitor, 0);
//                    // Return the modified class bytes (if no changes, they’ll be the same)
//                    return cw.toByteArray();
//                } catch (Exception e) {
//                    // In case of an error, return the original bytes.
//                    e.printStackTrace();
//                    return classfileBuffer;
//                }
//            }
//        });
//    }
}