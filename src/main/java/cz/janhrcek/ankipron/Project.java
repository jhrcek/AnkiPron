package cz.janhrcek.ankipron;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Project {

    private static final Path ROOT_DIR = Paths.get("/home/jhrcek/Temp/AnkiDeutschPron/");
    private static final Path DOWNLOAD_DIR = ROOT_DIR.resolve("Downloaded");
    private static final Path ANKI_DB = ROOT_DIR.resolve("collection.anki2");

    public static Path getRootDir() {
        return ROOT_DIR;
    }

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
            wordsDownloaded.add(filename.substring(0, filename.indexOf(".")));
        }
        return wordsDownloaded;
    }

    private static List<String> readLines(String resource) {
        try {
            Path resPath = Paths.get(Project.class.getResource(resource).toURI());
            return Files.readAllLines(resPath, StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException ex) {
            throw new IllegalStateException("Failed to read resource " + resource);
        }
    }
}
