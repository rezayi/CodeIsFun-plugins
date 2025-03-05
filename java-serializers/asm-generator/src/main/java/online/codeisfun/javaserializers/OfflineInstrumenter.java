package online.codeisfun.javaserializers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.nio.file.*;

/**
 * OfflineInstrumenter processes all .class files from an input directory,
 * applies ASM transformations using ExtraMethodClassVisitor,
 * and writes the instrumented classes to an output directory.
 */
public class OfflineInstrumenter {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: OfflineInstrumenter <input-directory> <output-directory>");
            System.exit(1);
        }

        Path inputDir = Paths.get(args[0]);
        Path outputDir = Paths.get(args[1]);

        if (!Files.exists(inputDir) || !Files.isDirectory(inputDir)) {
            System.err.println("Invalid input directory: " + inputDir);
            System.exit(1);
        }
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Process each .class file recursively
        Files.walk(inputDir)
             .filter(path -> path.toString().endsWith(".class"))
             .forEach(path -> {
                 try {
                     byte[] originalBytes = Files.readAllBytes(path);

                     // Instrument the class using ASM
                     ClassReader cr = new ClassReader(originalBytes);
                     ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
                     ExtraMethodClassVisitor visitor = new ExtraMethodClassVisitor(cw);
                     cr.accept(visitor, 0);
                     byte[] instrumentedBytes = cw.toByteArray();

                     // Preserve directory structure in the output directory
                     Path relativePath = inputDir.relativize(path);
                     Path outputPath = outputDir.resolve(relativePath);
                     Files.createDirectories(outputPath.getParent());
                     Files.write(outputPath, instrumentedBytes);

                     System.out.println("Instrumented: " + path + " -> " + outputPath);
                 } catch (Exception e) {
                     System.err.println("Error instrumenting file: " + path);
                     e.printStackTrace();
                 }
             });
    }
}