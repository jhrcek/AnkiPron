package cz.janhrcek.ankipron;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class PronDownloader {

    private final Path downloadDir;
    private static final String URLS = "urls.txt";

    public PronDownloader(Path downloadDir) {
        this.downloadDir = Objects.requireNonNull(downloadDir);
    }

    public void performDownload(Map<String, String> word2pronURL) {
        writeWordsToFile(word2pronURL.values());
        downloadFiles();
        renameDownloadedFiles(word2pronURL);
    }

    private void downloadFiles() {
        ProcessBuilder pb = new ProcessBuilder("wget",
                "--quiet", "--directory-prefix", downloadDir.toString(), "--input-file", URLS);
        pb.directory(new File("."));
        System.out.println("----- PHASE 3: Download mp3s -----");
        System.out.printf("Running '%s'%n", pb.command().toString().replaceAll("[\\[\\],]", ""));
        try {

            Process wget = pb.start();
            wget.waitFor();
            int wgetExitValue = wget.exitValue();
            if (wgetExitValue != 0) {
                throw new IllegalStateException("ERROR: wget exited with value " + wgetExitValue);
            }
        } catch (IOException | InterruptedException ex) {
            System.err.printf("ERROR: error downloading files using wget: %s%n", ex.getMessage());
        }
    }

    private void writeWordsToFile(Collection<String> words) {
        System.out.printf("----- PHASE 2: write %d URLs to %s -----%n", words.size(), URLS);
        Path urlsFile = Paths.get(URLS);
        try {
            Files.write(urlsFile, words, Charset.forName("UTF-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            System.err.printf("ERROR: Failed writing URLs to file %s:%s%n", urlsFile, ex.getMessage());
            System.exit(1);
        }
    }

    private void renameDownloadedFiles(Map<String, String> word2pronURL) {
        System.out.println("----- PHASE 4: rename mp3s -----");
        for (Map.Entry<String, String> entry : word2pronURL.entrySet()) {
            try {
                rename(entry.getKey(), entry.getValue());
            } catch (IOException ex) {
                System.err.printf("ERROR: failed to rename %s: %s%n", entry.getKey(), ex.getMessage());
            }
        }
    }

    private void rename(String word, String pronUrl) throws IOException {
        String fileName = extractFileName(pronUrl);
        Path from = downloadDir.resolve(fileName);
        Path to = downloadDir.resolve(word + ".mp3");
        System.out.printf("Renaming '%s' to '%s'%n", from.getFileName(), to.getFileName());
        Files.move(from, to);
    }

    private static String extractFileName(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
