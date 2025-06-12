package pokedex.ui;

import pokedex.util.ErrorHandler;
import pokedex.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Loading screen component that displays progress indicators and status messages.
 * Supports both animated spinner and progress bar modes with graceful fallback
 * for missing resources.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public class LoadingView extends JPanel {

    private static final String SPINNER_RESOURCE = "/spinner.gif";
    private static final Font LOADING_FONT = new Font("Segoe UI", Font.BOLD | Font.ITALIC, 16);

    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    private final boolean showSpinner;
    private boolean spinnerLoadFailed = false;

    /**
     * Creates a LoadingView with default configuration (spinner and progress bar enabled).
     */
    public LoadingView() {
        this(true, true);
    }

    /**
     * Creates a LoadingView with custom configuration.
     * 
     * @param showSpinner whether to display the animated spinner
     * @param showProgressBar whether to display the progress bar
     */
    public LoadingView(boolean showSpinner, boolean showProgressBar) {
        this.showSpinner = showSpinner;
        
        // Initialize components with error handling
        JLabel tempStatusLabel;
        JProgressBar tempProgressBar;
        boolean initializationFailed = false;
        
        try {
            tempStatusLabel = createStatusLabel();
            tempProgressBar = showProgressBar ? createProgressBar() : null;
            
        } catch (Exception e) {
            ErrorHandler.showError(this, e, "inicjalizacja widoku ładowania");
            tempStatusLabel = createFallbackLabel();
            tempProgressBar = null;
            initializationFailed = true;
        }
        
        // Assign to final fields only once
        this.statusLabel = tempStatusLabel;
        this.progressBar = tempProgressBar;
        
        // Complete initialization based on success/failure
        if (initializationFailed) {
            createFallbackView();
        } else {
            try {
                initializeView();
                loadSpinnerIfRequested();
            } catch (Exception e) {
                ErrorHandler.showError(this, e, "inicjalizacja widoku ładowania");
                createFallbackView();
            }
        }
    }

    /**
     * Starts a loading task with the progress bar in indeterminate mode.
     * 
     * @param taskStarter the Runnable that begins the background task
     */
    public void start(Runnable taskStarter) {
        setProgressBarIndeterminate(true);
        taskStarter.run();
    }

    /**
     * Sets the loading view to indeterminate mode with optional status text.
     * 
     * @param statusText the status text to display (null to keep current text)
     */
    public void setIndeterminate(String statusText) {
        updateStatusText(statusText);
        setProgressBarIndeterminate(true);
    }

    /**
     * Updates the progress bar with specific values.
     * 
     * @param current the current progress value
     * @param maximum the maximum progress value
     */
    public void setProgress(int current, int maximum) {
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(Math.max(maximum, 1));
                progressBar.setValue(Math.max(0, Math.min(current, maximum)));
            });
        }
    }

    /**
     * Updates the status label text.
     * 
     * @param text the new status text (null values are ignored)
     */
    public void setLabelText(String text) {
        if (text != null && statusLabel != null) {
            updateStatusText(text);
        }
    }

    /**
     * Controls the visibility of the progress bar.
     * 
     * @param visible whether the progress bar should be visible
     */
    public void setProgressBarVisible(boolean visible) {
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(visible);
                revalidateView();
            });
        }
    }

    /**
     * Switches the view to an error state with the specified message.
     * 
     * @param errorMessage the error message to display (null for default message)
     */
    public void setErrorState(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            String message = errorMessage != null ? errorMessage : UIConstants.Strings.ERROR_LOADING;
            statusLabel.setText(message);
            statusLabel.setForeground(UIConstants.Colors.ERROR);
            setProgressBarVisible(false);
            revalidateView();
        });
    }

    /**
     * Resets the view to normal loading state.
     */
    public void resetToNormalState() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(UIConstants.Strings.LOADING);
            statusLabel.setForeground(UIConstants.Colors.TEXT_PRIMARY);
            setProgressBarVisible(true);
            setProgressBarIndeterminate(true);
            revalidateView();
        });
    }

    /**
     * Returns the progress bar component for direct manipulation if needed.
     * 
     * @return the progress bar component, or null if not created
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Indicates whether the spinner loading failed.
     * 
     * @return true if spinner loading failed, false otherwise
     */
    public boolean isSpinnerLoadFailed() {
        return spinnerLoadFailed;
    }

    /**
     * Attempts to retry loading the spinner icon.
     */
    public void retrySpinnerLoad() {
        if (showSpinner && spinnerLoadFailed) {
            loadSpinnerIcon();
        }
    }

    /**
     * Initializes the main view layout and components.
     */
    private void initializeView() {
        setLayout(new GridBagLayout());
        setBackground(UIConstants.Colors.BACKGROUND);
        createMainLayout();
    }

    /**
     * Creates the main component layout.
     */
    private void createMainLayout() {
        JPanel componentGroup = createComponentGroup();
        add(componentGroup);
    }

    /**
     * Creates a grouped container for the loading components.
     */
    private JPanel createComponentGroup() {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        
        group.add(statusLabel);
        
        if (progressBar != null) {
            group.add(Box.createRigidArea(new Dimension(0, 10)));
            group.add(progressBar);
        }
        
        return group;
    }

    /**
     * Creates and configures the status label.
     */
    private JLabel createStatusLabel() {
        JLabel label = new JLabel(UIConstants.Strings.LOADING);
        label.setFont(LOADING_FONT);
        label.setForeground(UIConstants.Colors.TEXT_PRIMARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Creates and configures the progress bar.
     */
    private JProgressBar createProgressBar() {
        JProgressBar bar = new JProgressBar();
        bar.setPreferredSize(UIConstants.Sizes.PROGRESS_BAR);
        bar.setStringPainted(true);
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        return bar;
    }

    /**
     * Loads the spinner icon if requested and available.
     */
    private void loadSpinnerIfRequested() {
        if (showSpinner) {
            loadSpinnerIcon();
        }
    }

    /**
     * Attempts to load the animated spinner icon.
     */
    private void loadSpinnerIcon() {
        try {
            URL spinnerUrl = getClass().getResource(SPINNER_RESOURCE);
            if (spinnerUrl == null) {
                handleSpinnerLoadFailure();
                return;
            }

            ImageIcon spinnerIcon = loadAndValidateSpinner(spinnerUrl);
            configureSpinnerDisplay(spinnerIcon);
            
        } catch (Exception e) {
            handleSpinnerLoadFailure();
        }
    }

    /**
     * Loads and validates the spinner image.
     */
    private ImageIcon loadAndValidateSpinner(URL spinnerUrl) throws Exception {
        Image spinnerImage = Toolkit.getDefaultToolkit().createImage(spinnerUrl);
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(spinnerImage, 0);
        tracker.waitForAll();

        if (tracker.isErrorAny()) {
            throw new Exception("Spinner image failed to load");
        }

        return new ImageIcon(spinnerImage);
    }

    /**
     * Configures the status label to display the spinner.
     */
    private void configureSpinnerDisplay(ImageIcon spinnerIcon) {
        statusLabel.setIcon(spinnerIcon);
        statusLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        statusLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    /**
     * Handles spinner loading failures.
     */
    private void handleSpinnerLoadFailure() {
        spinnerLoadFailed = true;
        // Continue without spinner - text-only display
    }

    /**
     * Creates a fallback view when initialization fails.
     */
    private void createFallbackView() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);
        add(statusLabel, BorderLayout.CENTER);
        spinnerLoadFailed = true;
    }

    /**
     * Creates a fallback label for error scenarios.
     */
    private JLabel createFallbackLabel() {
        JLabel label = new JLabel(UIConstants.Strings.LOADING, JLabel.CENTER);
        label.setForeground(Color.WHITE);
        return label;
    }

    /**
     * Updates the status text on the Event Dispatch Thread.
     */
    private void updateStatusText(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    /**
     * Sets the progress bar to indeterminate mode safely.
     */
    private void setProgressBarIndeterminate(boolean indeterminate) {
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(indeterminate));
        }
    }

    /**
     * Revalidates and repaints the view safely on the EDT.
     */
    private void revalidateView() {
        revalidate();
        repaint();
    }
}