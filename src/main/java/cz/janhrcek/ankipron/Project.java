package cz.janhrcek.ankipron;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {

    private static final Path ROOT_DIR = Paths.get(System.getProperty("user.dir"));
    private static final Path DOWNLOAD_DIR = ROOT_DIR.resolve("Downloaded");
    private static final Path ANKI_DB = ROOT_DIR.resolve("collection.anki2");

    public static Path getDownloadDir() {
        return DOWNLOAD_DIR;
    }

    public static Path getAnkiDb() {
        return ANKI_DB;
    }

    /**
     * @return list of words, which were found on DWDS, but which don't have pronunciation available
     */
    public static List<String> getWordsForWhichPronNotAvailable() {
        return readLines("pron_not_available.txt");
    }

    /**
     * @return list of words, which cannot be found on DWDS
     */
    public static List<String> getWordsNotFound() {
        return readLines("words_not_found.txt");
    }

    /**
     * @return a list of words, for which downloaded .mp3 files exist in the download dir
     */
    public static List<String> getWordsDownloaded() throws IOException {
        List<String> wordsDownloaded = new ArrayList<>();
        DirectoryStream<Path> mp3FilesInDownloadDir = Files.newDirectoryStream(DOWNLOAD_DIR, "*.mp3");
        for (Path mp3 : mp3FilesInDownloadDir) {
            String filename = mp3.getFileName().toString();
            wordsDownloaded.add(filename.substring(0, filename.indexOf('.')));
        }
        return wordsDownloaded;
    }

    private static List<String> readLines(String resource) {
        try {
            /* http://stackoverflow.com/questions/25032716/getting-filesystemnotfoundexception-from-zipfilesystemprovider-when-creating-a-p
             Have to create ZipFilesystem in order to be able to read from resource files packaged in executable jar file.
             */
            URI resourceUri = Project.class.getResource(resource).toURI();
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            try (FileSystem fs = FileSystems.newFileSystem(resourceUri, env)) {
                Path resPath = Paths.get(resourceUri);
                return Files.readAllLines(resPath, StandardCharsets.UTF_8);
            }
        } catch (IOException | URISyntaxException ex) {
            throw new IllegalStateException("Failed to read resource " + resource);
        }
    }
}
