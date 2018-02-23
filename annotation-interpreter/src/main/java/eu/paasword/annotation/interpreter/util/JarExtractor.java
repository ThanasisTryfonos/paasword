package eu.paasword.annotation.interpreter.util;

import com.comoyo.emjar.EmJarClassLoader;
import eu.paasword.annotation.interpreter.PaaSwordClassLoader;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * Created by smantzouratos on 19/07/16.
 */
public class JarExtractor {

    private static final Logger logger = Logger.getLogger(JarExtractor.class.getName());

    /**
     * @param args the first arg should be the path to the parent jar file, and
     *             the second should be the directory where child jar files are
     *             extracted.
     */
    public static void main(String[] args) {
        // JarExtractor ex = new JarExtractor(args[0], args[1]);
        // For this demo, args are prefixed.
        JarExtractor ex = new JarExtractor("a.jar", "extracted");
        ex.parseJarFile();
    }

    private String parentPath = null;
    private String extractPath = ".";

    /**
     * @param parentPath  the path to the parent jar file
     * @param extractPath the path to the directory where child files being extracted
     */
    JarExtractor(String parentPath, String extractPath) {
        this.parentPath = parentPath;
        this.extractPath = extractPath;
    }

    /**
     * @param parentJar the parent jar file
     * @param extractee the file to be extracted
     * @return the extracted jar file
     * @throws IOException
     */
    public static File extractJarFileFromJar(final JarFile parentJar,
                                             final ZipEntry extractee) throws IOException {
        BufferedInputStream is = new BufferedInputStream(parentJar
                .getInputStream(extractee));

        String originalFilename = extractee.getName().substring(0, extractee.getName().lastIndexOf("."));
        String suffix = extractee.getName().substring(extractee.getName().lastIndexOf("."));

        File f = File.createTempFile(originalFilename, suffix); //new File("extracted" + File.separator + extractee.getName());

        String parentName = f.getParent();
        if (parentName != null) {
            File dir = new File(parentName);
            dir.mkdirs();
        }
        BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(f));

        int c;
        while ((c = is.read()) != -1) {
            os.write((byte) c);
        }
        is.close();
        os.close();

        return f;
    }

    /**
     * the default parseJarFile method
     */
    private void parseJarFile() {
        parseJarFile(new File(parentPath), null, null);
    }

    /**
     * Parses the jar file.
     *
     * @param file the file to be parsed, which should be jar file, otherwise, an
     *             ioexception will be thrown.
     */
    public static URLClassLoader parseJarFile(final File file, Map<String, Class<?>> discoveredClasses, String packageName) {
        if (file == null) {
            throw new RuntimeException("file is null.");
        }

        JarFile jarFile = null;
        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);

        try {

            URL[] urls = {new URL(suffix + ":file:" + file + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            jarFile = new JarFile(file);

            Enumeration entries = jarFile.entries();

            PaaSwordClassLoader paaSwordClassLoader = new PaaSwordClassLoader(file);

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String entryName = entry.toString();
                if (entryName == null) {
                    continue;
                } else if (entryName.endsWith(".jar") || entryName.endsWith(".war") || entryName.endsWith(".ear")) {
                    // Found a child jar file inside the parent.
                    File f = extractJarFileFromJar(jarFile, entry);
                    if (f != null) {
                        // Try to extract descendant jar files from the child
                        // jar recursively.
                        parseJarFile(f, discoveredClasses, packageName);
                    }
                } else {

                    if (entryName.endsWith(".class")) {

                        String className = entryName.substring(0, entryName.length() - 6);
                        className = className.replace('/', '.');

                        if (className.contains(packageName)) {
                            logger.info("Entry: " + className);

                            Class loadedClazz = paaSwordClassLoader.loadClass(className);

                            if (null != loadedClazz) {
                                logger.info("Discovered class: " + className);

                                if (className.contains("BOOT-INF")) {
                                    className = className.substring(17);
                                }

                                discoveredClasses.put(className, loadedClazz);

                            }

                        }
                    }
                }
            } // EoW

            return cl;

        } catch (IOException e) {
            logger.severe(e.getMessage());
            return null;
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                    return null;
                }
            }
        }


    }

}
