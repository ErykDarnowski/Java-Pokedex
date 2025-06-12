// Refactored SearchView.java using UIConstants
package pokedex.ui;

import pokedex.model.Pokemon;
import pokedex.util.ErrorHandler;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
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
    private String lastSearchTerm = "";

    public SearchView(List<Pokemon> pokemons, Consumer<Pokemon> onSelect, Runnable unused) {
        this.onSelect = onSelect;
        this.all = pokemons.stream().sorted(Comparator.comparing(Pokemon::getName, String.CASE_INSENSITIVE_ORDER)).toList();
        this.current = all;

        setLayout(new BorderLayout());
        setBackground(UIConstants.Colors.BACKGROUND);

        add(buildTopPanel(), BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, 1, UIConstants.Sizes.GRID_HGAP, UIConstants.Sizes.GRID_VGAP));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        gridPanel.setOpaque(false);

        gridContainerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gridContainerPanel.setOpaque(false);
        gridContainerPanel.add(gridPanel);

        updateDisplayedPokemon();

        scrollPane = new JScrollPane(gridContainerPanel);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
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
        JLabel authorLabel = new JLabel(UIConstants.Strings.AUTHOR);
        authorLabel.setForeground(UIConstants.Colors.TEXT_PRIMARY);
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
            ErrorHandler.showError(this, e, "ładowanie logo aplikacji");
            logo.setText(UIConstants.Strings.APP_TITLE_FALLBACK);
            logo.setForeground(UIConstants.Colors.TEXT_PRIMARY);
        }
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        topPanel.add(logo, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setPreferredSize(UIConstants.Sizes.SEARCH_FIELD);
        searchField.setText(UIConstants.Strings.PLACEHOLDER_SEARCH);
        searchField.setForeground(Color.LIGHT_GRAY);
        searchField.setToolTipText(UIConstants.Strings.TOOLTIP_SEARCH);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(UIConstants.Strings.PLACEHOLDER_SEARCH)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(UIConstants.Strings.PLACEHOLDER_SEARCH);
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        searchField.addCaretListener(e -> filter(searchField.getText()));

        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        return topPanel;
    }

    private void filter(String term) {
        if (term.equals(lastSearchTerm)) return;
        lastSearchTerm = term;

        String t = term.equals(UIConstants.Strings.PLACEHOLDER_SEARCH) ? "" : term.trim().toLowerCase().replace("-", "");

        List<Pokemon> filtered = all.stream()
            .filter(p -> {
                String name = p.getName().toLowerCase().replace("-", "");
                boolean nameMatches = name.contains(t);
                boolean idMatches = t.matches("\\d+") && String.valueOf(p.getId()).equals(t);
                return nameMatches || idMatches;
            })
            .collect(Collectors.toList());

        if (!filtered.equals(current)) {
            current = filtered;
            updateDisplayedPokemon();
        }
    }

    private void updateDisplayedPokemon() {
        gridPanel.removeAll();
        int initialBatch = Math.min(50, current.size());
        for (int i = 0; i < initialBatch; i++) {
            Pokemon p = current.get(i);
            gridPanel.add(pokemonPanels.computeIfAbsent(p, this::createPokemonButton));
        }
        updateGridColumns();
        gridPanel.revalidate();
        gridPanel.repaint();
        gridContainerPanel.revalidate();
        gridContainerPanel.repaint();

        if (current.size() > initialBatch) {
            SwingUtilities.invokeLater(() -> new SwingWorker<Void, JPanel>() {
                @Override protected Void doInBackground() throws Exception {
                    for (int i = initialBatch; i < current.size(); i++) {
                        Pokemon p = current.get(i);
                        publish(pokemonPanels.computeIfAbsent(p, SearchView.this::createPokemonButton));
                        Thread.sleep(1);
                    }
                    return null;
                }

                @Override protected void process(List<JPanel> chunks) {
                    for (JPanel panel : chunks) gridPanel.add(panel);
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }.execute());
        }
    }

    private JPanel createPokemonButton(Pokemon value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(UIConstants.Sizes.POKEMON_PANEL_DIM);

        JButton imageButton = new JButton();
        imageButton.setPreferredSize(new Dimension(UIConstants.Sizes.POKEMON_PANEL_WIDTH - 20, 150));
        imageButton.setFocusPainted(false);
        imageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imageButton.addActionListener(e -> onSelect.accept(value));

        SwingUtilities.invokeLater(() -> new SwingWorker<ImageIcon, Void>() {
            @Override protected ImageIcon doInBackground() throws Exception {
                return ImageCache.loadScaled(value.getId(), 130);
            }

            @Override protected void done() {
                try {
                    ImageIcon scaledIcon = get();
                    if (scaledIcon != null) {
                        imageButton.setIcon(scaledIcon);
                    } else {
                        imageButton.setText("<html><center><span style='" + UIConstants.Styles.IMG_ERR_SEARCH + "'>" + UIConstants.Strings.NO_IMAGE + "</span></center></html>");
                    }
                } catch (Exception e) {
                    imageButton.setText("<html><center><span style='" + UIConstants.Styles.IMG_ERR_SEARCH + "'>" + UIConstants.Strings.ERROR_LOADING + "</span></center></html>");
                }
            }
        }.execute());

        JLabel nameLabel = new JLabel(value.getName());
        nameLabel.setFont(UIConstants.Fonts.NAMES);
        nameLabel.setForeground(UIConstants.Colors.TEXT_PRIMARY);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(imageButton, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void updateGridColumns() {
        int itemWidth = UIConstants.Sizes.POKEMON_PANEL_WIDTH;
        int hGap = UIConstants.Sizes.GRID_HGAP;
        if (scrollPane != null && scrollPane.getViewport() != null) {
            int availableWidth = scrollPane.getViewport().getWidth();
            int scrollBuffer = scrollPane.getVerticalScrollBar().isVisible() ? scrollPane.getVerticalScrollBar().getWidth() : 0;
            int effectiveWidth = availableWidth - scrollBuffer;
            int columns = Math.max(1, effectiveWidth / (itemWidth + hGap));
            if (!(gridPanel.getLayout() instanceof GridLayout) || ((GridLayout) gridPanel.getLayout()).getColumns() != columns) {
                gridPanel.setLayout(new GridLayout(0, columns, hGap, UIConstants.Sizes.GRID_VGAP));
                gridPanel.revalidate();
                gridContainerPanel.revalidate();
            }
        }
    }
}