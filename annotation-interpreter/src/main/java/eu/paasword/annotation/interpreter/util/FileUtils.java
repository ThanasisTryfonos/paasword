package eu.paasword.annotation.interpreter.util;

import java.io.*;

/**
 * Created by smantzouratos on 18/07/16.
 */
public class FileUtils {

    public static void copyFile(String from, String to) throws IOException {
        File fromFile = new File(from);
        File toFile = new File(to);

        writeFile(readFile(fromFile), toFile);
    }

    /**
     * Read the contents of the specified file.
     * @param file The file to read.
     * @return The file contents.
     * @throws IOException Error readiong file.
     */
    public static byte[] readFile(File file) throws IOException {
//        AssertArgument.isNotNull(file, "file");

        if(!file.exists()) {
            throw new IllegalArgumentException("No such file '" + file.getAbsoluteFile() + "'.");
        } else if(file.isDirectory()) {
            throw new IllegalArgumentException("File '" + file.getAbsoluteFile() + "' is a directory.  Cannot read.");
        }

        InputStream stream = new FileInputStream(file);
        try {
            return StreamUtils.readStream(stream);
        } finally {
            stream.close();
        }
    }

    public static void writeFile(byte[] bytes, File file) throws IOException {
        if(file.isDirectory()) {
            throw new IllegalArgumentException("File '" + file.getAbsoluteFile() + "' is an existing directory.  Cannot write.");
        }

        File folder = file.getParentFile();
        if(folder != null && !folder.exists()) {
            folder.mkdirs();
        }

        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(bytes);
            stream.flush();
        } finally {
            stream.close();
        }
    }

}
