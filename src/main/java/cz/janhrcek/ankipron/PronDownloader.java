package cz.janhrcek.ankipron;

import static cz.janhrcek.ankipron.search.SearchResult.PRON_FOUND;
import static cz.janhrcek.ankipron.search.SearchResult.PRON_NOT_AVAILABLE;
import static cz.janhrcek.ankipron.search.SearchResult.WORD_NOT_FOUND;

import cz.janhrcek.ankipron.search.SearchResult;
import cz.janhrcek.ankipron.search.Searcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PronDownloader {

    private final Searcher searcher;

    public PronDownloader(Searcher searcher) {
        this.searcher = searcher;
    }

    public void performDownload(List<String> wordsToDownload) {
        System.out.println(wordsToDownload.size() + " words to download");
        Map<String, String> word2pronURL = new HashMap<>();
        List<String> wordsNotFound = new ArrayList<>();
        List<String> pronNotAvailable = new ArrayList<>();
        List<String> unknownErrors = new ArrayList<>();

        for (String word : wordsToDownload) {
            SearchResult sr = searcher.search(word);
            switch (sr) {
                case PRON_FOUND:
                    word2pronURL.put(word, searcher.getPronURL());
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

        searcher.close();

        //Phase 2: download prons based on URL
        List<String> failedRenames = new ArrayList<>();
        for (Map.Entry<String, String> entry : word2pronURL.entrySet()) {
            boolean downloadAndRenameSuccesfull = downloadAndRenamePronFile(entry.getKey(), entry.getValue());
            if (!downloadAndRenameSuccesfull) {
                failedRenames.add(entry.getKey());
            }
        }

        System.out.println("----- REPORT -----");
        reportCollection("OK", word2pronURL.keySet());
        reportCollection("NOT FOUND", wordsNotFound);
        reportCollection("PRON NOT AVAILABLE", pronNotAvailable);
        reportCollection("UNKNOWN ERRORS", unknownErrors);
        reportCollection("FAILED TO RENAME", failedRenames);
    }

    private void reportCollection(String collectionName, Collection<String> collection) {
        if (!collection.isEmpty()) {
            System.out.printf("%s : %d %s%n", collectionName, collection.size(), collection);
        }
    }

    private boolean downloadAndRenamePronFile(String word, String pronUrl) {
        ProcessBuilder pb = new ProcessBuilder("wget", pronUrl);
        pb.directory(Project.getDownloadDir().toFile());

        boolean dldAndRenameSuccessfull = false;
        try {
            Process wget = pb.start();
            System.out.printf("Downloading '%s'%n", pronUrl);
            wget.waitFor();

            rename(word, pronUrl);

            dldAndRenameSuccessfull = true;
        } catch (IOException | InterruptedException ex) {
            System.err.printf("Error downloading pron for '%s' from URL '%s' :%s%n", word, pronUrl, ex);
        }
        return dldAndRenameSuccessfull;
    }

    private void rename(String word, String pronUrl) throws IOException {
        String fileName = extractFileName(pronUrl);
        Path from = Project.getDownloadDir().resolve(fileName);
        Path to = Project.getDownloadDir().resolve(word + ".mp3");
        System.out.printf("Renaming '%s' to '%s'%n", from, to);
        Files.move(from, to);
    }

    private static String extractFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
