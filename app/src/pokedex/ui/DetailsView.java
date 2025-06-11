package pokedex.ui;

import pokedex.model.PokemonDetails;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class DetailsView extends JPanel {

    public DetailsView(PokemonDetails d, Runnable onBack) {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // --- TOP: Back Button ---
        JButton back = new JButton("← Wróć");
        back.setFont(new Font("SansSerif", Font.BOLD, 16));
        back.setFocusPainted(false);
        back.setBackground(new Color(200, 200, 200));
        back.setForeground(Color.BLACK);
        back.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        back.addActionListener(e -> onBack.run());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        top.add(back);
        add(top, BorderLayout.NORTH);

        // --- LEFT: Label column ---
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(0, 15, 10, 10));

        JLabel nameAndId = new JLabel(d.getName() + " #" + d.getId());
        nameAndId.setFont(UIConstants.FONT_TITLE);
        nameAndId.setForeground(Color.WHITE);
        nameAndId.setBorder(new EmptyBorder(0, 0, 15, 0));
        left.add(nameAndId);

        addDataLabel(left, "Gatunek: " + d.getSpecies());
        addDataLabel(left, String.format("Wzrost: %d cm", d.getHeight() * 10));
        addDataLabel(left, String.format("Waga: %.1f kg", d.getWeight() / 10.0));

        JLabel abilTitle = createTitleLabel("Umiejętności:");
        left.add(abilTitle);
        for (String ab : d.getAbilities()) {
            addSubLabel(left, ab);
        }

        JLabel statTitle = createTitleLabel("Statystyki:");
        left.add(statTitle);
        addSubLabel(left, "HP: " + d.getHp());
        addSubLabel(left, "Atak: " + d.getAttack());
        addSubLabel(left, "Obrona: " + d.getDefense());
        addSubLabel(left, "Szybkość: " + d.getSpeed());
        addSubLabel(left, "Atak specjalny: " + d.getSpAttack());
        addSubLabel(left, "Obrona specjalna: " + d.getSpDefense());

        add(left, BorderLayout.WEST);

        // --- RIGHT: Image Panel ---
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel imgLabel = new JLabel("Preview");
        imgLabel.setHorizontalAlignment(JLabel.RIGHT);
        imgLabel.setVerticalAlignment(JLabel.TOP);
        imgLabel.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 10));

        right.add(imgLabel, BorderLayout.NORTH);
        add(right, BorderLayout.EAST);

        // Load image async
        CompletableFuture.supplyAsync(() -> {
            try {
                return ImageCache.load(String.valueOf(d.getId()));
            } catch (Exception ignored) {
                return null;
            }
        }).thenAccept(icon -> {
            if (icon != null) {
                Image scaled = icon.getImage().getScaledInstance(310, 310, Image.SCALE_SMOOTH);
                imgLabel.setIcon(new ImageIcon(scaled));
                imgLabel.setText(null); // remove "Preview" fallback
            }
        });

        // --- Footer: Author Info ---
        JLabel authorLabel = new JLabel("Eryk Darnowski (7741) - II inf. NST (24/25)");
        authorLabel.setForeground(Color.WHITE);

        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        authorPanel.setOpaque(false);
        authorPanel.add(authorLabel);
        add(authorPanel, BorderLayout.SOUTH);
    }

    private void addDataLabel(JPanel parent, String text) {
        JLabel lab = new JLabel(text);
        lab.setFont(UIConstants.FONT_MONO_LARGE);
        lab.setForeground(new Color(240, 240, 240));
        parent.add(lab);
    }

    private JLabel createTitleLabel(String text) {
        JLabel lab = new JLabel(text);
        lab.setFont(UIConstants.FONT_MONO_LARGE);
        lab.setForeground(new Color(240, 240, 240));
        return lab;
    }

    private void addSubLabel(JPanel parent, String text) {
        JLabel lab = new JLabel("    • " + text);
        lab.setFont(UIConstants.FONT_MONO_MED);
        lab.setForeground(new Color(220, 220, 220));
        parent.add(lab);
    }
}