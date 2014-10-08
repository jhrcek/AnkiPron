package cz.janhrcek.ankipron;

import com.google.common.base.Charsets;
import cz.janhrcek.ankipron.search.DWDS;
import cz.janhrcek.ankipron.search.Searcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.firefox.FirefoxDriver;

public class HappyClient {

    public static void main(String[] args) {
        Path infile = checkArgs(args);
        List<String> words = readWords(infile);
        Path downloadDir = getDownloadDir();

        try (Searcher searcher = new DWDS(new FirefoxDriver())) {
            Map<String, String> searchResult = searcher.batchSearch(words);

            PronDownloader downloader = new PronDownloader(downloadDir);
            downloader.performDownload(searchResult);
        }
    }

    private static Path checkArgs(String[] args) {
        if (args.length < 1) {
            System.err.printf("ERROR: missing file argument. Usage:%njava -jar dwdown.jar <filename>%n");
            System.exit(1);
        }
        Path infile = Paths.get(args[0]);
        if (!Files.exists(infile)) {
            System.err.printf("ERROR: file '%s' does not exist!%n", infile);
            System.exit(1);
        }
        return infile;
    }

    private static List<String> readWords(Path infile) {
        List<String> words = null;
        try {
            words = Files.readAllLines(infile, Charsets.UTF_8);
        } catch (IOException ioe) {
            System.err.printf("ERROR: can't read words from file %s: %s%n", infile, ioe.getMessage());
            System.exit(1);
        }
        //Remove empty lines
        for (Iterator<String> it = words.iterator(); it.hasNext();) {
            if (it.next().isEmpty()) {
                it.remove();
            }
        }
        return words;
    }

    private static Path getDownloadDir() {
        Path dir = Paths.get("./downloads");
        if (Files.notExists(dir)) {
            try {
                Files.createDirectory(dir);
            } catch (IOException ex) {
                System.err.printf("ERROR: failed to create download dir: %s%n", ex.getMessage());
            }
        }
        return dir;
    }
}
