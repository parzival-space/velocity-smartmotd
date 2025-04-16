package space.parzival.minecraft.velocity.smartmotd.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.minimessage.MiniMessage;
import space.parzival.minecraft.velocity.smartmotd.config.ConfigParser;
import space.parzival.minecraft.velocity.smartmotd.config.model.ConfigModel;
import space.parzival.minecraft.velocity.smartmotd.config.model.PingInformation;
import space.parzival.minecraft.velocity.smartmotd.config.model.PlayerList;

import javax.swing.text.DateFormatter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static space.parzival.minecraft.velocity.smartmotd.util.PlaceholderParser.injectPlaceholders;

@Slf4j
@RequiredArgsConstructor
public class PingEventHandler {
    private static final ServerPing.Players FALLBACK_PLAYER_LIST = new ServerPing.Players(0, 0, List.of());
    private static final Map<String, Function<ProxyPingEvent, String>> placeholders = Map.of(
            "server_hostname", event -> event.getConnection().getRawVirtualHost().orElse(""),
            "client_ip", event -> event.getConnection().getRemoteAddress().getHostName(),
            "client_port", event -> String.valueOf(event.getConnection().getRemoteAddress().getPort()),
            "ping_time", event -> new SimpleDateFormat("hh:mm aa").format(new Date()),
            "ping_time_24h", event -> new SimpleDateFormat("HH:mm").format(new Date()),
            "ping_date", event -> new SimpleDateFormat("dd.MM.yyyy").format(new Date())
    );

    private final ConfigParser<ConfigModel> configParser;
    private final Path dataDirectory;

    @Subscribe
    public void onProxyPingEvent(ProxyPingEvent event) {
        log.debug("New ProxyPingEvent received from {}", event.getConnection().getRemoteAddress().getHostName());

        switch (configParser.getConfig().getMode()) {
            // in simple mode, the plugin will display the same motd for all servers
            case SIMPLE -> {
                PingInformation pingInformation = configParser.getConfig().getSimple();
                event.setPing(createServerPing(pingInformation, event));
            }

            // in network mode, the plugin will display a different motd for each server in the network
            case NETWORK -> {
                String hostname = event.getConnection().getRawVirtualHost().orElse("default");
                PingInformation pingInformation = configParser.getConfig().getNetwork().getOrDefault(hostname,
                        configParser.getConfig().getNetwork().getOrDefault("default", null));

                if (pingInformation == null) {
                    log.warn("No ping information found for hostname {} and the default configuration is not available! " +
                            "Did you break your configuration?", hostname);
                    break;
                }

                event.setPing(createServerPing(pingInformation, event));
            }

            // in passthrough mode, the plugin will only inject placeholders into the ping message
            case PASSTHROUGH -> event.setPing(createServerPing(new PingInformation(), event));

            default -> log.warn("Unknown plugin mode: {}", configParser.getConfig().getMode());
        }
    }


    // create a new ServerPing object and inject placeholders
    private ServerPing createServerPing(PingInformation pingInformation, ProxyPingEvent event) {
        PlayerList playerList = pingInformation.getPlayers() != null ? pingInformation.getPlayers() : new PlayerList();
        ServerPing.Players backendPlayers = event.getPing().getPlayers().orElse(FALLBACK_PLAYER_LIST);

        String faviconPath = pingInformation.getFavicon() != null ?
                Paths.get(dataDirectory.toString(), pingInformation.getFavicon()).toString() :
                null;
        log.info("Loading favicon from path: {}", faviconPath);

        return new ServerPing(
                // set protocol version (passthrough mode) and version name
                new ServerPing.Version(
                        event.getPing().getVersion().getProtocol(),
                        injectPlaceholders(pingInformation.getVersion() != null ?
                                pingInformation.getVersion() : event.getPing().getVersion().getName(), placeholders, event)
                ),

                // set players
                new ServerPing.Players(
                        playerList.getCurrent() != null ? playerList.getCurrent() : backendPlayers.getOnline(),
                        playerList.getMax() != null ? playerList.getMax() : backendPlayers.getMax(),
                        playerList.getSamples() != null ?
                                playerList.getSamples().stream().map(sample ->
                                        new ServerPing.SamplePlayer(
                                                injectPlaceholders(sample, placeholders, event),
                                                UUID.randomUUID())).toList() :
                                backendPlayers.getSample().stream().map(samplePlayer ->
                                        new ServerPing.SamplePlayer(
                                                injectPlaceholders(samplePlayer.getName(), placeholders, event),
                                                samplePlayer.getId())).toList()
                ),

                // set description (motd)
                injectPlaceholders(pingInformation.getMotd() != null ?
                        MiniMessage.miniMessage().deserialize(pingInformation.getMotd()) :
                        event.getPing().getDescriptionComponent(), placeholders, event),

                // set favicon
                faviconPath != null ?
                        loadFavicon(faviconPath) :
                        event.getPing().getFavicon().orElse(null),

                // set mod list
                event.getPing().getModinfo().orElse(null)
        );
    }

    private Favicon loadFavicon(String pathOrBase64Url) {
        if (pathOrBase64Url.startsWith("data:image/png;base64,"))
            return new Favicon(pathOrBase64Url);

        try {
            return Favicon.create(Path.of(pathOrBase64Url));
        } catch (IOException e) {
            log.warn("Failed to load favicon from path: {}", pathOrBase64Url, e);

            // create missing texture
            return Favicon.create(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
        }
    }
}
