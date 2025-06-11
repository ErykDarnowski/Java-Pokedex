import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImgTest extends JFrame {

    private JPanel loadingPanel;
    private JPanel imagePanel;
    private JProgressBar progressBar;

    private final String[] imageUrls = ImgURLs.URLS;

    private int imagesLoaded = 0;

    public ImgTest() {
        setTitle("Image Downloader");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

		progressBar = new JProgressBar(0, imageUrls.length);
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(300, 25)); // Custom width

		// Create label
		JLabel loadingLabel = new JLabel("Ładowanie...");
		loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Create progress bar container
		progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Vertical panel to hold both
		JPanel loadingGroup = new JPanel();
		loadingGroup.setLayout(new BoxLayout(loadingGroup, BoxLayout.Y_AXIS));
		loadingGroup.add(loadingLabel);
		loadingGroup.add(Box.createRigidArea(new Dimension(0, 10))); // vertical spacing
		loadingGroup.add(progressBar);

		// Main loading panel centered
		loadingPanel = new JPanel();
		loadingPanel.setLayout(new GridBagLayout()); // center the group in the frame
		loadingPanel.add(loadingGroup);
		        
        add(loadingPanel, BorderLayout.CENTER);
        startBatchImageDownload();
    }

    private void startBatchImageDownload() {
        imagePanel = new JPanel(new GridLayout(0, 2, 10, 10));
        File tmpDir = new File("tmp");
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        for (String imageUrl : imageUrls) {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1); // extract filename
            new ImageLoaderWorker(imageUrl, filename, tmpDir).execute();
        }
    }

    private class ImageLoaderWorker extends SwingWorker<ImageIcon, Void> {
        private final String imageUrl;
        private final String filename;
        private final File tmpDir;

        public ImageLoaderWorker(String imageUrl, String filename, File tmpDir) {
            this.imageUrl = imageUrl;
            this.filename = filename;
            this.tmpDir = tmpDir;
        }

        @Override
		protected ImageIcon doInBackground() {
		    try {
				File imageFile = new File(tmpDir, filename);
				
				if (!imageFile.exists()) {
				    URL url = new URL(imageUrl);
				    BufferedImage image = ImageIO.read(url);

				    if (image == null) throw new Exception("Image could not be read from URL: " + imageUrl);

				    ImageIO.write(image, filename.substring(filename.lastIndexOf('.') + 1), imageFile);
				    System.out.println("Downloaded: " + filename);
				} else {
				    System.out.println("Cached: " + filename);
				}

				BufferedImage savedImage = ImageIO.read(imageFile);

				return new ImageIcon(savedImage);
		    } catch (Exception e) {
				System.err.println("Failed to load image from " + imageUrl + ": " + e.getMessage());

				// Return a simple placeholder icon (gray square)
				BufferedImage placeholder = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = placeholder.createGraphics();
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, 0, 200, 200);
				g.setColor(Color.DARK_GRAY);
				g.drawString("Image failed", 50, 100);
				g.dispose();

				return new ImageIcon(placeholder);
		    }
		}


        @Override
        protected void done() {
            try {
                ImageIcon icon = get();
                JLabel imageLabel = new JLabel(icon);
                imagePanel.add(imageLabel);

                imagesLoaded++;
                progressBar.setValue(imagesLoaded);

                if (imagesLoaded == imageUrls.length) {
                    // Replace loading screen with image grid
                    SwingUtilities.invokeLater(() -> {
                        remove(loadingPanel);
                        add(new JScrollPane(imagePanel), BorderLayout.CENTER);
                        revalidate();
                        repaint();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImgTest().setVisible(true));
    }
}