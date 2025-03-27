package online.codeisfun.javaserializers;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

/**
 * A Javac plugin that tries to add a method to classes annotated with @MyAnnotation.
 *
 * NOTE: This uses internal javac APIs and is not guaranteed to be stable across releases.
 */
public class MyLombokPlugin implements Plugin, TaskListener {

    private Context context;
    private JavacTrees trees;
    private TreeMaker treeMaker;
    private Names names;

    @Override
    public String getName() {
        return "MyLombokPlugin";
    }

    @Override
    public void init(JavacTask task, String... args) {
        System.out.println("Initializing MyLombokPlugin...");

        // Save the context for later use
        context = ((com.sun.tools.javac.api.BasicJavacTask) task).getContext();
        // Get helpers to manipulate the AST
        trees = JavacTrees.instance(task);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);

        // Register this plugin as a TaskListener
        task.addTaskListener(this);
    }

    @Override
    public void started(TaskEvent e) {
        // No action needed at the start of each event
    }

    @Override
    public void finished(TaskEvent e) {
        // Typically, to add methods that are visible to the compiler,
        // you want to act around the ANALYZE or ENTER phases, not GENERATE.
        if (e.getKind() == TaskEvent.Kind.ANALYZE) {
            // The compilation unit's AST
            JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) e.getCompilationUnit();
            if (compilationUnit == null) return;

            // We traverse the AST with a custom translator
            compilationUnit.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl classDecl) {
                    // Check if the class is annotated with @MyAnnotation
                    if (hasMyAnnotation(classDecl)) {
                        // Add a new method to the AST
                        addExtraMethod(classDecl);
                    }
                    super.visitClassDef(classDecl);
                }
            });
        }
    }

    /**
     * Example check for a custom annotation @MyAnnotation.
     */
    private boolean hasMyAnnotation(JCTree.JCClassDecl classDecl) {
        if (classDecl.mods.annotations == null) return false;
        return classDecl.mods.annotations.stream().anyMatch(anno -> {
            String annoType = anno.annotationType.toString();
            return annoType.endsWith("MyAnnotation"); // simplistic check
        });
    }

    /**
     * Injects a method into the given class.
     */
    private void addExtraMethod(JCTree.JCClassDecl classDecl) {
        // public void extraMethod() {
        //     System.out.println("Hello from extraMethod!");
        // }
        //
        // We'll create that using TreeMaker:
        long flags = com.sun.tools.javac.code.Flags.PUBLIC;
        JCTree.JCExpression returnType = treeMaker.TypeIdent(com.sun.tools.javac.code.TypeTag.VOID);

        // Method name
        com.sun.tools.javac.util.Name methodName = names.fromString("extraMethod");

        // Create method body: { System.out.println("Hello from extraMethod!"); }
        JCTree.JCStatement printStatement = makePrintln("Hello from extraMethod!");
        JCTree.JCBlock body = treeMaker.Block(0, com.sun.tools.javac.util.List.of(printStatement));

        // Create method declaration
        JCTree.JCMethodDecl newMethod = treeMaker.MethodDef(
                treeMaker.Modifiers(flags),
                methodName,
                returnType,
                /* type parameters */ com.sun.tools.javac.util.List.nil(),
                /* parameters */ com.sun.tools.javac.util.List.nil(),
                /* thrown exceptions */ com.sun.tools.javac.util.List.nil(),
                body,
                null /* default value for annotation methods */
        );

        // Prepend it to the class definitions
        classDecl.defs = classDecl.defs.prepend(newMethod);

        System.out.println("Added extraMethod() to class: " + classDecl.name);
    }

    private JCTree.JCStatement makePrintln(String msg) {
        // System.out.println("msg");
        // We can build that as: java.lang.System.out.println("msg");

        // Select the System.out field
        JCTree.JCFieldAccess fieldOut = treeMaker.Select(
                treeMaker.Select(
                    treeMaker.Ident(names.fromString("java")),
                    names.fromString("lang")
                ),
                names.fromString("System")
            );
        fieldOut = treeMaker.Select(fieldOut, names.fromString("out"));

        // Build method invocation: out.println("msg")
        JCTree.JCExpression printlnCall = treeMaker.Apply(
                /* type args */ com.sun.tools.javac.util.List.nil(),
                treeMaker.Select(fieldOut, names.fromString("println")),
                com.sun.tools.javac.util.List.of(treeMaker.Literal(msg))
        );

        return treeMaker.Exec(printlnCall);
    }
}
