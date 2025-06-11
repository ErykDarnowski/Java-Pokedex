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

        // OPTIMIZATION: Don't create all panels at once - lazy load them
        // Only create panels when they're actually needed for display
        updateDisplayedPokemon();

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
        MouseAdapter unfocusMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.hasFocus()) {
                    ((Component) e.getSource()).requestFocusInWindow();
                }
            }
        };

        this.addMouseListener(unfocusMouseAdapter);
        this.setFocusable(true);

        scrollPane.getViewport().addMouseListener(unfocusMouseAdapter);
        scrollPane.getViewport().setFocusable(true);

        gridContainerPanel.addMouseListener(unfocusMouseAdapter);
        gridContainerPanel.setFocusable(true);

        gridPanel.addMouseListener(unfocusMouseAdapter);
        gridPanel.setFocusable(true);

        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        authorPanel.setOpaque(false);
        authorPanel.addMouseListener(unfocusMouseAdapter);
        authorPanel.setFocusable(true);
        JLabel authorLabel = new JLabel("Eryk Darnowski (7741) - II inf. NST (24/25)");
        authorLabel.setForeground(Color.WHITE);
        authorPanel.add(authorLabel);
        add(authorPanel, BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.setFocusable(true);
        topPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.hasFocus()) {
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
        searchField.setForeground(Color.LIGHT_GRAY);

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

        updateDisplayedPokemon();
    }

    // OPTIMIZATION: New method to update displayed Pokemon with batch loading
    private void updateDisplayedPokemon() {
        gridPanel.removeAll();

        // Limit initial creation to first 50 panels for instant UI response
        int initialBatch = Math.min(50, current.size());
        
        // Add first batch immediately
        for (int i = 0; i < initialBatch; i++) {
            Pokemon p = current.get(i);
            JPanel panel = pokemonPanels.get(p);
            if (panel == null) {
                panel = createPokemonButton(p);
                pokemonPanels.put(p, panel);
            }
            gridPanel.add(panel);
        }

        updateGridColumns();
        gridPanel.revalidate();
        gridPanel.repaint();
        gridContainerPanel.revalidate();
        gridContainerPanel.repaint();

        // Load remaining panels in background if there are more
        if (current.size() > initialBatch) {
            SwingUtilities.invokeLater(() -> {
                new SwingWorker<Void, JPanel>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        for (int i = initialBatch; i < current.size(); i++) {
                            Pokemon p = current.get(i);
                            JPanel panel = pokemonPanels.get(p);
                            if (panel == null) {
                                panel = createPokemonButton(p);
                                pokemonPanels.put(p, panel);
                            }
                            publish(panel);
                            
                            // Small delay to prevent UI freezing
                            Thread.sleep(1);
                        }
                        return null;
                    }

                    @Override
                    protected void process(java.util.List<JPanel> chunks) {
                        for (JPanel panel : chunks) {
                            gridPanel.add(panel);
                        }
                        gridPanel.revalidate();
                        gridPanel.repaint();
                    }
                }.execute();
            });
        }
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

        // OPTIMIZATION: Create placeholder first, then load/scale image asynchronously
        BufferedImage placeholder = new BufferedImage(130, 130, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRect(0, 0, 130, 130);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Loading...", 40, 65);
        g2d.dispose();
        imageButton.setIcon(new ImageIcon(placeholder));

        // Load and scale image asynchronously to avoid blocking EDT
        SwingUtilities.invokeLater(() -> {
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    try {
                        // Use the optimized loadScaled method
                        return ImageCache.loadScaled(value.getId(), 130);
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon scaledIcon = get();
                        if (scaledIcon != null) {
                            imageButton.setIcon(scaledIcon);
                        } else {
                            imageButton.setText("No Image");
                            imageButton.setForeground(Color.RED);
                        }
                    } catch (Exception e) {
                        imageButton.setText("Error");
                        imageButton.setForeground(Color.RED);
                    }
                }
            }.execute();
        });

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