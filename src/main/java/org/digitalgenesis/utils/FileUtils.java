package org.digitalgenesis.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {

    /**
     * Reads the content of an InputStream and returns it as a String.
     * @param is the InputStream to read from
     * @return the content of the InputStream as a String
     * @throws IOException if an I/O error occurs
     */
    public static String read(InputStream is) throws IOException {
        StringBuilder out = new StringBuilder();
        int len;
        byte[] buf = new byte[512];
        while((len = is.read(buf))!=-1) {
            out.append(new String(buf, 0, len));
        }
        return out.toString();
    }

    /**
     * Unzips a zip archive to the specified directory.
     * @param archivePath the path to the zip archive
     * @param extractPath the path to extract the files to
     */
    public static void unzipArchive(String archivePath, String extractPath) {
        try {
            try(ZipFile zipFile = new ZipFile(archivePath)) {
                byte[] buf = new byte[1024];
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while(entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if(entry.isDirectory()) {
                        continue;
                    }

                    File newFile = newFile(new File(extractPath), entry);
                    newFile.getParentFile().mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    InputStream input = zipFile.getInputStream(entry);
                    int len;
                    while ((len = input.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                        fos.flush();
                    }
                    fos.close();
                }
            }
        } catch (IOException e) {
            Logger.getInstance().appendToLog(e.getMessage());
        }
    }

    /**
     * Checks if the zip entry is within the destination directory.
     * @param destinationDir the directory to check
     * @param zipEntry the zip entry to check
     * @return the file that would be created by extracting the zip entry
     * @throws IOException when the checks fail
     */

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Compares the SHA1 hash of a file with a given hash.
     * @param f the file to compare
     * @param sourceSHA the SHA1 hash to compare with
     * @return true if the hashes match, false otherwise
     */
    public static boolean compareSHA1(File f, String sourceSHA) {
        try {
            String sha1_dst;
            try (InputStream is = Files.newInputStream(f.toPath())) {
                sha1_dst = new String(Hex.encodeHex(DigestUtils.sha1(is)));
            }
            if (sourceSHA != null) return sha1_dst.equalsIgnoreCase(sourceSHA);
            else return true; // No hash provided

        } catch (IOException e) {
            Logger.getInstance().appendToLog("Issue while comparing SHA1: " + e);
            return false;
        }
    }
}
