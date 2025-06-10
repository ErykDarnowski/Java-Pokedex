import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LoadingTest {
	public static void main(String[] args) {
        try {
            // Option A: Use the System's default Look and Feel
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Option B: Use the Cross-Platform (Metal) Look and Feel (default)
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            // Option C: Use a specific L&F (e.g., Nimbus, CDE/Motif, Windows, GTK+)
            // Note: Availability depends on the OS and Java version
            // For Nimbus:
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Unsupported Look and Feel: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Look and Feel class not found: " + e.getMessage());
        } catch (InstantiationException e) {
            System.err.println("Error instantiating Look and Feel: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("Illegal access for Look and Feel: " + e.getMessage());
        }

        // Create and show the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoadingTestWindow();
            }
        });
	}
}
