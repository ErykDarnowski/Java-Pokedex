import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class ImgTest extends JFrame {
	private JLabel label;

	public ImgTest() {
		setTitle("Image Downloader");
		setSize(475, 475);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		label = new JLabel("Loading image...", SwingConstants.CENTER);
		add(label, BorderLayout.CENTER);

		loadImageWithWorker("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png", "25.png");
	}
	
	private void loadImageWithWorker(String imageUrl, String filename) {
		SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() { // do it in the background
		    @Override
		    protected ImageIcon doInBackground() throws Exception {
				// Ensure tmp directory exists
				File tmpDir = new File("tmp");
				if (!tmpDir.exists()) {
				    tmpDir.mkdirs();
				}

				File imageFile = new File(tmpDir, filename);
				// Download image if not already downloaded
				if (!imageFile.exists()) {
				    URL url = new URL(imageUrl);
				    BufferedImage image = ImageIO.read(url);
				    ImageIO.write(image, "png", imageFile); // save to disk
				}

				// Load from disk
				BufferedImage savedImage = ImageIO.read(imageFile);
				return new ImageIcon(savedImage);
		    }

		    @Override
		    protected void done() {
				try {
				    ImageIcon icon = get();
				    label.setIcon(icon);
				    label.setText(null); // clear placeholder text
				} catch (Exception ex) {
				    label.setText("Failed to load image.");
				    ex.printStackTrace();
				}
		    }
		};
		worker.execute();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new ImgTest().setVisible(true));
	}
}
