package pokedex.ui;

import pokedex.model.Pokemon;
import pokedex.util.UIConstants;
import pokedex.util.ImageCache;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchView extends JPanel {

    private final List<Pokemon> all;
    private List<Pokemon> current;
    private final JTextField searchField = new JTextField(30);
    private final Consumer<Pokemon> onSelect;

    private JPanel gridPanel;
    private JScrollPane scrollPane;
    private JPanel gridContainerPanel;

    private final Map<Pokemon, JPanel> pokemonPanels = new HashMap<>();

    public SearchView(List<Pokemon> pokemons, Consumer<Pokemon> onSelect, Runnable unused) {
        this.onSelect = onSelect;
        this.all = pokemons.stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
        this.current = all;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND);

        add(buildTopPanel(), BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, 1, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        gridPanel.setOpaque(false);

        gridContainerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gridContainerPanel.setOpaque(false);
        gridContainerPanel.add(gridPanel);

        for (Pokemon p : all) {
            JPanel panel = createPokemonButton(p);
            pokemonPanels.put(p, panel);
            gridPanel.add(panel);
        }

        scrollPane = new JScrollPane(gridContainerPanel);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        updateGridColumns();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateGridColumns();
            }
        });

        // --- ENHANCED CODE FOR UNFOCUSING ---
        // Create a single MouseAdapter for unfocusing
        MouseAdapter unfocusMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.hasFocus()) {
                    // Request focus on the clicked component itself
                    // This causes the searchField to lose focus.
                    ((Component) e.getSource()).requestFocusInWindow();
                }
            }
        };

        // Apply the listener to all panels that represent "empty space" or background
        // 1. The main SearchView panel
        this.addMouseListener(unfocusMouseAdapter);
        this.setFocusable(true); // Ensure SearchView can receive focus

        // 2. The top panel (already had this, but now using the shared adapter)
        //    Make sure topPanel's existing listener is replaced or adapted if needed.
        //    For now, let's just make sure it's focusable and clickable in buildTopPanel
        //    (its specific behavior to request focus on itself is fine)

        // 3. The scrollPane's viewport (the visible area of scrollable content)
        scrollPane.getViewport().addMouseListener(unfocusMouseAdapter);
        scrollPane.getViewport().setFocusable(true); // Essential for it to accept focus

        // 4. The gridContainerPanel (which holds the gridPanel, useful for FlowLayout gaps)
        gridContainerPanel.addMouseListener(unfocusMouseAdapter);
        gridContainerPanel.setFocusable(true); // Essential for it to accept focus

        // 5. The gridPanel itself (where the Pokemon panels are placed, covers gaps between them)
        gridPanel.addMouseListener(unfocusMouseAdapter);
        gridPanel.setFocusable(true); // Essential for it to accept focus

        // The authorPanel (at the bottom)
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        authorPanel.setOpaque(false);
        authorPanel.addMouseListener(unfocusMouseAdapter); // Add listener to author panel
        authorPanel.setFocusable(true); // Make author panel focusable
        JLabel authorLabel = new JLabel("Eryk Darnowski (7741) - II inf. NST (24/25)");
        authorLabel.setForeground(Color.WHITE);
        authorPanel.add(authorLabel);
        add(authorPanel, BorderLayout.SOUTH);
        // --- END ENHANCED CODE ---
    }

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // This listener is specific to the topPanel, letting it take focus
        // when clicked (which will unfocus the searchField). This is fine.
        topPanel.setFocusable(true);
        topPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.hasFocus()) { // Only do this if searchField has focus
                    topPanel.requestFocusInWindow();
                }
            }
        });

        JLabel logo = new JLabel();
        try {
            Image logoImg = ImageIO.read(getClass().getResource("/logo.png"));
            logo.setIcon(new ImageIcon(logoImg));
        } catch (Exception e) {
            logo.setText("Pokédex");
            logo.setForeground(Color.WHITE);
        }
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        topPanel.add(logo, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 30));

        searchField.setText(UIConstants.PLACEHOLDER_TEXT);
        searchField.setForeground(Color.GRAY);

        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(UIConstants.PLACEHOLDER_TEXT)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText(UIConstants.PLACEHOLDER_TEXT);
                }
            }
        });

        searchField.setToolTipText("Wyszukaj Pokémona po nazwie...");
        searchField.addCaretListener(e -> filter(searchField.getText()));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        return topPanel;
    }

    private void filter(String term) {
        String actualTerm = term.equals(UIConstants.PLACEHOLDER_TEXT) ? "" : term;

        String t = actualTerm.trim().toLowerCase().replace("-", "");
        current = all.stream()
                .filter(p -> p.getName().toLowerCase().replace("-", "").contains(t))
                .collect(Collectors.toList());

        gridPanel.removeAll();

        for (Pokemon p : current) {
            JPanel panel = pokemonPanels.get(p);
            if (panel != null) {
                gridPanel.add(panel);
            }
        }

        updateGridColumns();
        gridPanel.revalidate();
        gridPanel.repaint();
        gridContainerPanel.revalidate();
        gridContainerPanel.repaint();
    }

    private JPanel createPokemonButton(Pokemon value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.setPreferredSize(new Dimension(UIConstants.POKEMON_PANEL_FIXED_WIDTH, UIConstants.POKEMON_PANEL_FIXED_HEIGHT));
        panel.setMinimumSize(new Dimension(UIConstants.POKEMON_PANEL_FIXED_WIDTH, UIConstants.POKEMON_PANEL_FIXED_HEIGHT));
        panel.setMaximumSize(new Dimension(UIConstants.POKEMON_PANEL_FIXED_WIDTH, UIConstants.POKEMON_PANEL_FIXED_HEIGHT));

        JButton imageButton = new JButton();
        imageButton.setPreferredSize(new Dimension(UIConstants.POKEMON_PANEL_FIXED_WIDTH - 20, 150));
        imageButton.setFocusPainted(false);
        imageButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageButton.addActionListener(e -> onSelect.accept(value));

        try {
            ImageIcon icon = ImageCache.load(value.getId());
            Image scaled = icon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            imageButton.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            imageButton.setIcon(new ImageIcon(new BufferedImage(130, 130, BufferedImage.TYPE_INT_ARGB)));
            imageButton.setText("No Image");
            imageButton.setForeground(Color.RED);
        }

        JLabel nameLabel = new JLabel(value.getName());
        nameLabel.setFont(UIConstants.FONT_NAMES);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(imageButton, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateGridColumns() {
        int itemWidth = UIConstants.POKEMON_PANEL_FIXED_WIDTH;
        int hGap = 10;

        if (scrollPane != null && scrollPane.getViewport() != null) {
            int availableWidth = scrollPane.getViewport().getWidth();
            int scrollBuffer = scrollPane.getVerticalScrollBar().isVisible()
                    ? scrollPane.getVerticalScrollBar().getWidth() : 0;

            int effectiveWidth = availableWidth - scrollBuffer;
            int columns = Math.max(1, effectiveWidth / (itemWidth + hGap));

            if (!(gridPanel.getLayout() instanceof GridLayout) || ((GridLayout) gridPanel.getLayout()).getColumns() != columns) {
                gridPanel.setLayout(new GridLayout(0, columns, hGap, 10));
                gridPanel.revalidate();
                gridContainerPanel.revalidate();
            }
        }
    }
}