package org.digitalgenesis.installer.core;

import org.digitalgenesis.utils.downloadUtils.DownloadManager;
import org.digitalgenesis.utils.downloadUtils.DownloadUtils;
import org.digitalgenesis.utils.FileUtils;
import org.digitalgenesis.utils.Logger;
import org.digitalgenesis.utils.WebAPIUtils;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;

/** Methods for installing the core parts of Minecraft.*/
public class CoreInstaller {

    /**
     * Downloads the Minecraft client file.
     * @param versionInfo
     * @param gameDir
     * @return Path to client file
     * @throws IOException
     */
    public static String installClient(VersionInfo versionInfo, String gameDir) throws IOException {
        File clientFile = new File(gameDir + "/versions/" + versionInfo.id + "/" + versionInfo.id + ".jar");

        try {
            for (int i = 0; i < 5; i++) {
                if (i == 4) throw new RuntimeException("Client download failed after 5 retries");

                if (!clientFile.exists()) {
                    DownloadUtils.downloadFile(versionInfo.downloads.client.url, clientFile, new DownloadManager(1));
                } else if (FileUtils.compareSHA1(clientFile, versionInfo.downloads.client.sha1)) {
                    clientFile.delete();
                    DownloadUtils.downloadFile(versionInfo.downloads.client.url, clientFile, new DownloadManager(1));
                }

                // Check if the downloaded client matches the expected SHA1 hash
                if (FileUtils.compareSHA1(clientFile, versionInfo.downloads.client.sha1)) {
                    Logger.getInstance().appendToLog("Client downloaded");
                    return clientFile.getAbsolutePath();
                }
            }
        } catch (IOException e) {
            Logger.getInstance().appendToLog("Failed to download client: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /** Installs the required libraries for Minecraft.
     * Will only download a library if it is missing, however it will overwrite it if sha1 does not match the downloaded library.
     * @param versionInfo
     * @param gameDir
     * @return Classpath of the downloaded libraries
     * @throws IOException
     */
    public static String installLibraries(VersionInfo versionInfo, String gameDir) throws IOException {
        Logger.getInstance().appendToLog("Checking Libraries for: " + versionInfo.id);
        StringJoiner classpath = new StringJoiner(File.pathSeparator);

        for (VersionInfo.Library library : versionInfo.libraries) {
            for (int i = 0; i < 5; i++) {
                if (i == 4) throw new RuntimeException(String.format("Library download of %s failed after 5 retries", library.name));

                File libraryFile;
                String sha1;

                // Null means mod loader library, otherwise vanilla library
                if (library.downloads == null) {
                    String path = parseLibraryNameToPath(library.name);
                    libraryFile = new File(gameDir + "/libraries/", path);
                    sha1 = WebAPIUtils.getRaw(library.url + path + ".sha1");
                    if (!libraryFile.exists()) {
                        Logger.getInstance().appendToLog("Downloading: " + library.name);
                        DownloadUtils.downloadFile(library.url + path, libraryFile, new DownloadManager(1));
                    }
                } else {
                    VersionInfo.Library.Artifact artifact = library.downloads.artifact;
                    libraryFile = new File(gameDir + "/libraries/", artifact.path);
                    sha1 = artifact.sha1;
                    if (!libraryFile.exists()) {
                        Logger.getInstance().appendToLog("Downloading: " + library.name);
                        DownloadUtils.downloadFile(artifact.url, libraryFile, new DownloadManager(1));
                    }
                }

                if(FileUtils.compareSHA1(libraryFile, sha1)) {
                    classpath.add(libraryFile.getAbsolutePath());
                    break;
                }
            }
        }

        Logger.getInstance().appendToLog("Libraries installed");
        return classpath.toString();
    }

    /**
     * Parses mod loader library name to path.
     * @param libraryName Name of the library to parse
     */
    private static String parseLibraryNameToPath(String libraryName) {
        String[] parts = libraryName.split(":");
        String location = parts[0].replace(".", "/");
        String name = parts[1];
        String version = parts[2];

        return String.format("%s/%s/%s/%s", location, name, version, name + "-" + version + ".jar");
    }
}
