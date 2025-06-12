// Refactored LoadingView.java using UIConstants
package pokedex.ui;

import pokedex.util.ErrorHandler;
import pokedex.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class LoadingView extends JPanel {
    private JLabel label;
    private final boolean showSpinner;
    private JProgressBar progressBar;
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
            createFallbackView();
        }
    }

    private void initializeView(boolean showBar) {
        setLayout(new GridBagLayout());
        setBackground(UIConstants.Colors.BACKGROUND);

        label = new JLabel(UIConstants.Strings.LOADING);
        label.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 16));
        label.setForeground(UIConstants.Colors.TEXT_PRIMARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (showSpinner) {
            loadSpinnerIcon();
        }

        if (showBar) {
            progressBar = new JProgressBar();
            progressBar.setPreferredSize(UIConstants.Sizes.PROGRESS_BAR);
            progressBar.setStringPainted(true);
            progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        createLayout();
    }

    private void loadSpinnerIcon() {
        try {
            URL resource = getClass().getResource("/spinner.gif");
            if (resource == null) {
                spinnerLoadFailed = true;
                return;
            }
            Image image = Toolkit.getDefaultToolkit().createImage(resource);
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(image, 0);
            tracker.waitForAll();

            if (!tracker.isErrorAny()) {
                label.setIcon(new ImageIcon(image));
                label.setVerticalTextPosition(SwingConstants.BOTTOM);
                label.setHorizontalTextPosition(SwingConstants.CENTER);
            } else {
                spinnerLoadFailed = true;
            }
        } catch (Exception e) {
            spinnerLoadFailed = true;
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

    private void createFallbackView() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);
        label = new JLabel(UIConstants.Strings.LOADING, JLabel.CENTER);
        label.setForeground(Color.WHITE);
        add(label, BorderLayout.CENTER);
        progressBar = null;
        spinnerLoadFailed = true;
    }

    public void start(Runnable taskStarter) {
        if (progressBar != null) progressBar.setIndeterminate(true);
        taskStarter.run();
    }

    public void setIndeterminate(String text) {
        if (text != null) label.setText(text);
        if (progressBar != null) progressBar.setIndeterminate(true);
    }

    public void setProgress(int value, int max) {
        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(Math.max(max, 1));
            progressBar.setValue(Math.max(0, Math.min(value, max)));
        }
    }

    public void setLabelText(String text) {
        if (label != null && text != null) {
            SwingUtilities.invokeLater(() -> label.setText(text));
        }
    }

    public void setProgressBarVisible(boolean visible) {
        if (progressBar != null) {
            progressBar.setVisible(visible);
            SwingUtilities.invokeLater(() -> {
                revalidate();
                repaint();
            });
        }
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public boolean isSpinnerLoadFailed() {
        return spinnerLoadFailed;
    }

    public void retrySpinnerLoad() {
        if (showSpinner && spinnerLoadFailed) {
            loadSpinnerIcon();
        }
    }

    public void setErrorState(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            label.setText(errorMessage != null ? errorMessage : UIConstants.Strings.ERROR_LOADING);
            label.setForeground(UIConstants.Colors.ERROR);
            if (progressBar != null) progressBar.setVisible(false);
            revalidate();
            repaint();
        });
    }

    public void resetToNormalState() {
        SwingUtilities.invokeLater(() -> {
            label.setText(UIConstants.Strings.LOADING);
            label.setForeground(UIConstants.Colors.TEXT_PRIMARY);
            if (progressBar != null) {
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
            }
            revalidate();
            repaint();
        });
    }
}