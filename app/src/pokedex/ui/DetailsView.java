package pokedex.ui;

import pokedex.model.PokemonDetails;
import pokedex.util.ErrorHandler;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class DetailsView extends JPanel {

    public DetailsView(PokemonDetails d, Runnable onBack) {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        try {
            initializeView(d, onBack);
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "inicjalizacja widoku szczegółów Pokémona");
            createErrorStateView(onBack);
        }
    }

    private void initializeView(PokemonDetails d, Runnable onBack) {
        JButton back = new JButton("← Wróć");
        back.setFont(new Font("SansSerif", Font.BOLD, 16));
        back.setFocusPainted(false);
        back.setBackground(new Color(200, 200, 200));
        back.setForeground(Color.BLACK);
        back.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        back.addActionListener(e -> {
            try {
                onBack.run();
            } catch (Exception ex) {
                ErrorHandler.showError(this, ex, "powrót do widoku wyszukiwania");
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        top.add(back);
        add(top, BorderLayout.NORTH);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(0, 15, 10, 10));

        try {
            String pokemonName = d.getName() != null ? d.getName() : "Nieznany";
            JLabel nameAndId = new JLabel(pokemonName + " #" + d.getId());
            nameAndId.setFont(UIConstants.FONT_TITLE);
            nameAndId.setForeground(Color.WHITE);
            nameAndId.setBorder(new EmptyBorder(0, 0, 15, 0));
            left.add(nameAndId);

            // Enhanced data labels with styled values
            addStyledDataLabel(left, "Gatunek", d.getSpecies() != null ? d.getSpecies() : "Nieznany");
            addStyledDataLabel(left, "Wzrost", String.format("%d cm", d.getHeight() * 10));
            addStyledDataLabel(left, "Waga", String.format("%.1f kg", d.getWeight() / 10.0));

            JLabel abilTitle = createTitleLabel("Umiejętności:");
            left.add(abilTitle);
            if (d.getAbilities() != null && !d.getAbilities().isEmpty()) {
                for (String ab : d.getAbilities()) {
                    if (ab != null && !ab.trim().isEmpty()) {
                        addStyledSubLabel(left, ab);
                    }
                }
            } else {
                addStyledSubLabel(left, "Brak danych");
            }

            JLabel statTitle = createTitleLabel("Statystyki:");
            left.add(statTitle);
            addStyledSubLabel(left, "HP", String.valueOf(d.getHp()));
            addStyledSubLabel(left, "Atak", String.valueOf(d.getAttack()));
            addStyledSubLabel(left, "Obrona", String.valueOf(d.getDefense()));
            addStyledSubLabel(left, "Szybkość", String.valueOf(d.getSpeed()));
            addStyledSubLabel(left, "Atak specjalny", String.valueOf(d.getSpAttack()));
            addStyledSubLabel(left, "Obrona specjalna", String.valueOf(d.getSpDefense()));

        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "wyświetlanie informacji o Pokémonie");
            JLabel errorLabel = new JLabel("Błąd wyświetlania danych Pokémona");
            errorLabel.setForeground(Color.RED);
            left.add(errorLabel);
        }

        add(left, BorderLayout.WEST);

        try {
            createImagePanel(d);
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "tworzenie panelu obrazu");

            JLabel errorLabel = new JLabel("<html><center><span style='color:black;'>BRAK OBRAZKA<br>W API</span></center></html>");
            errorLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            errorLabel.setVerticalAlignment(JLabel.CENTER);
            errorLabel.setPreferredSize(new Dimension(310, 310));

            JPanel placeholder = new JPanel(new GridBagLayout());
            placeholder.setOpaque(false);
            placeholder.setPreferredSize(new Dimension(310, 310));
            placeholder.add(errorLabel);

            add(placeholder, BorderLayout.EAST);
        }

        try {
            createFooter();
        } catch (Exception ex) {
            System.err.println("Failed to create footer: " + ex.getMessage());
        }
    }

    private void createImagePanel(PokemonDetails d) {
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel imgLabel = new JLabel("");
        imgLabel.setHorizontalAlignment(JLabel.RIGHT);
        imgLabel.setVerticalAlignment(JLabel.TOP);
        imgLabel.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 10));
        imgLabel.setForeground(Color.WHITE);

        right.add(imgLabel, BorderLayout.NORTH);
        add(right, BorderLayout.EAST);

        CompletableFuture.supplyAsync(() -> {
            try {
                return ImageCache.load(String.valueOf(d.getId()));
            } catch (Exception ex) {
                System.err.println("Failed to load image for Pokemon " + d.getId() + ": " + ex.getMessage());
                return null;
            }
        }).thenAccept(icon -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (icon != null) {
                        Image scaled = icon.getImage().getScaledInstance(310, 310, Image.SCALE_SMOOTH);
                        imgLabel.setIcon(new ImageIcon(scaled));
                        imgLabel.setText(null);
                    } else {
                        imgLabel.setText(String.format("<html><center><span style='%s'>BRAK OBRAZKA<br>W API</span></center></html>", UIConstants.IMG_ERR_STYLE_DETAILS));
                        imgLabel.setHorizontalAlignment(JLabel.CENTER);
                        imgLabel.setVerticalAlignment(JLabel.CENTER);
                        imgLabel.setPreferredSize(new Dimension(310, 310));
                        imgLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                        imgLabel.setVerticalTextPosition(SwingConstants.CENTER);
                        imgLabel.setIcon(null);
                    }
                } catch (Exception ex) {
                    ErrorHandler.showError(this, ex, "skalowanie obrazu Pokémona");
                    imgLabel.setText(String.format("<html><center><span style='%s'>BŁĄD SKALOWANIA</span></center></html>", UIConstants.IMG_ERR_STYLE_DETAILS));
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                System.err.println("Async image loading failed: " + throwable.getMessage());
	    	imgLabel.setText(String.format("<html><center><span style='%s'>BŁĄD ŁADOWANIA</span></center></html>", UIConstants.IMG_ERR_STYLE_DETAILS));
                imgLabel.setHorizontalAlignment(JLabel.CENTER);
                imgLabel.setVerticalAlignment(JLabel.CENTER);
                imgLabel.setPreferredSize(new Dimension(310, 310));
                imgLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                imgLabel.setVerticalTextPosition(SwingConstants.CENTER);
                imgLabel.setIcon(null);
            });
            return null;
        });
    }

    private void createFooter() {
        JLabel authorLabel = new JLabel("Eryk Darnowski (7741) - II inf. NST (24/25)");
        authorLabel.setForeground(Color.WHITE);

        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        authorPanel.setOpaque(false);
        authorPanel.add(authorLabel);
        add(authorPanel, BorderLayout.SOUTH);
    }

    private void createErrorStateView(Runnable onBack) {
        removeAll();
        setLayout(new BorderLayout());

        JButton back = new JButton("← Wróć");
        back.setFont(new Font("SansSerif", Font.BOLD, 16));
        back.setFocusPainted(false);
        back.setBackground(new Color(200, 200, 200));
        back.setForeground(Color.BLACK);
        back.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        back.addActionListener(e -> {
            try {
                onBack.run();
            } catch (Exception ex) {
                System.err.println("Error in back button: " + ex.getMessage());
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        top.add(back);
        add(top, BorderLayout.NORTH);

        JLabel errorLabel = new JLabel("Nie można wyświetlić szczegółów Pokémona");
        errorLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(errorLabel);
        add(center, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // Enhanced method for styling data labels with HTML formatting
    private void addStyledDataLabel(JPanel parent, String label, String value) {
        try {
            String safeLabel = label != null ? label : "Brak etykiety";
            String safeValue = value != null ? value : "Brak danych";
            
            // Get font family name from UIConstants for values
            String valueFontFamily = UIConstants.FONT_HIGHLIGHT_DETAILS.getFontName();
            
            // Using HTML to style the value part differently with custom font
            String htmlText = String.format(
                "<html>%s: <span style='color: #90EE90; font-weight: bold; font-family: %s;'>%s</span></html>", 
                safeLabel, valueFontFamily, safeValue
            );
            
            JLabel lab = new JLabel(htmlText);
            lab.setFont(UIConstants.FONT_MONO_LARGE);
            lab.setForeground(new Color(240, 240, 240));
            parent.add(lab);
        } catch (Exception ex) {
            System.err.println("Error adding styled data label: " + ex.getMessage());
        }
    }

    // Original method for backward compatibility
    private void addDataLabel(JPanel parent, String text) {
        try {
            JLabel lab = new JLabel(text != null ? text : "Brak danych");
            lab.setFont(UIConstants.FONT_MONO_LARGE);
            lab.setForeground(new Color(240, 240, 240));
            parent.add(lab);
        } catch (Exception ex) {
            System.err.println("Error adding data label: " + ex.getMessage());
        }
    }

    private JLabel createTitleLabel(String text) {
        try {
            JLabel lab = new JLabel(text != null ? text : "");
            lab.setFont(UIConstants.FONT_MONO_LARGE);
            lab.setForeground(new Color(240, 240, 240));
            return lab;
        } catch (Exception ex) {
            System.err.println("Error creating title label: " + ex.getMessage());
            return new JLabel("Błąd");
        }
    }

    // Enhanced method for styling sub labels with bullets
    private void addStyledSubLabel(JPanel parent, String text) {
        try {
            String safeText = text != null ? text : "Brak danych";
            
            // Get font family name from UIConstants for values
            String valueFontFamily = UIConstants.FONT_HIGHLIGHT_DETAILS.getFontName();
            
            // Using HTML to style just the value, keeping bullet normal color
            String htmlText = String.format(
                "<html>&nbsp;&nbsp;&nbsp;&nbsp;• <span style='color: #87CEEB; font-weight: bold; font-family: %s;'>%s</span></html>", 
                valueFontFamily, safeText
            );
            
            JLabel lab = new JLabel(htmlText);
            lab.setFont(UIConstants.FONT_MONO_MED);
            lab.setForeground(new Color(220, 220, 220));
            parent.add(lab);
        } catch (Exception ex) {
            System.err.println("Error adding styled sub label: " + ex.getMessage());
        }
    }

    // Overloaded method for statistics with separate label and value
    private void addStyledSubLabel(JPanel parent, String label, String value) {
        try {
            String safeLabel = label != null ? label : "Brak etykiety";
            String safeValue = value != null ? value : "Brak danych";
            
            // Get font family name from UIConstants for values
            String valueFontFamily = UIConstants.FONT_HIGHLIGHT_DETAILS.getFontName();
            
            // Using HTML to style just the value, keeping bullet and label normal color
            String htmlText = String.format(
                "<html>&nbsp;&nbsp;&nbsp;&nbsp;• %s: <span style='color: #F0E68C; font-weight: bold; font-size: 110%%; font-family: %s;'>%s</span></html>", 
                safeLabel, valueFontFamily, safeValue
            );
            
            JLabel lab = new JLabel(htmlText);
            lab.setFont(UIConstants.FONT_MONO_MED);
            lab.setForeground(new Color(220, 220, 220));
            parent.add(lab);
        } catch (Exception ex) {
            System.err.println("Error adding styled sub label with separate value: " + ex.getMessage());
        }
    }

    // Original method for backward compatibility
    private void addSubLabel(JPanel parent, String text) {
        try {
            JLabel lab = new JLabel("    • " + (text != null ? text : "Brak danych"));
            lab.setFont(UIConstants.FONT_MONO_MED);
            lab.setForeground(new Color(220, 220, 220));
            parent.add(lab);
        } catch (Exception ex) {
            System.err.println("Error adding sub label: " + ex.getMessage());
        }
    }
}