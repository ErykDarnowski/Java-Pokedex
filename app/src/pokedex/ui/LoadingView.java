package pokedex.ui;

import pokedex.util.ErrorHandler;
import pokedex.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Combined loading screen with spinner icon and optional progress bar.
 */
public class LoadingView extends JPanel {

    private JLabel label = null;
    private final boolean showSpinner;
    private JProgressBar progressBar = null;
    private boolean spinnerLoadFailed = false;

    public LoadingView() {
        this(true, true);
    }

    public LoadingView(boolean showSpinner, boolean showBar) {
        this.showSpinner = showSpinner;
        
        try {
            initializeView(showBar);
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "inicjalizacja widoku ładowania");
            // Create minimal fallback view
            createFallbackView();
        }
    }

    private void initializeView(boolean showBar) {
        setLayout(new GridBagLayout());
        setBackground(UIConstants.BACKGROUND);

        label = new JLabel("Ładowanie...");
        try {
            label.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 16));
            label.setForeground(new Color(238, 238, 238));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
        } catch (Exception ex) {
            System.err.println("Error setting label properties: " + ex.getMessage());
            // Continue with default properties
        }

        if (showSpinner) {
            loadSpinnerIcon();
        }

        if (showBar) {
            try {
                progressBar = new JProgressBar();
                progressBar.setPreferredSize(new Dimension(300, 25));
                progressBar.setStringPainted(true);
                progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
            } catch (Exception ex) {
                ErrorHandler.showError(this, ex, "tworzenie paska postępu");
                progressBar = null;
            }
        } else {
            progressBar = null;
        }

        try {
            createLayout();
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "tworzenie układu widoku ładowania");
            createSimpleLayout();
        }
    }

    private void loadSpinnerIcon() {
	    try {
		URL resource = getClass().getResource("/spinner.gif");
		if (resource == null) {
		    System.err.println("Resource not found: /spinner.gif");
		    spinnerLoadFailed = true;
		    return;
		}

		Image image = Toolkit.getDefaultToolkit().createImage(resource);
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(image, 0);
		tracker.waitForAll();

		if (tracker.isErrorAny()) {
		    System.err.println("MediaTracker failed to load spinner.gif");
		    spinnerLoadFailed = true;
		} else {
		    label.setIcon(new ImageIcon(image));
		    label.setVerticalTextPosition(SwingConstants.BOTTOM);
		    label.setHorizontalTextPosition(SwingConstants.CENTER);
		}
	    } catch (Exception e) {
			spinnerLoadFailed = true;
			System.err.println("Exception loading spinner.gif via toolkit: " + e.getMessage());
	    }
	}


    private void createLayout() {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.add(label);

        if (progressBar != null) {
            group.add(Box.createRigidArea(new Dimension(0, 10)));
            group.add(progressBar);
        }

        add(group);
    }

    private void createSimpleLayout() {
        // Fallback to simple layout if main layout creation fails
        try {
            setLayout(new FlowLayout());
            add(label);
            if (progressBar != null) {
                add(progressBar);
            }
        } catch (Exception ex) {
            System.err.println("Even simple layout failed: " + ex.getMessage());
        }
    }

    private void createFallbackView() {
        // Emergency fallback if constructor completely fails
        try {
            setLayout(new BorderLayout());
            setBackground(Color.DARK_GRAY);
            
            label = new JLabel("Ładowanie...", JLabel.CENTER);
            label.setForeground(Color.WHITE);
            add(label, BorderLayout.CENTER);
            
            progressBar = null;
            spinnerLoadFailed = true;
        } catch (Exception ex) {
            System.err.println("Critical error in LoadingView fallback: " + ex.getMessage());
        }
    }

    public void start(Runnable taskStarter) {
        try {
            if (progressBar != null) {
                progressBar.setIndeterminate(true);
            }
            taskStarter.run();
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "uruchomienie zadania ładowania");
        }
    }

    public void setIndeterminate(String text) {
        try {
            if (text != null) {
                label.setText(text);
            }
            if (progressBar != null) {
                progressBar.setIndeterminate(true);
            }
        } catch (Exception ex) {
            System.err.println("Error setting indeterminate state: " + ex.getMessage());
            // Don't show error dialog for UI state changes
        }
    }

    public void setProgress(int value, int max) {
        try {
            if (progressBar != null) {
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(Math.max(max, 1)); // Prevent division by zero
                progressBar.setValue(Math.max(0, Math.min(value, max))); // Clamp value
            }
        } catch (Exception ex) {
            System.err.println("Error setting progress: " + ex.getMessage());
            // Don't show error dialog for progress updates
        }
    }

    public void setLabelText(String text) {
        try {
            if (label != null && text != null) {
                SwingUtilities.invokeLater(() -> label.setText(text));
            }
        } catch (Exception ex) {
            System.err.println("Error setting label text: " + ex.getMessage());
        }
    }

    public void setProgressBarVisible(boolean visible) {
        try {
            if (progressBar != null) {
                progressBar.setVisible(visible);
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();
                });
            }
        } catch (Exception ex) {
            System.err.println("Error setting progress bar visibility: " + ex.getMessage());
        }
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Check if the spinner icon failed to load
     * @return true if spinner loading failed
     */
    public boolean isSpinnerLoadFailed() {
        return spinnerLoadFailed;
    }

    /**
     * Retry loading the spinner icon
     */
    public void retrySpinnerLoad() {
        if (showSpinner && spinnerLoadFailed) {
            loadSpinnerIcon();
        }
    }

    /**
     * Set an error state for the loading view
     * @param errorMessage Error message to display
     */
    public void setErrorState(String errorMessage) {
        try {
            SwingUtilities.invokeLater(() -> {
                label.setText(errorMessage != null ? errorMessage : "Błąd ładowania");
                label.setForeground(Color.RED);
                if (progressBar != null) {
                    progressBar.setVisible(false);
                }
                revalidate();
                repaint();
            });
        } catch (Exception ex) {
            System.err.println("Error setting error state: " + ex.getMessage());
        }
    }

    /**
     * Reset the loading view to normal state
     */
    public void resetToNormalState() {
        try {
            SwingUtilities.invokeLater(() -> {
                label.setText("Ładowanie...");
                label.setForeground(new Color(238, 238, 238));
                if (progressBar != null) {
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                }
                revalidate();
                repaint();
            });
        } catch (Exception ex) {
            System.err.println("Error resetting to normal state: " + ex.getMessage());
        }
    }
}