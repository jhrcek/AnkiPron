package cz.janhrcek.ankipron;

import cz.janhrcek.ankipron.dwds.DWDS;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

import static cz.janhrcek.ankipron.dwds.DWDS.SearchResult.PRON_FOUND;
import static cz.janhrcek.ankipron.dwds.DWDS.SearchResult.PRON_NOT_AVAILABLE;
import static cz.janhrcek.ankipron.dwds.DWDS.SearchResult.WORD_NOT_FOUND;

/**
 *
 */
public class PronDownloader {

    public static final String PROJECT_DIR = "/home/jhrcek/Temp/AnkiDeutschPron/";
    public static final String DOWNLOAD_DIR = PROJECT_DIR + "Downloaded";
    private static final String INPUT_FILE = PROJECT_DIR + "in.txt";

    public void performDownload(List<String> wordsToDownload) {
        System.out.println(wordsToDownload.size() + " words to download");
        Map<String, String> word2pronURL = new HashMap<>();
        List<String> wordsNotFound = new ArrayList<>();
        List<String> pronNotAvailable = new ArrayList<>();
        List<String> unknownErrors = new ArrayList<>();

        DWDS dwds = new DWDS();

        for (String word : wordsToDownload) {
            DWDS.SearchResult sr = dwds.search(word);
            switch (sr) {
                case PRON_FOUND:
                    word2pronURL.put(word, dwds.getPronURL());
                    break;
                case WORD_NOT_FOUND:
                    wordsNotFound.add(word);
                    break;
                case PRON_NOT_AVAILABLE:
                    pronNotAvailable.add(word);
                    break;
                default:
                    unknownErrors.add(word);
            }
        }

        dwds.close();

        //Phase 2: download prons based on URL
        for (Map.Entry<String, String> entry : word2pronURL.entrySet()) {
            downloadAndRenamePronFile(entry.getKey(), entry.getValue());
        }

        System.out.println("----- REPORT -----");
        if (!word2pronURL.keySet().isEmpty()) {
            System.out.printf("OK : %d %s%n", word2pronURL.keySet().size(), word2pronURL.keySet());
        }
        if (!wordsNotFound.isEmpty()) {
            System.out.printf("NOT FOUND : %d %s%n", wordsNotFound.size(), wordsNotFound);
        }
        if (!pronNotAvailable.isEmpty()) {
            System.out.printf("PRON NOT AVAILABLE : %d %s%n", pronNotAvailable.size(), pronNotAvailable);
        }
        if (!unknownErrors.isEmpty()) {
            System.out.printf("UNKNOWN ERRORS : %d %s%n", unknownErrors.size(), unknownErrors);
        }
    }

    private void downloadAndRenamePronFile(String word, String pronUrl) {
        ProcessBuilder pb = new ProcessBuilder("wget", pronUrl);
        pb.directory(new File(DOWNLOAD_DIR));

        Process p;
        try {
            p = pb.start();
            System.out.println("Downloading pronunciation of word '" + word + "' from URL '" + pronUrl + "'");
            p.waitFor();

            String expectedFilename = getSimpleFilename(pronUrl);
            File downloadedPronFile = new File(DOWNLOAD_DIR, expectedFilename);
            System.out.println("Checking for presence of '" + expectedFilename + (downloadedPronFile.exists()
                    ? "' - OK, present" : "' - NOT PRESENT!"));

            downloadedPronFile.renameTo(new File(DOWNLOAD_DIR, word + ".mp3"));

        } catch (IOException | InterruptedException ex) {
            System.err.
                    println("Error downloading pron for '" + word + "' from URL '" + pronUrl + "' because of \n" + ex);
        }
    }

    private static String getSimpleFilename(String url) {
        return url.substring(url.lastIndexOf("/"));
    }

    /**
     * @return a list of words, for which downloaded .mp3 files exist in Donwload dir
     */
    public List<String> getDownloadedProns() {
        List<String> wordsAlreadyDownloaded = new ArrayList<>();
        File[] mp3FilesInDownloadDir = new File(DOWNLOAD_DIR).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        });
        for (File mp3 : mp3FilesInDownloadDir) {
            String filename = mp3.getName();
            wordsAlreadyDownloaded.add(filename.substring(0, filename.indexOf(".")));
        }
        return wordsAlreadyDownloaded;
    }

    /**
     * @return list of words, which were found on DWDS, but which don't have pronunciation available
     */
    public List<String> getWordsForWhichPronNotAvailable() throws IOException {
        return FileUtils.readLines(new File(PROJECT_DIR, "pron_not_available.txt"));
    }

    /**
     * @return list of words, which cannot be found on DWDS
     */
    public List<String> getWordsNotFound() throws IOException {
        return FileUtils.readLines(new File(PROJECT_DIR, "words_not_found.txt"));
    }
}
