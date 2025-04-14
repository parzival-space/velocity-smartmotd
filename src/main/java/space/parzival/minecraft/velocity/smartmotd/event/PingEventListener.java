package space.parzival.minecraft.velocity.smartmotd.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import space.parzival.minecraft.velocity.smartmotd.SmartMotdPlugin;
import space.parzival.minecraft.velocity.smartmotd.config.ConfigParser;
import space.parzival.minecraft.velocity.smartmotd.config.model.ConfigModel;
import space.parzival.minecraft.velocity.smartmotd.config.model.PingInformation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class PingEventListener {
    private final SmartMotdPlugin plugin;
    private final ConfigParser<ConfigModel> configParser;

    private static final Map<String, Function<ProxyPingEvent, String>> placeholders = Map.of(
            "server_hostname", event -> event.getConnection().getRawVirtualHost().orElse(""),
            "client_ip", event -> event.getConnection().getRemoteAddress().getHostName(),
            "client_port", event -> event.getConnection().getRemoteAddress().getPort() + ""
    );

    @Subscribe
    public void onProxyPingEvent(ProxyPingEvent event) {
        log.debug("New ProxyPingEvent received from {}", event.getConnection().getRemoteAddress().getHostName());

        switch (configParser.config.getMode()) {
            case SIMPLE -> handleSimpleMode(event);
            case NETWORK -> handleNetworkMode(event);
            case PASSTHROUGH -> handlePassthroughMode(event);
            default -> log.warn("Unknown plugin mode: {}", configParser.config.getMode());
        }
    }

    private void handleSimpleMode(ProxyPingEvent event) {
        event.setPing(createServerPing(
                event,
                configParser.config.getSimple().getVersion(),
                configParser.config.getSimple().getMotd(),
                configParser.config.getSimple().getPlayers().getCurrent(),
                configParser.config.getSimple().getPlayers().getMax(),
                configParser.config.getSimple().getPlayers().getSamples()
        ));
    }

    private void handleNetworkMode(ProxyPingEvent event) {
        String hostname = event.getConnection().getRawVirtualHost().orElse("default");
        PingInformation pingInformation = configParser.config.getNetwork().getOrDefault(hostname,
                configParser.config.getNetwork().getOrDefault("default", null));

        if (pingInformation == null) {
            log.warn("No ping information found for hostname {} and the default configuration is not available! " +
                    "Did you break your configuration?", hostname);
            return;
        }

        event.setPing(createServerPing(
                event,
                pingInformation.getVersion(),
                pingInformation.getMotd(),
                pingInformation.getPlayers().getCurrent(),
                pingInformation.getPlayers().getMax(),
                pingInformation.getPlayers().getSamples()
        ));
    }

    private void handlePassthroughMode(ProxyPingEvent event) {
        event.setPing(createServerPing(
                event,
                event.getPing().getVersion().getName(),
                MiniMessage.miniMessage().serialize(event.getPing().getDescriptionComponent()),
                event.getPing().getPlayers().get().getOnline(),
                event.getPing().getPlayers().get().getMax(),
                event.getPing().getPlayers().get().getSample().stream()
                        .map(ServerPing.SamplePlayer::getName)
                        .toList()
        ));
    }

    private ServerPing createServerPing(
            ProxyPingEvent event,
            @Nullable String version,
            @Nullable String description,
            int onlinePlayers,
            int maxPlayers,
            @Nullable List<String> samplePlayers) {
        String descriptionWithPlaceholders = description == null ? "" : description;
        String versionWithPlaceholders = version == null ? "" : version;
        List<String> samplePlayersWithPlaceholders = samplePlayers == null ? List.of() : samplePlayers;

        // replace placeholders
        for (Map.Entry<String, Function<ProxyPingEvent, String>> entry : placeholders.entrySet()) {
            descriptionWithPlaceholders = descriptionWithPlaceholders.replace(
                    "{{" + entry.getKey() + "}}",
                    entry.getValue().apply(event)
            );
            versionWithPlaceholders = versionWithPlaceholders.replace(
                    "{{" + entry.getKey() + "}}",
                    entry.getValue().apply(event)
            );
            samplePlayersWithPlaceholders = samplePlayersWithPlaceholders.stream()
                    .map(player -> player.replace("{{" + entry.getKey() + "}}", entry.getValue().apply(event)))
                    .toList();
        }

        return new ServerPing(
                new ServerPing.Version(
                        event.getPing().getVersion().getProtocol(),
                        versionWithPlaceholders),
                new ServerPing.Players(
                        onlinePlayers,
                        maxPlayers,
                        samplePlayersWithPlaceholders.stream().map(samplePlayer ->
                                new ServerPing.SamplePlayer(samplePlayer, UUID.randomUUID())).toList()),
                MiniMessage.miniMessage().deserialize(descriptionWithPlaceholders),
                event.getPing().getFavicon().orElse(null) // FIXME: add favicon support
        );
    }
}
