package pokedex.util;

/**
 * Simple utility class for formatting Pokemon names from API format to display format.
 * Converts hyphen-separated names to proper title case.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public final class FormatterUtil {

    /**
     * Prevents instantiation of this utility class.
     */
    private FormatterUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Formats a raw Pokemon name from API format to display format.
     * Converts hyphen-separated lowercase names to proper title case.
     * 
     * @param rawName the raw name from the API
     * @return formatted display name, or "Unknown" if input is invalid
     */
    public static String formatName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "Unknown";
        }

        String[] parts = rawName.split("-");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(capitalizeFirst(part));
            }
        }
        
        return result.toString().trim();
    }

    /**
     * Capitalizes the first letter of a word.
     * 
     * @param word the word to capitalize
     * @return word with first letter capitalized
     */
    private static String capitalizeFirst(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }
}