package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.Version;
import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.util.ConfigUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.FriendsList;
import seedu.address.model.GamesList;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyFriendsList;
import seedu.address.model.ReadOnlyGamesList;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.FriendsListStorage;
import seedu.address.storage.GamesListStorage;
import seedu.address.storage.JsonFriendsListStorage;
import seedu.address.storage.JsonGamesListStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.UserPrefsStorage;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(0, 2, 0, true);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected Config config;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        AppParameters appParameters = AppParameters.parse(getParameters());
        config = initConfig(appParameters.getConfigPath());

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        FriendsListStorage friendsListStorage = new JsonFriendsListStorage(userPrefs.getFriendsListFilePath());
        GamesListStorage gamesListStorage = new JsonGamesListStorage(userPrefs.getGamesListFilePath());
        storage = new StorageManager(friendsListStorage, gamesListStorage, userPrefsStorage);

        initLogging(config);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s friends list, games list and
     * {@code userPrefs}. <br>
     * The data from the sample friend's list will be used instead if {@code storage}'s friends list is not found,
     * or an empty friends list will be used instead if errors occur when reading {@code storage}'s friends list.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        // TODO: refine and update this method
        // necessary to use empty friends list if the games list storage faces loading errors - since
        // friends may have GameFriendLinks to games that do not exist due to failure loading.
        ReadOnlyGamesList initialGameData;
        ReadOnlyFriendsList initialFriendData;
        try {
            Optional<ReadOnlyGamesList> readOnlyGamesList = storage.readGamesList();
            if (readOnlyGamesList.isEmpty()) {
                logger.info("Game data file not found. Will be starting with a empty friends and games list.");
                initialGameData = new GamesList();
                initialFriendData = new FriendsList();
            } else {
                initialGameData = readOnlyGamesList.get();
                initialFriendData = loadInitialFriendData(storage);
            }
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty friends and "
                    + "games list.");
            initialGameData = new GamesList();
            initialFriendData = new FriendsList();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty friends and "
                    + "games list.");
            initialGameData = new GamesList();
            initialFriendData = new FriendsList();
        }

        return new ModelManager(initialFriendData, initialGameData, userPrefs);
    }

    private ReadOnlyFriendsList loadInitialFriendData(Storage storage) {
        try {
            Optional<ReadOnlyFriendsList> readOnlyFriendsList = storage.readFriendsList();
            if (readOnlyFriendsList.isEmpty()) {
                logger.info("Data file not found. Will be starting with a sample friend's list");
            }
            return readOnlyFriendsList.orElseGet(SampleDataUtil::getSampleFriendsList);
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty friends list.");
            return new FriendsList();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty friends list.");
            return new FriendsList();
        }
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     */
    protected Config initConfig(Path configFilePath) {
        Config initializedConfig;
        Path configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataConversionException e) {
            logger.warning("Config file at " + configFilePathUsed + " is not in the correct format. "
                    + "Using default config properties");
            initializedConfig = new Config();
        }

        //Update config file in case it was missing to begin with or there are new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using prefs file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataConversionException e) {
            logger.warning("UserPrefs file at " + prefsFilePath + " is not in the correct format. "
                    + "Using default user prefs");
            initializedPrefs = new UserPrefs();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty AddressBook");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping Address Book ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
