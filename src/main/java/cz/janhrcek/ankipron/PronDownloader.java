package cz.janhrcek.ankipron;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PronDownloader {

    public void performDownload(Map<String, String> word2pronURL) {
        List<String> failedRenames = new ArrayList<>();
        for (Map.Entry<String, String> entry : word2pronURL.entrySet()) {
            boolean downloadAndRenameSuccesfull = downloadAndRenamePronFile(entry.getKey(), entry.getValue());
            if (!downloadAndRenameSuccesfull) {
                failedRenames.add(entry.getKey());
            }
        }
        if (failedRenames.size() > 0) {
            System.out.printf("FAILED RENAMES : %d %s%n", failedRenames.size(), failedRenames);
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
            System.err.printf("ERROR: failed to download pron for '%s' from URL '%s' :%s%n", word, pronUrl, ex);
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
