package space.parzival.minecraft.velocity.smartmotd.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
public class ConfigParser<T> {
    private final Yaml yaml;

    @Getter
    private final File configFile;

    /**
     * The config object that is loaded from the config file.
     */
    public T config;

    public ConfigParser(File configFile, String defaultConfigResourceStreamPath, Class<T> configClass) {
        // create directories if they don't exist
        if (!configFile.getParentFile().exists())
            configFile.getParentFile().mkdirs();

        this.configFile = configFile;

        // create file if it doesn't exist
        if (!configFile.exists()) {
            try (InputStream defaultConfigFileStream = getClass().getResourceAsStream(defaultConfigResourceStreamPath)) {
                if (defaultConfigFileStream != null)
                    Files.copy(defaultConfigFileStream, configFile.toPath());
                else
                    configFile.createNewFile(); // NOSONAR - intentionally ignore result
            } catch (IOException e) {
                log.error("Failed to create default config file", e);
            }
        }

        Constructor constructor = new Constructor(configClass);
        constructor.getPropertyUtils().setSkipMissingProperties(true);

        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);

        // load the YAML parser
        this.yaml = new Yaml(constructor, representer);
        T configObject = null; // NOSONAR - intentionally set to null
        try (FileReader configFileReader = new FileReader(configFile)) {
            configObject = yaml.load(configFileReader);
        } catch (IOException e) {
            log.error("Failed to load config file. Falling back to defaults.", e);
        }
        this.config = configObject;
    }

    /**
     * Write the changes made to this.config back to the config file.
     * @throws IOException if the config file cannot be written to
     */
    public void save() throws IOException {
        // create directories if they don't exist
        if (!configFile.getParentFile().exists())
            configFile.getParentFile().mkdirs();

        // save the config file
        Files.writeString(configFile.toPath(), this.yaml.dump(config));
    }
}
