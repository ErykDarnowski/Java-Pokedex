import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import static javax.swing.BorderFactory.createEmptyBorder;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;

public class MainPageWindow extends JFrame {
    // vars and such
    
    public MainPageWindow() {
        // Window setup
        setSize(800, 600);
        setLocationRelativeTo(null); // center window
        setDefaultCloseOperation(EXIT_ON_CLOSE); // close on `X` click
        
        setTitle("Java Pokedex");
        getContentPane().setBackground(new Color(219, 47, 66)); // 215, 10, 55
        setVisible(true);
        
        setLayout(new BorderLayout());
        
        // --- Logo ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setOpaque(false);

        // Logo Panel (centered)
        JLabel logoLabel = null;
        try {
            Image logoImage = ImageIO.read(this.getClass().getResource("logo.png")); // BufferedImage (this worked after adding `resources` as source folder in `MainPageTest`'s properties)
            Image logoImageResized = logoImage.getScaledInstance(194, 70, Image.SCALE_DEFAULT);
            logoLabel = new JLabel(new ImageIcon(logoImageResized));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        } catch (IOException ex) {
            System.err.println("Failed to load logo image: " + ex.getMessage());
            ex.printStackTrace();
            logoLabel = new JLabel("Pokedex");
        }
        topPanel.add(logoLabel, BorderLayout.NORTH);

        // --- Search Bar ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField(30);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 30));
        searchField.setToolTipText("Wyszukaj Pokémona po nazwie...");
        searchField.addFocusListener(new FocusListener() { // janky placeholder txt implementation
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Pikachu")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Pikachu");
                }
            }
            });
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        searchPanel.add(searchField);

        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        
        // --- Search Results ---
        JPanel gridPanel = new JPanel(new GridLayout(0, 4, 10, 10)); // 0 rows, 5 columns, 10px gaps
        gridPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        gridPanel.setOpaque(false);
        
        // Custom Rounded Border
        class RoundBorder extends AbstractBorder {
            private int radius;

            RoundBorder(int radius) {
                this.radius = radius;
            }

            @Override
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new java.awt.Color(150, 150, 150)); // Light grey
                g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            }

            @Override
            public java.awt.Insets getBorderInsets(java.awt.Component c, java.awt.Insets insets) {
                insets.left = insets.top = insets.right = insets.bottom = radius/2;
                return insets;
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        }
        //RoundBorder roundedBorder = new RoundBorder(15); // Radius for rounded corners

        // Generating and setting up grid children
        // Load the image ONCE before the loop
        ImageIcon scaledIcon = null;
        try {
            // String imagePath = "sprites/" + i + ".png";
            Image pokemonImage = ImageIO.read(this.getClass().getResource("pikachu.png"));
            Image scaledImage = pokemonImage.getScaledInstance(130, 130, Image.SCALE_SMOOTH); // scale to fit nicely
            scaledIcon = new ImageIcon(scaledImage);
        } catch (IOException ex) {
            System.err.println("Failed to load Pokémon image: " + ex.getMessage());
            ex.printStackTrace();
        }

        for (int i = 1; i <= 1300; i++) {
            JPanel buttonLabelPanel = new JPanel(new BorderLayout());
            buttonLabelPanel.setOpaque(false);

            // Create the button without text
            JButton pokemonBtn = new JButton();
            pokemonBtn.setPreferredSize(new Dimension(100, 150));
            pokemonBtn.setFocusPainted(false);           // cleaner look
            pokemonBtn.setBorderPainted(false);          // remove border
            //pokemonBtn.setContentAreaFilled(false);      // remove background
            pokemonBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Load and set the image icon
            pokemonBtn.setIcon(scaledIcon);

            buttonLabelPanel.add(pokemonBtn, BorderLayout.CENTER);

            JLabel pokemonNameLabel = new JLabel("Label for Pokémon " + i);
            pokemonNameLabel.setFont(new Font("Courier", Font.BOLD, 14));
            pokemonNameLabel.setForeground(Color.WHITE);
            pokemonNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            buttonLabelPanel.add(pokemonNameLabel, BorderLayout.SOUTH);

            gridPanel.add(buttonLabelPanel);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);

        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);
        
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        
        scrollPane.getViewport().setOpaque(false);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // auto change collumns on window resize
        final int itemWidth = 180; // width of a grid item
        final int hGap = 10;
        final int vGap = 10;

        // Set initial layout — placeholder, will update after window shown
        gridPanel.setLayout(new GridLayout(0, 1, hGap, vGap));

        // Dynamically adjust number of columns only when full new column fits
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int availableWidth = scrollPane.getViewport().getWidth();

                // Subtract estimated scrollbar and padding if needed
                int scrollBuffer = scrollPane.getVerticalScrollBar().isVisible() ? scrollPane.getVerticalScrollBar().getWidth() : 0;
                int effectiveWidth = availableWidth - scrollBuffer;

                // Fit only full columns
                int columns = Math.max(1, effectiveWidth / itemWidth);

                // Apply new column count
                gridPanel.setLayout(new GridLayout(0, columns, hGap, vGap));
                gridPanel.revalidate();
            }
        });

        // --- Author ---
        JLabel authorLabel = new JLabel("Eryk Darnowski (7741) - II inf. NST (24/25)");
        authorLabel.setForeground(Color.WHITE);
        
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        authorPanel.setOpaque(false);
        authorPanel.add(authorLabel);
        
        add(authorPanel, BorderLayout.SOUTH);
    }
}