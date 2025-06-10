import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import javax.imageio.ImageIO;

public class DetailsPageWindow extends JFrame {
    // vars and such
    private JLabel nameAndIdLabel;
    private JLabel speciesLabel;
    private JLabel heightLabel;
    private JLabel weightLabel;
    private JLabel abilitiesLabel;
    private JLabel statsLabel;
    
    public DetailsPageWindow() {
	// Window setup
	setSize(800, 600);
	setLocationRelativeTo(null); // center window
	setDefaultCloseOperation(EXIT_ON_CLOSE); // close on `X` click
	setTitle("Java Pokedex");
	getContentPane().setBackground(new Color(219, 47, 66)); // Background color
	setVisible(true);

	// Main panel with BorderLayout
	JPanel mainPanel = new JPanel(new BorderLayout());
	mainPanel.setOpaque(false);

	// --- TOP: Back Button ---
	JButton backButton = new JButton("← Wróć");
	backButton.setFont(new Font("SansSerif", Font.BOLD, 16));
	backButton.setFocusPainted(false);
	backButton.setBackground(new Color(200, 200, 200));
	backButton.setForeground(Color.BLACK);
	backButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

	backButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		dispose(); // Close the window on back button click
	    }
	});

	JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	topPanel.setOpaque(false);
	topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
	topPanel.add(backButton);
	mainPanel.add(topPanel, BorderLayout.NORTH);

	// --- LEFT: Column of labels ---
	JPanel labelPanel = new JPanel();
	labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
	labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
	labelPanel.setOpaque(false);

	nameAndIdLabel = new JLabel("Pikachu #25");
	nameAndIdLabel.setFont(new Font("Courier", Font.BOLD, 50));
	nameAndIdLabel.setForeground(new Color(255, 255, 255));
	nameAndIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
	labelPanel.add(nameAndIdLabel);

	Font dataFont = new Font("Monospaced", Font.BOLD, 23);
	Font dataSubFont = new Font("Monospaced", Font.BOLD, 19);
	Color dataColor = new Color(240, 240, 240);
	Color dataSubColor = new Color(220, 220, 220);

	speciesLabel = new JLabel("Gatunek: Pikachu");
	speciesLabel.setFont(dataFont);
	speciesLabel.setForeground(dataColor);
	labelPanel.add(speciesLabel);

	heightLabel = new JLabel("Wzrost: 40 cm");
	heightLabel.setFont(dataFont);
	heightLabel.setForeground(dataColor);
	labelPanel.add(heightLabel);

	weightLabel = new JLabel("Waga: 6.0 kg");
	weightLabel.setFont(dataFont);
	weightLabel.setForeground(dataColor);
	labelPanel.add(weightLabel);

	abilitiesLabel = new JLabel("Umiejętności:");
	abilitiesLabel.setFont(dataFont);
	abilitiesLabel.setForeground(dataColor);
	labelPanel.add(abilitiesLabel);

	String[] abilities = { "Static", "Lightning Rod (ukryta)" };
	for (String text : abilities) {
	    JLabel label = new JLabel("    \u2022 " + text);
	    label.setFont(dataSubFont);
	    label.setForeground(dataSubColor);
	    labelPanel.add(label);
	}

	statsLabel = new JLabel("Statystyki:");
	statsLabel.setFont(dataFont);
	statsLabel.setForeground(dataColor);
	labelPanel.add(statsLabel);

	String[] stats = { "HP: 35", "Atak: 55", "Prędkość: 90", "Atak specjalny: 50", "Obrona specjalna: 50" };
	for (String text : stats) {
	    JLabel label = new JLabel("    \u2022 " + text);
	    label.setFont(dataSubFont);
	    label.setForeground(dataSubColor);
	    labelPanel.add(label);
	}

	mainPanel.add(labelPanel, BorderLayout.WEST);

	// --- RIGHT: Image panel ---
	JPanel imageContainer = new JPanel(new BorderLayout());
	imageContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	imageContainer.setOpaque(false);

	JLabel imageLabel;
	try {
	    Image previewImage = ImageIO.read(this.getClass().getResource("pikachu.png"));
	    Image previewImageResized = previewImage.getScaledInstance(300, 300, Image.SCALE_DEFAULT);
	    imageLabel = new JLabel(new ImageIcon(previewImageResized));
	    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
	} catch (IOException ex) {
	    System.err.println("Failed to load logo image: " + ex.getMessage());
	    ex.printStackTrace();
	    imageLabel = new JLabel("Preview");
	}

	imageLabel.setHorizontalAlignment(JLabel.RIGHT);
	imageLabel.setVerticalAlignment(JLabel.TOP);
	imageContainer.add(imageLabel, BorderLayout.NORTH);

	mainPanel.add(imageContainer, BorderLayout.EAST);

	// Add main panel to frame
	add(mainPanel);

	// --- Author footer ---
	JLabel authorLabel = new JLabel("Eryk Darnowski (7741) - II inf. NST (24/25)");
	authorLabel.setForeground(Color.WHITE);

	JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
	authorPanel.setOpaque(false);
	authorPanel.add(authorLabel);

	add(authorPanel, BorderLayout.SOUTH);

	setVisible(true);
    }
}
