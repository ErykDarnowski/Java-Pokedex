package pokedex.util;

/**
 * Observer interface for monitoring loading progress and status changes.
 * Implementations can react to different loading events such as progress updates,
 * completion, and errors.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public interface LoadingObserver {
    
    /**
     * Called when the loading progress is updated.
     * 
     * @param current the current progress value
     * @param total the total/maximum progress value
     */
    void onProgressUpdate(int current, int total);
    
    /**
     * Called when the loading status text changes.
     * 
     * @param statusText the new status message
     */
    void onStatusChange(String statusText);
    
    /**
     * Called when loading is successfully completed.
     */
    void onLoadingComplete();
    
    /**
     * Called when an error occurs during loading.
     * 
     * @param errorMessage the error message to display
     */
    void onLoadingError(String errorMessage);
    
    /**
     * Called to show or hide the progress bar.
     * 
     * @param visible true to show the progress bar, false to hide it
     */
    void onProgressBarVisibilityChange(boolean visible);
}