package space.parzival.minecraft.velocity.smartmotd;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
@Plugin(
        id = "smartmotd",
        name = "SmartMotd",
        version = "1.0.0",
        description = "Dynamic Motd's for your Velocity network",
        authors = {
                "Parzival (parzival-space) <me@parzival.space>"
        }
)
public class SmartMotdPlugin {
    @Getter
    private static SmartMotdPlugin instance;

    private final Path dataDirectory;
    private final ProxyServer proxyServer;

    public SmartMotdPlugin(ProxyServer proxyServer, @DataDirectory Path dataDirectory) {
        if (instance != null) throw new IllegalStateException("Plugin already initialized");
        instance = this; // NOSONAR - this is a singleton pattern, not a static context

        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
    }
}
