package cz.janhrcek.ankipron;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author jhrcek
 */
public class Project {

    private static final File ROOT_DIR = new File("/home/jhrcek/Temp/AnkiDeutschPron/");
    private static final File DOWNLOAD_DIR = new File(ROOT_DIR, "Downloaded");
    private static final File ANKI_DB = new File(ROOT_DIR, "collection.anki2");

    public static File getRootDir() {
        return ROOT_DIR;
    }

    public static File getDownloadDir() {
        return DOWNLOAD_DIR;
    }

    public static File getAnkiDb() {
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

    private static List<String> readLines(String resouce) {
        Scanner scanner = new Scanner(Project.class.getResourceAsStream(resouce));
        List<String> lines = new ArrayList<>(500);

        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }

        return lines;
    }
}
