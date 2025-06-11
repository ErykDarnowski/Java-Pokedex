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
        try {
            if (!TMP.exists()) {
                boolean created = TMP.mkdirs();
                if (!created) {
                    System.err.println("Warning: Could not create tmp directory");
                }
            }
        } catch (SecurityException e) {
            ErrorHandler.showError(null, e, "tworzenie katalogu tymczasowego");
        }
    }

    public static ImageIcon load(String id) throws Exception {
        File file = new File(TMP, id + ".png");
        if (!file.exists()) {
            try {
                download(id, file);
            } catch (Exception e) {
                // Re-throw with more context for better error handling upstream
                throw new Exception("Nie można pobrać obrazu dla Pokémona #" + id, e);
            }
        }
        
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                throw new Exception("Plik obrazu jest uszkodzony: " + file.getName());
            }
            return new ImageIcon(img);
        } catch (Exception e) {
            // If file is corrupted, try to re-download
            if (file.exists()) {
                file.delete();
                return load(id); // Recursive call to re-download
            }
            throw new Exception("Nie można wczytać obrazu dla Pokémona #" + id, e);
        }
    }

    // OPTIMIZATION: New method to load pre-scaled images
    public static ImageIcon loadScaled(String id, int size) throws Exception {
        String cacheKey = id + "_" + size;
        ImageIcon cached = SCALED_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            ImageIcon original = load(id);
            Image scaled = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaled);
            
            SCALED_CACHE.put(cacheKey, scaledIcon);
            return scaledIcon;
        } catch (Exception e) {
            throw new Exception("Nie można przeskalować obrazu dla Pokémona #" + id, e);
        }
    }

    public static CompletableFuture<ImageIcon> loadAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try { 
                return load(id);
            } catch (Exception e) { 
                // Log error but don't show dialog in async context
                System.err.println("Failed to load image for Pokemon #" + id + ": " + e.getMessage());
                return null; 
            }
        }, EXEC);
    }

    // OPTIMIZATION: Async method for pre-scaled images
    public static CompletableFuture<ImageIcon> loadScaledAsync(String id, int size) {
        return CompletableFuture.supplyAsync(() -> {
            try { 
                return loadScaled(id, size); 
            } catch (Exception e) { 
                // Log error but don't show dialog in async context
                System.err.println("Failed to load scaled image for Pokemon #" + id + ": " + e.getMessage());
                return null; 
            }
        }, EXEC);
    }

    private static void download(String id, File dest) throws Exception {
        String[] paths = {
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%s.png",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/%s.png",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%s.png"
        };
        
        Exception lastException = null;
        
        for (String p : paths) {
            try {
                URL url = new URL(String.format(p, id));
                BufferedImage img = ImageIO.read(url);
                if (img != null) {
                    boolean written = ImageIO.write(img, "png", dest);
                    if (written) {
                        return; // Success
                    } else {
                        throw new Exception("Nie można zapisać obrazu do pliku");
                    }
                }
            } catch (Exception e) {
                lastException = e;
                // Continue to next URL
            }
        }
        
        // If we get here, all attempts failed
        if (lastException != null) {
            throw new Exception("Nie można pobrać obrazu dla Pokémona #" + id + " z żadnego źródła", lastException);
        } else {
            throw new Exception("Nie można pobrać obrazu dla Pokémona #" + id + " - wszystkie źródła zwróciły puste dane");
        }
    }
}