package pokedex.ui;

import pokedex.util.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Combined loading screen with spinner icon and optional progress bar.
 */
public class LoadingView extends JPanel {

    private final JLabel label;
    private final boolean showSpinner;
    private final JProgressBar progressBar;

    public LoadingView() {
        this(true, true);
    }

    public LoadingView(boolean showSpinner, boolean showBar) {
        this.showSpinner = showSpinner;
        setLayout(new GridBagLayout());
        setBackground(UIConstants.BACKGROUND);

        label = new JLabel("Ładowanie...");
        label.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 17));
        label.setForeground(new Color(238, 238, 238));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (showSpinner) {
            try {
                ImageIcon loadingIcon = new ImageIcon(getClass().getResource("/spinner.gif"));
                label.setIcon(loadingIcon);
                label.setVerticalTextPosition(SwingConstants.BOTTOM);
                label.setHorizontalTextPosition(SwingConstants.CENTER);
            } catch (Exception e) {
                System.err.println("Could not load spinner.gif: " + e.getMessage());
            }
        }

        if (showBar) {
            progressBar = new JProgressBar();
            progressBar.setPreferredSize(new Dimension(300, 25));
            progressBar.setStringPainted(true);
            progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        } else {
            progressBar = null;
        }

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

    public void start(Runnable taskStarter) {
        if (progressBar != null) progressBar.setIndeterminate(true);
        taskStarter.run();
    }

    public void setIndeterminate(String text) {
        label.setText(text);
        if (progressBar != null) {
            progressBar.setIndeterminate(true);
        }
    }

    public void setProgress(int value, int max) {
        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(max);
            progressBar.setValue(value);
        }
    }

    public void setLabelText(String text) {
        label.setText(text);
    }

    public void setProgressBarVisible(boolean visible) {
        if (progressBar != null) {
            progressBar.setVisible(visible);
            revalidate();
            repaint();
        }
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }
}