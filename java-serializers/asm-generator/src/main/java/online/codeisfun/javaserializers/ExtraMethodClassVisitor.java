package online.codeisfun.javaserializers;

import org.objectweb.asm.*;

public class ExtraMethodClassVisitor extends ClassVisitor {

    private boolean addMethod = false;
    private String className;

    public ExtraMethodClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // Look for our annotation (the descriptor is in the format L<binary name>;)
        if ("Lonline/codeisfun/javaserializers/AddExtraMethod;".equals(descriptor)) {
            addMethod = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitEnd() {
        if (addMethod) {
            // Define a new public method: public void extraMethod()
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "extraMethod", "()V", null, null);
            if (mv != null) {
                mv.visitCode();
                // Get System.out field
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                // Push a string literal onto the stack
                mv.visitLdcInsn("Extra method injected into " + className.replace('/', '.'));
                // Invoke PrintStream.println(String)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
                                   "(Ljava/lang/String;)V", false);
                // Return void
                mv.visitInsn(Opcodes.RETURN);
                // Compute max stack and locals automatically
                mv.visitMaxs(2, 1);
                mv.visitEnd();
            }
        }
        super.visitEnd();
    }
}