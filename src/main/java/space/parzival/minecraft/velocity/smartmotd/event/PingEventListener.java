package space.parzival.minecraft.velocity.smartmotd.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import space.parzival.minecraft.velocity.smartmotd.config.ConfigParser;
import space.parzival.minecraft.velocity.smartmotd.config.model.ConfigModel;
import space.parzival.minecraft.velocity.smartmotd.config.model.PingInformation;
import space.parzival.minecraft.velocity.smartmotd.config.model.PlayerList;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class PingEventListener {
    private static final ServerPing.Players FALLBACK_PLAYER_LIST = new ServerPing.Players(0, 0, List.of());
    private static final Map<String, Function<ProxyPingEvent, String>> placeholders = Map.of(
            "server_hostname", event -> event.getConnection().getRawVirtualHost().orElse(""),
            "client_ip", event -> event.getConnection().getRemoteAddress().getHostName(),
            "client_port", event -> event.getConnection().getRemoteAddress().getPort() + ""
    );

    private final ConfigParser<ConfigModel> configParser;

    @Subscribe
    public void onProxyPingEvent(ProxyPingEvent event) {
        log.debug("New ProxyPingEvent received from {}", event.getConnection().getRemoteAddress().getHostName());

        switch (configParser.config.getMode()) {
            // in simple mode, the plugin will display the same motd for all servers
            case SIMPLE -> {
                PingInformation pingInformation = configParser.config.getSimple();
                event.setPing(createServerPing(pingInformation, event));
            }

            // in network mode, the plugin will display a different motd for each server in the network
            case NETWORK -> {
                String hostname = event.getConnection().getRawVirtualHost().orElse("default");
                PingInformation pingInformation = configParser.config.getNetwork().getOrDefault(hostname,
                        configParser.config.getNetwork().getOrDefault("default", null));

                if (pingInformation == null) {
                    log.warn("No ping information found for hostname {} and the default configuration is not available! " +
                            "Did you break your configuration?", hostname);
                    break;
                }

                event.setPing(createServerPing(pingInformation, event));
            }

            // in passthrough mode, the plugin will only inject placeholders into the ping message
            case PASSTHROUGH -> event.setPing(createServerPing(new PingInformation(), event));

            default -> log.warn("Unknown plugin mode: {}", configParser.config.getMode());
        }
    }


    // create a new ServerPing object and inject placeholders
    private ServerPing createServerPing(PingInformation pingInformation, ProxyPingEvent event) {
        PlayerList playerList = pingInformation.getPlayers() != null ? pingInformation.getPlayers() : new PlayerList();
        ServerPing.Players backendPlayers = event.getPing().getPlayers().orElse(FALLBACK_PLAYER_LIST);

        return new ServerPing(
                new ServerPing.Version(
                        event.getPing().getVersion().getProtocol(),
                        injectPlaceholders(pingInformation.getVersion() != null ?
                                pingInformation.getVersion() : event.getPing().getVersion().getName(), event)
                ),
                new ServerPing.Players(
                        playerList.getCurrent() != null ? playerList.getCurrent() : backendPlayers.getOnline(),
                        playerList.getMax() != null ? playerList.getMax() : backendPlayers.getMax(),
                        playerList.getSamples() != null ?
                                playerList.getSamples().stream().map(sample ->
                                        new ServerPing.SamplePlayer(
                                                injectPlaceholders(sample, event),
                                                UUID.randomUUID())).toList() :
                                backendPlayers.getSample().stream().map(samplePlayer ->
                                        new ServerPing.SamplePlayer(
                                                injectPlaceholders(samplePlayer.getName(), event),
                                                samplePlayer.getId())).toList()
                ),
                injectPlaceholders(pingInformation.getMotd() != null ?
                        MiniMessage.miniMessage().deserialize(pingInformation.getMotd()) :
                        event.getPing().getDescriptionComponent(), event),
                event.getPing().getFavicon().orElse(null),
                event.getPing().getModinfo().orElse(null)
        );
    }

    private String injectPlaceholders(String input, ProxyPingEvent event) {
        for (Map.Entry<String, Function<ProxyPingEvent, String>> entry : placeholders.entrySet()) {
            input = input.replaceAll(
                    "{{" + entry.getKey() + "}}",
                    entry.getValue().apply(event)
            );
        }
        return input;
    }

    private Component injectPlaceholders(Component input, ProxyPingEvent event) {
        String intermediate = MiniMessage.miniMessage().serialize(input);
        intermediate = injectPlaceholders(intermediate, event);
        return MiniMessage.miniMessage().deserialize(intermediate);
    }
}
