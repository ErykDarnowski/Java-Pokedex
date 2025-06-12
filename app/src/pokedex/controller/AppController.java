package pokedex.controller;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

import pokedex.model.Pokemon;
import pokedex.model.PokemonDetails;
import pokedex.service.PokeApiService;
import pokedex.ui.DetailsView;
import pokedex.ui.LoadingView;
import pokedex.ui.SearchView;
import pokedex.util.ErrorHandler;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;
import pokedex.util.LoadingSubject;
import pokedex.util.LoadingObserver;

/**
 * Main controller coordinating the application's UI flow and data management.
 * Implements the Model-View-Controller pattern by managing view transitions,
 * data loading, and user interactions. Now implements LoadingSubject for
 * observer pattern support.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public class AppController implements LoadingSubject {

    // View identifiers for CardLayout
    private static final String LOADING_VIEW = "loading";
    private static final String SEARCH_VIEW = "search";
    private static final String DETAILS_VIEW = "details";
    
    // Image loading configuration
    private static final int IMAGE_LOAD_TIMEOUT_MINUTES = 5;
    private static final int IMAGE_LOAD_THREAD_POOL_SIZE = 
        Math.max(4, Runtime.getRuntime().availableProcessors() * 2);

    // UI components
    private final JFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    private final LoadingView loadingView;

    // Views (created as needed)
    private SearchView searchView;
    private DetailsView detailsView;

    // Services and data
    private final PokeApiService apiService;
    private List<Pokemon> pokemonData;
    
    // Observer pattern support
    private final List<LoadingObserver> loadingObservers = new ArrayList<>();

    /**
     * Constructs a new AppController with initialized UI components and services.
     */
    public AppController() {
        this.mainFrame = createMainFrame();
        this.cardLayout = new CardLayout();
        this.rootPanel = new JPanel(cardLayout);
        this.loadingView = new LoadingView(true, true);
        this.apiService = new PokeApiService();
        
        setupRootPanel();
        // Register the loading view as an observer
        addLoadingObserver(loadingView);
    }

    // LoadingSubject implementation
    @Override
    public void addLoadingObserver(LoadingObserver observer) {
        if (observer != null && !loadingObservers.contains(observer)) {
            loadingObservers.add(observer);
        }
    }

    @Override
    public void removeLoadingObserver(LoadingObserver observer) {
        loadingObservers.remove(observer);
    }

    @Override
    public void notifyProgressUpdate(int current, int total) {
        for (LoadingObserver observer : loadingObservers) {
            observer.onProgressUpdate(current, total);
        }
    }

    @Override
    public void notifyStatusChange(String statusText) {
        for (LoadingObserver observer : loadingObservers) {
            observer.onStatusChange(statusText);
        }
    }

    @Override
    public void notifyLoadingComplete() {
        for (LoadingObserver observer : loadingObservers) {
            observer.onLoadingComplete();
        }
    }

    @Override
    public void notifyLoadingError(String errorMessage) {
        for (LoadingObserver observer : loadingObservers) {
            observer.onLoadingError(errorMessage);
        }
    }

    @Override
    public void notifyProgressBarVisibilityChange(boolean visible) {
        for (LoadingObserver observer : loadingObservers) {
            observer.onProgressBarVisibilityChange(visible);
        }
    }

    /**
     * Initializes and displays the application.
     * Sets up the main window and begins the data loading process.
     */
    public void init() {
        configureMainFrame();
        setupInitialView();
        beginDataLoading();
    }

    /**
     * Creates and configures the main application frame.
     */
    private JFrame createMainFrame() {
        JFrame frame = new JFrame(UIConstants.APP_NAME + " v" + UIConstants.APP_VERSION);
        
        // Set application icon if available
        setApplicationIcon(frame);
        
        return frame;
    }

    /**
     * Attempts to set the application icon from resources.
     */
    private void setApplicationIcon(JFrame frame) {
        URL iconUrl = getClass().getResource("/icon.png");
        if (iconUrl != null) {
            try {
                ImageIcon icon = new ImageIcon(iconUrl);
                frame.setIconImage(icon.getImage());
            } catch (Exception e) {
                ErrorHandler.showError(frame, e, "ładowanie ikony aplikacji");
            }
        } else {
            System.err.println("Application icon not found: /icon.png");
        }
    }

    /**
     * Configures the main frame properties and makes it visible.
     */
    private void configureMainFrame() {
        mainFrame.setSize(900, 680);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setBackground(UIConstants.Colors.BACKGROUND);
        mainFrame.setContentPane(rootPanel);
        mainFrame.setVisible(true);
    }

    /**
     * Sets up the root panel with initial views.
     */
    private void setupRootPanel() {
        rootPanel.setOpaque(false);
        rootPanel.add(loadingView, LOADING_VIEW);
    }

    /**
     * Shows the initial loading view.
     */
    private void setupInitialView() {
        cardLayout.show(rootPanel, LOADING_VIEW);
    }

    /**
     * Begins the asynchronous data loading process.
     */
    private void beginDataLoading() {
        notifyStatusChange("Ładowanie Pokémonów...");
        notifyProgressBarVisibilityChange(true);
        
        loadingView.start(this::executeDataLoadingTask);
    }

    /**
     * Executes the main data loading task in a background thread.
     */
    private void executeDataLoadingTask() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadPokemonData();
                preloadPokemonImages();
                return null;
            }

            @Override
            protected void done() {
                handleDataLoadingCompletion();
            }
        }.execute();
    }

    /**
     * Loads Pokemon data from the API.
     */
    private void loadPokemonData() throws Exception {
        pokemonData = apiService.fetchAllPokemon();
        
        if (pokemonData == null || pokemonData.isEmpty()) {
            throw new Exception("No Pokemon data received from API");
        }
    }

    /**
     * Preloads Pokemon images for better user experience.
     */
    private void preloadPokemonImages() throws InterruptedException {
        ExecutorService imageLoadingPool = createImageLoadingPool();
        AtomicInteger loadedCount = new AtomicInteger(0);
        int totalCount = pokemonData.size();

        submitImageLoadingTasks(imageLoadingPool, loadedCount, totalCount);
        shutdownImageLoadingPool(imageLoadingPool);
    }

    /**
     * Creates a thread pool for image loading tasks.
     */
    private ExecutorService createImageLoadingPool() {
        return Executors.newFixedThreadPool(IMAGE_LOAD_THREAD_POOL_SIZE);
    }

    /**
     * Submits image loading tasks to the thread pool.
     */
    private void submitImageLoadingTasks(ExecutorService pool, AtomicInteger loadedCount, int totalCount) {
        for (Pokemon pokemon : pokemonData) {
            pool.submit(() -> {
                loadSinglePokemonImage(pokemon);
                updateImageLoadingProgress(loadedCount.incrementAndGet(), totalCount);
            });
        }
    }

    /**
     * Loads a single Pokemon image with error handling.
     */
    private void loadSinglePokemonImage(Pokemon pokemon) {
        try {
            ImageCache.load(pokemon.getId());
        } catch (Exception e) {
            System.err.println("Failed to load image for Pokemon " + pokemon.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Updates the loading progress UI using observer pattern.
     */
    private void updateImageLoadingProgress(int current, int total) {
        SwingUtilities.invokeLater(() -> notifyProgressUpdate(current, total));
    }

    /**
     * Shuts down the image loading thread pool with timeout.
     */
    private void shutdownImageLoadingPool(ExecutorService pool) throws InterruptedException {
        pool.shutdown();
        if (!pool.awaitTermination(IMAGE_LOAD_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            pool.shutdownNow();
            ErrorHandler.showTimeoutError(mainFrame, "ładowanie obrazków Pokémonów");
        }
    }

    /**
     * Handles the completion of data loading (success or failure).
     */
    private void handleDataLoadingCompletion() {
        try {
            // Check if the background task completed successfully
            // This will throw an exception if the task failed
            finishDataLoading();
        } catch (Exception e) {
            handleDataLoadingError(e);
        }
    }

    /**
     * Completes the data loading process and transitions to search view.
     */
    private void finishDataLoading() {
        notifyLoadingComplete();
        transitionToSearchView();
    }

    /**
     * Handles data loading errors with retry options.
     */
    private void handleDataLoadingError(Exception e) {
        ErrorHandler.showError(mainFrame, e, "ładowanie początkowych danych");
        
        int userChoice = showDataLoadingRetryDialog();
        if (userChoice == JOptionPane.YES_OPTION) {
            beginDataLoading();
        } else {
            System.exit(1);
        }
    }

    /**
     * Shows a retry dialog for data loading failures.
     */
    private int showDataLoadingRetryDialog() {
        return JOptionPane.showConfirmDialog(
            mainFrame,
            "Czy chcesz spróbować ponownie?",
            "Błąd ładowania",
            JOptionPane.YES_NO_OPTION
        );
    }

    /**
     * Transitions to the search view, creating it if necessary.
     */
    private void transitionToSearchView() {
        if (searchView == null) {
            createSearchView();
        } else {
            showSearchView();
        }
    }

    /**
     * Creates the search view asynchronously to avoid blocking the UI.
     */
    private void createSearchView() {
        notifyStatusChange("Przygotowywanie widoku...");
        
        new SwingWorker<SearchView, Void>() {
            @Override
            protected SearchView doInBackground() {
                return new SearchView(pokemonData, AppController.this::showPokemonDetails, null);
            }

            @Override
            protected void done() {
                try {
                    searchView = get();
                    rootPanel.add(searchView, SEARCH_VIEW);
                    showSearchView();
                } catch (Exception e) {
                    handleSearchViewCreationError(e);
                }
            }
        }.execute();
    }

    /**
     * Displays the search view.
     */
    private void showSearchView() {
        cardLayout.show(rootPanel, SEARCH_VIEW);
    }

    /**
     * Handles search view creation errors.
     */
    private void handleSearchViewCreationError(Exception e) {
        ErrorHandler.showError(mainFrame, e, "wyświetlanie widoku wyszukiwania");
        JOptionPane.showMessageDialog(
            mainFrame,
            "Nie można uruchomić aplikacji. Aplikacja zostanie zamknięta.",
            "Błąd krytyczny",
            JOptionPane.ERROR_MESSAGE
        );
        System.exit(1);
    }

    /**
     * Shows detailed information for the selected Pokemon.
     * Called when a user selects a Pokemon from the search view.
     */
    private void showPokemonDetails(Pokemon pokemon) {
        new SwingWorker<PokemonDetails, Void>() {
            @Override
            protected PokemonDetails doInBackground() throws Exception {
                return apiService.fetchPokemonDetails(pokemon.getUrl());
            }

            @Override
            protected void done() {
                try {
                    PokemonDetails details = get();
                    displayPokemonDetails(details);
                } catch (Exception e) {
                    handlePokemonDetailsError(e, pokemon);
                }
            }
        }.execute();
    }

    /**
     * Creates and displays the Pokemon details view.
     */
    private void displayPokemonDetails(PokemonDetails details) {
        detailsView = new DetailsView(details, this::returnToSearchView);
        rootPanel.add(detailsView, DETAILS_VIEW);
        cardLayout.show(rootPanel, DETAILS_VIEW);
    }

    /**
     * Handles errors when loading Pokemon details.
     */
    private void handlePokemonDetailsError(Exception e, Pokemon pokemon) {
        ErrorHandler.showError(mainFrame, e, "ładowanie szczegółów Pokémona: " + pokemon.getName());
        returnToSearchView();
    }

    /**
     * Returns to the search view from the details view.
     */
    private void returnToSearchView() {
        cardLayout.show(rootPanel, SEARCH_VIEW);
    }
}