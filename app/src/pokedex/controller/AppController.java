package pokedex.controller;

import javax.swing.*;
import java.awt.*;
import java.net.URL; // Import the URL class
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pokedex.model.Pokemon;
import pokedex.model.PokemonDetails;
import pokedex.service.PokeApiService;
import pokedex.ui.DetailsView;
import pokedex.ui.LoadingView;
import pokedex.ui.SearchView;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;

public class AppController {

    private final JFrame frame = new JFrame(UIConstants.APP_NAME + " v" + UIConstants.APP_VERSION);
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    private final LoadingView loadingView = new LoadingView(true, true);
    private SearchView searchView;
    private DetailsView detailsView;

    private final PokeApiService api = new PokeApiService();
    private List<Pokemon> pokemons;

    public void init() {
        // --- START: ADDED CODE FOR ICON ---
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            frame.setIconImage(icon.getImage());
        } else {
            // Optional: Log an error if the icon isn't found
            System.err.println("Couldn't find icon file: icon.png");
        }
        // --- END: ADDED CODE FOR ICON ---

        frame.setSize(900, 680); // 720
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(UIConstants.BACKGROUND);

        root.setOpaque(false);
        root.add(loadingView, "loading");
        frame.setContentPane(root);
        frame.setVisible(true);

        loadInitial();
    }

    private void loadInitial() {
        loadingView.setLabelText("Ładowanie Pokémonów...");
        loadingView.setProgressBarVisible(true);
        cardLayout.show(root, "loading");

        loadingView.start(() -> new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                pokemons = api.fetchAllPokemon();
                int total = pokemons.size();
                AtomicInteger loaded = new AtomicInteger();

                ExecutorService imageLoadingPool = Executors.newFixedThreadPool(
                        Math.max(4, Runtime.getRuntime().availableProcessors() * 2));

                for (Pokemon p : pokemons) {
                    imageLoadingPool.submit(() -> {
                        try {
                            ImageCache.load(p.getId());
                        } catch (Exception ignored) {
                            // Suppress exceptions for individual image loads
                        }
                        int current = loaded.incrementAndGet();
                        SwingUtilities.invokeLater(() ->
                                loadingView.setProgress(current, total));
                    });
                }

                imageLoadingPool.shutdown();
                imageLoadingPool.awaitTermination(5, TimeUnit.MINUTES);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions from doInBackground
                    loadingView.setProgressBarVisible(false);
                    showSearch(); // Now show the search view
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error loading initial data: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        }.execute());
    }

    private void showSearch() {
        if (searchView == null) {
            loadingView.setLabelText("Przygotowywanie widoku...");
            cardLayout.show(root, "loading");

            new SwingWorker<SearchView, Void>() {
                @Override
                protected SearchView doInBackground() {
                    return new SearchView(pokemons, AppController.this::showDetails, null);
                }

                @Override
                protected void done() {
                    try {
                        searchView = get();
                        root.add(searchView, "search");
                        cardLayout.show(root, "search");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error displaying search view: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                }
            }.execute();
        } else {
            cardLayout.show(root, "search");
        }
    }

    private void showDetails(Pokemon p) {
        // --- MODIFIED CODE ---
        // Removed calls to loadingView.setIndeterminate() and cardLayout.show(root, "loading");
        // The SwingWorker still runs in the background.
        new SwingWorker<PokemonDetails, Void>() {
            @Override
            protected PokemonDetails doInBackground() throws Exception {
                return api.fetchPokemonDetails(p.getUrl());
            }

            @Override
            protected void done() {
                try {
                    PokemonDetails details = get();
                    detailsView = new DetailsView(details, () -> {
                        cardLayout.show(root, "search");
                    });
                    root.add(detailsView, "details");
                    cardLayout.show(root, "details");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error loading details: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    cardLayout.show(root, "search");
                }
                // No finally block needed for loadingView state, as it's not being used here.
            }
        }.execute();
    }
}