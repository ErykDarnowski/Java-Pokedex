package pokedex.util;

/**
 * Subject interface for the Observer pattern in loading operations.
 * Classes implementing this interface can manage loading observers
 * and notify them of loading events.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public interface LoadingSubject {
    
    /**
     * Registers a loading observer to receive notifications.
     * 
     * @param observer the observer to add
     */
    void addLoadingObserver(LoadingObserver observer);
    
    /**
     * Removes a loading observer from receiving notifications.
     * 
     * @param observer the observer to remove
     */
    void removeLoadingObserver(LoadingObserver observer);
    
    /**
     * Notifies all observers of a progress update.
     * 
     * @param current the current progress value
     * @param total the total/maximum progress value
     */
    void notifyProgressUpdate(int current, int total);
    
    /**
     * Notifies all observers of a status change.
     * 
     * @param statusText the new status message
     */
    void notifyStatusChange(String statusText);
    
    /**
     * Notifies all observers that loading is complete.
     */
    void notifyLoadingComplete();
    
    /**
     * Notifies all observers of a loading error.
     * 
     * @param errorMessage the error message
     */
    void notifyLoadingError(String errorMessage);
    
    /**
     * Notifies all observers of progress bar visibility change.
     * 
     * @param visible true if progress bar should be visible
     */
    void notifyProgressBarVisibilityChange(boolean visible);
}