import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImgTest extends JFrame {

    private JPanel loadingPanel;
    private JPanel imagePanel;

    // Sample Pokémon images from PokeAPI
    private final String[][] imageEntries = {
        {"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png", "1.png"},
        {"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png", "4.png"},
        {"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png", "7.png"},
        {"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png", "25.png"},
    };

    private int imagesLoaded = 0;

    public ImgTest() {
        setTitle("Image Downloader");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.add(new JLabel("Loading images...", SwingConstants.CENTER), BorderLayout.CENTER);

        add(loadingPanel, BorderLayout.CENTER);
        startBatchImageDownload();
    }

    private void startBatchImageDownload() {
        imagePanel = new JPanel(new GridLayout(0, 2, 10, 10)); // dynamic rows, 2 columns
        File tmpDir = new File("tmp");
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        for (String[] entry : imageEntries) {
            String imageUrl = entry[0];
            String filename = entry[1];
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
        protected ImageIcon doInBackground() throws Exception {
            File imageFile = new File(tmpDir, filename);
            if (!imageFile.exists()) {
                URL url = new URL(imageUrl);
                BufferedImage image = ImageIO.read(url);
                ImageIO.write(image, filename.substring(filename.lastIndexOf('.') + 1), imageFile);
                System.out.println("Downloaded: " + filename);
            } else {
                System.out.println("Cached: " + filename);
            }
            BufferedImage savedImage = ImageIO.read(imageFile);
            return new ImageIcon(savedImage);
        }

        @Override
        protected void done() {
            try {
                ImageIcon icon = get();
                JLabel imageLabel = new JLabel(icon);
                imagePanel.add(imageLabel);

                imagesLoaded++;
                if (imagesLoaded == imageEntries.length) {
                    // All images are loaded — replace loading screen
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