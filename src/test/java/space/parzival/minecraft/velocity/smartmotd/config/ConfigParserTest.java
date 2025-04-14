package space.parzival.minecraft.velocity.smartmotd.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.parzival.minecraft.velocity.smartmotd.config.model.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {
    private final String defaultConfigResourceStreamPath = "/config.yml";
    private final String configFileName = "config.yaml";

    @Test
    void ConfigParser_shouldCreatesConfig_whenFileDoesNotExist(@TempDir Path workDir) throws IOException {
        File configFile = Path.of(workDir.toAbsolutePath().toString(), configFileName).toFile();

        ConfigParser<Config> configParser = new ConfigParser<>(configFile, defaultConfigResourceStreamPath, Config.class);

        assertTrue(configFile.exists(), "Config file should be created");
    }

    @Test
    void ConfigParser_shouldCreateEmptyConfig_whenInvalidStreamIsProvided(@TempDir Path workDir) throws IOException {
        File configFile = Path.of(workDir.toAbsolutePath().toString(), configFileName).toFile();

        // try parsing
        ConfigParser<Config> configParser = new ConfigParser<>(configFile, "/invalid/path/to/config.yml", Config.class);

        assertNull(configParser.config, "Config should be null");
    }

    @Test
    void ConfigParser_shouldParseConfig(@TempDir Path workDir) throws IOException {
        File configFile = Path.of(workDir.toAbsolutePath().toString(), configFileName).toFile();

        InputStream configFileStream = getClass().getResourceAsStream(defaultConfigResourceStreamPath);
        assert configFileStream != null;
        Files.copy(configFileStream, configFile.toPath());

        // try parsing
        ConfigParser<Config> configParser = new ConfigParser<>(configFile, defaultConfigResourceStreamPath, Config.class);

        assertNotNull(configParser.config, "Config should not be null");
        assertEquals("SIMPLE", configParser.config.getMode(), "Config mode should be 'SIMPLE'");

    }

    @Test
    void ConfigParser_shouldWriteConfig_whenSaveIsCalled(@TempDir Path workDir) throws IOException {
        File configFile = Path.of(workDir.toAbsolutePath().toString(), configFileName).toFile();

        InputStream configFileStream = getClass().getResourceAsStream(defaultConfigResourceStreamPath);
        assert configFileStream != null;
        Files.copy(configFileStream, configFile.toPath());

        // try parsing
        ConfigParser<Config> configParser = new ConfigParser<>(configFile, defaultConfigResourceStreamPath, Config.class);

        assertNotNull(configParser.config, "Config should not be null");
        assertEquals("SIMPLE", configParser.config.getMode(), "Config mode should be 'SIMPLE'");
        String oldConfigFileContent = Files.readString(configParser.getConfigFile().toPath());

        // change the mode
        configParser.config.setMode("TEST");
        configParser.save();
        String newConfigFileContent = Files.readString(configParser.getConfigFile().toPath());

        assertNotEquals(oldConfigFileContent, newConfigFileContent, "Config file should be changed");
    }
}