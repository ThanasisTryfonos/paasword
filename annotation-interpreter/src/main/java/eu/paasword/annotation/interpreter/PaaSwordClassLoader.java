package eu.paasword.annotation.interpreter;

import eu.paasword.annotation.interpreter.archive.Archive;
import eu.paasword.annotation.interpreter.util.Util;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

/**
 * Created by smantzouratos on 18/07/16.
 */
public class PaaSwordClassLoader extends ClassLoader {

    public Archive archive;
    public Map<String, Class> loadedClasses;
    private static final Logger logger = Logger.getLogger(PaaSwordClassLoader.class.getName());

    public PaaSwordClassLoader(File archiveFile) throws IOException {
        this(new Archive(new ZipInputStream(new FileInputStream(archiveFile))));
    }

    public PaaSwordClassLoader(Archive archive) {
        this(Thread.currentThread().getContextClassLoader(), archive);
    }

    public PaaSwordClassLoader(ClassLoader parent, Archive archive) {
        super(parent);
        this.loadedClasses = new HashMap();
//        AssertArgument.isNotNull(archive, "archive");
        this.archive = archive;
    }

    @Override
    public Class<?> loadClass(String name) {

        if (name.contains("BOOT-INF")) {
            name = name.substring(17);
        }

        try {

            Class loadedClass = (Class) this.loadedClasses.get(name);
            if (loadedClass != null) {
                return loadedClass;
            } else {
                String resName = "";// this.archive.getEntries().keySet().stream().filter(fName -> fName.contains(name.replace('.', '/') + ".class")).collect(Util.singletonCollector());

                for (String fName : this.archive.getEntries().keySet()) {

                    if (fName.contains(name.replace('.', '/') + ".class")) {
                        resName = fName;
                        break;
                    }

                }

                if (StringUtils.isEmpty(resName)) return super.loadClass(name);
                byte[] classBytes = this.archive.getEntries().get(resName);
                if (classBytes != null) {
                    loadedClass = this.defineClass(name, classBytes, 0, classBytes.length);
                    this.loadedClasses.put(name, loadedClass);
                    return loadedClass;
                } else {
                    return super.loadClass(name);
                }
            }

        } catch (ClassNotFoundException | NoClassDefFoundError e) {
//            e.printStackTrace();
            return null;
        }

    }

    public InputStream getResourceAsStream(String name) {
        byte[] bytes = this.archive.getEntries().get(name);
        return (InputStream) (bytes != null ? new ByteArrayInputStream(bytes) : super.getResourceAsStream(name));
    }

//    public Class<?> loadClass2(String pkgName, String name) throws ClassNotFoundException {
//        if(!name.startsWith(pkgName)) return super.loadClass(name);
//        Class loadedClass = (Class)this.loadedClasses.get(name);
//        if(loadedClass != null) {
//            return loadedClass;
//        } else {
//            this.archive.getEntries().keySet().forEach(en ->{
//
//            });
//            String resName = this.archive.getEntries().keySet().stream().filter( fName-> fName.contains(name.replace('.', '/') + ".class")).collect(Util.singletonCollector());
//            //if(StringUtils.isEmpty(resName)) return super.loadClass(name);
//            byte[] classBytes = this.archive.getEntryBytes(resName);
//            if(classBytes != null) {
//                loadedClass = this.defineClass(name, classBytes, 0, classBytes.length);
//                this.loadedClasses.put(name, loadedClass);
//                return loadedClass;
//            } else {
//                return super.loadClass(name);
//            }
//        }
//    }
//
//
//    protected URL findResource(String resName) {
//        URL resource = this.archive.getEntryURL(resName);
//        return resource != null?resource:this.getParent().getResource(resName);
//    }
//
//    protected Enumeration<URL> findResources(String resName) throws IOException {
//        ArrayList resources = new ArrayList();
//        URL resource = this.archive.getEntryURL(resName);
//        if(resource != null) {
//            resources.add(resource);
//        }
//
//        Enumeration parentResource = this.getParent().getResources(resName);
//        resources.addAll(Collections.list(parentResource));
//        return Collections.enumeration(resources);
//    }

}
