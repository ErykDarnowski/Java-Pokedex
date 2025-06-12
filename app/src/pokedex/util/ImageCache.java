package pokedex.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe image caching utility for Pokemon sprites.
 * Handles downloading, caching, and scaling of Pokemon images with fallback URLs.
 * Provides both synchronous and asynchronous loading capabilities.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public final class ImageCache {

    private static final File CACHE_DIR = new File("tmp");
    private static final String PLACEHOLDER_EXTENSION = ".placeholder";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors()));
    
    private static final Map<String, ImageIcon> SCALED_CACHE = new ConcurrentHashMap<>();
    
    // Pokemon sprite URLs in order of preference (highest quality first)
    private static final String[] SPRITE_URLS = {
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%s.png",
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/%s.png",
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%s.png"
    };

    static {
        initializeCacheDirectory();
    }

    /**
     * Prevents instantiation of this utility class.
     */
    private ImageCache() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Loads a Pokemon image synchronously. Downloads if not cached locally.
     * 
     * @param pokemonId the Pokemon ID
     * @return ImageIcon for the Pokemon, or null if loading fails or placeholder exists
     * @throws Exception if image cannot be loaded or downloaded
     */
    public static ImageIcon load(String pokemonId) throws Exception {
        validatePokemonId(pokemonId);
        
        File cachedFile = getCacheFile(pokemonId);
        File placeholderFile = getPlaceholderFile(pokemonId);
        
        // Check if we have a placeholder indicating previous failed downloads
        if (placeholderFile.exists()) {
            logError("Skipping download for Pokemon #" + pokemonId + " - placeholder exists (previous download failed)", null);
            return null;
        }
        
        if (!cachedFile.exists()) {
            try {
                downloadImage(pokemonId, cachedFile);
            } catch (Exception e) {
                // Create placeholder file to prevent future download attempts
                createPlaceholderFile(placeholderFile, pokemonId);
                throw e;
            }
        }
        
        return loadImageFromFile(cachedFile, pokemonId);
    }

    /**
     * Loads and scales a Pokemon image synchronously.
     * 
     * @param pokemonId the Pokemon ID
     * @param targetSize the desired image size
     * @return scaled ImageIcon, or null if loading fails
     * @throws Exception if image cannot be loaded or scaled
     */
    public static ImageIcon loadScaled(String pokemonId, int targetSize) throws Exception {
        String cacheKey = createScaledCacheKey(pokemonId, targetSize);
        
        ImageIcon cached = SCALED_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        ImageIcon original = load(pokemonId);
        if (original == null) {
            return null; // Don't cache null results
        }
        
        ImageIcon scaled = scaleImage(original, targetSize);
        SCALED_CACHE.put(cacheKey, scaled);
        return scaled;
    }

    /**
     * Loads a Pokemon image asynchronously.
     * 
     * @param pokemonId the Pokemon ID
     * @return CompletableFuture that resolves to ImageIcon or null
     */
    public static CompletableFuture<ImageIcon> loadAsync(String pokemonId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return load(pokemonId);
            } catch (Exception e) {
                logError("Failed to load image for Pokemon #" + pokemonId, e);
                return null;
            }
        }, EXECUTOR);
    }

    /**
     * Loads and scales a Pokemon image asynchronously.
     * 
     * @param pokemonId the Pokemon ID
     * @param targetSize the desired image size
     * @return CompletableFuture that resolves to scaled ImageIcon or null
     */
    public static CompletableFuture<ImageIcon> loadScaledAsync(String pokemonId, int targetSize) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadScaled(pokemonId, targetSize);
            } catch (Exception e) {
                logError("Failed to load scaled image for Pokemon #" + pokemonId, e);
                return null;
            }
        }, EXECUTOR);
    }

    /**
     * Removes the placeholder file for a Pokemon, allowing download attempts to resume.
     * Useful for retrying downloads after network issues are resolved.
     * 
     * @param pokemonId the Pokemon ID
     * @return true if placeholder was removed or didn't exist, false if removal failed
     */
    public static boolean removePlaceholder(String pokemonId) {
        validatePokemonId(pokemonId);
        File placeholderFile = getPlaceholderFile(pokemonId);
        
        if (!placeholderFile.exists()) {
            return true; // No placeholder to remove
        }
        
        boolean removed = placeholderFile.delete();
        if (removed) {
            logError("Removed placeholder for Pokemon #" + pokemonId + " - downloads will be retried", null);
        }
        return removed;
    }

    /**
     * Checks if a Pokemon has a placeholder file (indicating previous download failure).
     * 
     * @param pokemonId the Pokemon ID
     * @return true if placeholder exists, false otherwise
     */
    public static boolean hasPlaceholder(String pokemonId) {
        validatePokemonId(pokemonId);
        return getPlaceholderFile(pokemonId).exists();
    }

    /**
     * Shuts down the executor service gracefully.
     * Should be called when the application is closing.
     */
    public static void shutdown() {
        try {
            EXECUTOR.shutdown();
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Clears the scaled image cache to free memory.
     */
    public static void clearCache() {
        SCALED_CACHE.clear();
    }

    /**
     * Returns the current cache size for monitoring.
     * 
     * @return number of cached scaled images
     */
    public static int getCacheSize() {
        return SCALED_CACHE.size();
    }

    /**
     * Initializes the cache directory if it doesn't exist.
     */
    private static void initializeCacheDirectory() {
        try {
            if (!CACHE_DIR.exists() && !CACHE_DIR.mkdirs()) {
                System.err.println("Warning: Could not create cache directory: " + CACHE_DIR.getPath());
            }
        } catch (SecurityException e) {
            ErrorHandler.showError(null, e, "tworzenie katalogu cache");
        }
    }

    /**
     * Downloads a Pokemon image from available URLs.
     */
    private static void downloadImage(String pokemonId, File destination) throws Exception {
        Exception lastException = null;
        
        for (String urlTemplate : SPRITE_URLS) {
            try {
                URL imageUrl = new URL(String.format(urlTemplate, pokemonId));
                BufferedImage image = ImageIO.read(imageUrl);
                
                if (image != null && ImageIO.write(image, "png", destination)) {
                    return; // Success
                }
            } catch (Exception e) {
                lastException = e;
                // Continue to next URL
            }
        }
        
        throw new Exception("Failed to download image for Pokemon #" + pokemonId + 
                          " from all sources", lastException);
    }

    /**
     * Creates a placeholder file to indicate that download attempts have failed.
     * This prevents repeated failed download attempts.
     */
    private static void createPlaceholderFile(File placeholderFile, String pokemonId) {
        try {
            if (placeholderFile.createNewFile()) {
                logError("Created placeholder for Pokemon #" + pokemonId + " - future downloads will be skipped", null);
            }
        } catch (IOException e) {
            logError("Failed to create placeholder file for Pokemon #" + pokemonId, e);
        }
    }

    /**
     * Loads an ImageIcon from a cached file with error recovery.
     */
    private static ImageIcon loadImageFromFile(File file, String pokemonId) throws Exception {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                // File might be corrupted, try re-downloading
                file.delete();
                downloadImage(pokemonId, file);
                image = ImageIO.read(file);
            }
            
            if (image == null) {
                throw new Exception("Image file is corrupted and re-download failed");
            }
            
            return new ImageIcon(image);
        } catch (Exception e) {
            throw new Exception("Failed to load image for Pokemon #" + pokemonId, e);
        }
    }

    /**
     * Scales an ImageIcon to the specified size.
     */
    private static ImageIcon scaleImage(ImageIcon original, int targetSize) {
        if (original == null) {
            return null;
        }
        
        Image scaledImage = original.getImage().getScaledInstance(
            targetSize, targetSize, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    /**
     * Creates a cache key for scaled images.
     */
    private static String createScaledCacheKey(String pokemonId, int size) {
        return pokemonId + "_" + size;
    }

    /**
     * Returns the cache file for a given Pokemon ID.
     */
    private static File getCacheFile(String pokemonId) {
        return new File(CACHE_DIR, pokemonId + ".png");
    }

    /**
     * Returns the placeholder file for a given Pokemon ID.
     */
    private static File getPlaceholderFile(String pokemonId) {
        return new File(CACHE_DIR, pokemonId + PLACEHOLDER_EXTENSION);
    }

    /**
     * Validates the Pokemon ID parameter.
     */
    private static void validatePokemonId(String pokemonId) {
        if (pokemonId == null || pokemonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Pokemon ID cannot be null or empty");
        }
    }

    /**
     * Logs errors without showing UI dialogs in async contexts.
     */
    private static void logError(String message, Exception e) {
        if (e != null) {
            System.err.println(message + ": " + e.getMessage());
        } else {
            System.err.println(message);
        }
    }
}