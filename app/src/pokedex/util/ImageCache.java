package pokedex.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.concurrent.*;

public class ImageCache {

    private static final File TMP = new File("tmp");
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

    static {
        if (!TMP.exists()) TMP.mkdirs();
    }

    public static ImageIcon load(String id) throws Exception {
        File file = new File(TMP, id + ".png");
        if (!file.exists()) download(id, file);
        BufferedImage img = ImageIO.read(file);
        return new ImageIcon(img);
    }

    public static CompletableFuture<ImageIcon> loadAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try { return load(id);} catch (Exception e) { return null; }
        }, EXEC);
    }

    private static void download(String id, File dest) throws Exception {
        String[] paths = {
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%s.png",
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/%s.png",
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%s.png"
        };
        for (String p : paths) {
            try {
                URL url = new URL(String.format(p, id));
                BufferedImage img = ImageIO.read(url);
                if (img != null) {
                    ImageIO.write(img, "png", dest);
                    return;
                }
            } catch (Exception ignored) {}
        }
        throw new Exception("Unable to download image for id " + id);
    }
}