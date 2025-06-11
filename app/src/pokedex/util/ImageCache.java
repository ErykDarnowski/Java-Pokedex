package pokedex.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageCache {

    private static final File TMP = new File("tmp");
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors()));
    
    // OPTIMIZATION: Cache for pre-scaled images to avoid repeated scaling
    private static final Map<String, ImageIcon> SCALED_CACHE = new ConcurrentHashMap<>();
    private static final int SCALED_SIZE = 130;

    static {
        if (!TMP.exists()) TMP.mkdirs();
    }

    public static ImageIcon load(String id) throws Exception {
        File file = new File(TMP, id + ".png");
        if (!file.exists()) download(id, file);
        BufferedImage img = ImageIO.read(file);
        return new ImageIcon(img);
    }

    // OPTIMIZATION: New method to load pre-scaled images
    public static ImageIcon loadScaled(String id, int size) throws Exception {
        String cacheKey = id + "_" + size;
        ImageIcon cached = SCALED_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        ImageIcon original = load(id);
        Image scaled = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaled);
        
        SCALED_CACHE.put(cacheKey, scaledIcon);
        return scaledIcon;
    }

    public static CompletableFuture<ImageIcon> loadAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try { return load(id);} catch (Exception e) { return null; }
        }, EXEC);
    }

    // OPTIMIZATION: Async method for pre-scaled images
    public static CompletableFuture<ImageIcon> loadScaledAsync(String id, int size) {
        return CompletableFuture.supplyAsync(() -> {
            try { return loadScaled(id, size); } catch (Exception e) { return null; }
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