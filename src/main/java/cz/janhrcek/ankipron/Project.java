package cz.janhrcek.ankipron;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jhrcek
 */
public class Project {

    private static final File ROOT_DIR = new File("/home/jhrcek/Temp/AnkiDeutschPron/");
    private static final File DOWNLOAD_DIR = new File(ROOT_DIR, "Downloaded");
    private static final File WORDS_FOR_WHICH_PRON_NOT_AVAILABLE = new File(ROOT_DIR, "pron_not_available.txt");
    private static final File WORDS_NOT_FOUND = new File(ROOT_DIR, "words_not_found.txt");

    public static File getRootDir() {
        return ROOT_DIR;
    }

    public static File getDownloadDir() {
        return DOWNLOAD_DIR;
    }

    /**
     * @return list of words, which were found on DWDS, but which don't have pronunciation available
     */
    public static List<String> getWordsForWhichPronNotAvailable() throws IOException {
        return FileUtils.readLines(WORDS_FOR_WHICH_PRON_NOT_AVAILABLE);
    }

    /**
     * @return list of words, which cannot be found on DWDS
     */
    public static List<String> getWordsNotFound() throws IOException {
        return FileUtils.readLines(WORDS_NOT_FOUND);
    }

    /**
     * @return a list of words, for which downloaded .mp3 files exist in the download dir
     */
    public static List<String> getWordsDownloaded() {
        List<String> wordsDownloaded = new ArrayList<>();
        File[] mp3FilesInDownloadDir = DOWNLOAD_DIR.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        });
        for (File mp3 : mp3FilesInDownloadDir) {
            String filename = mp3.getName();
            wordsDownloaded.add(filename.substring(0, filename.indexOf(".")));
        }
        return wordsDownloaded;
    }
}
