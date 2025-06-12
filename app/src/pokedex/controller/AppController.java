package pokedex.controller;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import pokedex.model.Pokemon;
import pokedex.model.PokemonDetails;
import pokedex.service.PokeApiService;
import pokedex.ui.DetailsView;
import pokedex.ui.LoadingView;
import pokedex.ui.SearchView;
import pokedex.util.ErrorHandler;
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
        // Set application icon if available
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            try {
                ImageIcon icon = new ImageIcon(iconURL);
                frame.setIconImage(icon.getImage());
            } catch (Exception ex) {
                ErrorHandler.showError(frame, ex, "ładowanie ikony aplikacji");
            }
        } else {
            System.err.println("Couldn't find icon file: icon.png");
        }

        frame.setSize(900, 680);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(UIConstants.Colors.BACKGROUND);

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
                        } catch (Exception ex) {
                            System.err.println("Failed to load image for Pokemon " + p.getId() + ": " + ex.getMessage());
                        }
                        int current = loaded.incrementAndGet();
                        SwingUtilities.invokeLater(() -> loadingView.setProgress(current, total));
                    });
                }

                imageLoadingPool.shutdown();
                if (!imageLoadingPool.awaitTermination(5, TimeUnit.MINUTES)) {
                    imageLoadingPool.shutdownNow();
                    ErrorHandler.showTimeoutError(frame, "ładowanie obrazków Pokémonów");
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // check for errors
                    loadingView.setProgressBarVisible(false);
                    showSearch();
                } catch (Exception ex) {
                    ErrorHandler.showError(frame, ex, "ładowanie początkowych danych");
                    int choice = JOptionPane.showConfirmDialog(
                        frame,
                        "Czy chcesz spróbować ponownie?",
                        "Błąd ładowania",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        loadInitial();
                    } else {
                        System.exit(1);
                    }
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
                        ErrorHandler.showError(frame, ex, "wyświetlanie widoku wyszukiwania");
                        JOptionPane.showMessageDialog(
                            frame,
                            "Nie można uruchomić aplikacji. Aplikacja zostanie zamknięta.",
                            "Błąd krytyczny",
                            JOptionPane.ERROR_MESSAGE
                        );
                        System.exit(1);
                    }
                }
            }.execute();
        } else {
            cardLayout.show(root, "search");
        }
    }

    private void showDetails(Pokemon p) {
        new SwingWorker<PokemonDetails, Void>() {
            @Override
            protected PokemonDetails doInBackground() throws Exception {
                return api.fetchPokemonDetails(p.getUrl());
            }

            @Override
            protected void done() {
                try {
                    PokemonDetails details = get();
                    detailsView = new DetailsView(details, () -> cardLayout.show(root, "search"));
                    root.add(detailsView, "details");
                    cardLayout.show(root, "details");
                } catch (Exception ex) {
                    ErrorHandler.showError(frame, ex, "ładowanie szczegółów Pokémona: " + p.getName());
                    cardLayout.show(root, "search");
                }
            }
        }.execute();
    }
}