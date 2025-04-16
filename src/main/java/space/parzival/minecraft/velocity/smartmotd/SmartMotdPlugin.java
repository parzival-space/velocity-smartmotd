package space.parzival.minecraft.velocity.smartmotd;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import space.parzival.minecraft.velocity.smartmotd.config.ConfigParser;
import space.parzival.minecraft.velocity.smartmotd.config.model.ConfigModel;
import space.parzival.minecraft.velocity.smartmotd.event.PingEventHandler;

import java.nio.file.Path;

@Slf4j
@Plugin(
        id = "smartmotd",
        name = "SmartMotd",
        version = "1.0.0",
        description = "Dynamic Motd's for your Velocity network",
        authors = {
                "Parzival (parzival-space) <me@parzival.space>"
        },
        url = "https://github.com/parzival-space/velocity-smartmotd"
)
public class SmartMotdPlugin {
    @Getter
    private static SmartMotdPlugin instance;

    private final Path dataDirectory;
    private final ProxyServer proxyServer;

    @Inject
    public SmartMotdPlugin(ProxyServer proxyServer, @DataDirectory Path dataDirectory) {
        if (instance != null) throw new IllegalStateException("Plugin already initialized");
        instance = this; // NOSONAR - this is a singleton pattern, not a static context

        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ConfigParser<ConfigModel> configParser = new ConfigParser<>(
                this.dataDirectory.resolve("config.yml").toFile(),
                "/config.yml",
                ConfigModel.class
        );

        this.proxyServer.getEventManager().register(this, new PingEventHandler(configParser, dataDirectory));
        log.info("SmartMotd has been enabled!");
    }
}
