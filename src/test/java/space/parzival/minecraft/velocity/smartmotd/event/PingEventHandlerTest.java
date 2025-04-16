package space.parzival.minecraft.velocity.smartmotd.event;

import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import space.parzival.minecraft.velocity.smartmotd.config.ConfigParser;
import space.parzival.minecraft.velocity.smartmotd.config.model.ConfigModel;
import space.parzival.minecraft.velocity.smartmotd.config.model.PluginMode;
import space.parzival.minecraft.velocity.smartmotd.config.util.ConfigGenerator;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PingEventHandlerTest {
    private InboundConnection mockConnection;
    private ProxyPingEvent mockEvent;
    private ConfigParser<ConfigModel> mockConfigParser;
    private ServerPing backendPing;
    private ConfigModel mockConfig;

    @BeforeEach
    void setUp() {
        mockEvent = mock(ProxyPingEvent.class);

        // mock config parser
        mockConfigParser = mock(ConfigParser.class);
        mockConfig = ConfigGenerator.getConfigModel();
        when(mockConfigParser.getConfig()).thenReturn(mockConfig);

        // mock client connection
        mockConnection = mock(InboundConnection.class);
        InetSocketAddress mockAddress = new InetSocketAddress("mockclient", 12345);
        when(mockConnection.getRemoteAddress()).thenReturn(mockAddress);

        when(mockEvent.getConnection()).thenReturn(mockConnection);

        // mock backend ping result
        backendPing = new ServerPing(
                new ServerPing.Version(1, "1.19.2"),
                new ServerPing.Players(0, 0, List.of()),
                Component.text("backendPing motd"),
                null
        );
        when(mockEvent.getPing()).thenReturn(backendPing);
    }

    @Test
    void handlePingEvent_passthrough() {
        mockConfig.setMode(PluginMode.PASSTHROUGH);

        PingEventHandler pingEventHandler = new PingEventHandler(mockConfigParser, Path.of("/"));
        assertDoesNotThrow(() -> pingEventHandler.onProxyPingEvent(mockEvent));

        // verify that the original ping is passed through
        assertEquals(backendPing.getDescriptionComponent(), mockEvent.getPing().getDescriptionComponent());
        assertEquals(backendPing.getVersion(), mockEvent.getPing().getVersion());
        assertEquals(backendPing.getPlayers(), mockEvent.getPing().getPlayers());
    }

    @Test
    void handlePingEvent_simple() {
        mockConfig.setMode(PluginMode.SIMPLE);

        PingEventHandler pingEventHandler = new PingEventHandler(mockConfigParser, Path.of("/"));
        assertDoesNotThrow(() -> pingEventHandler.onProxyPingEvent(mockEvent));

        // verify that the original ping got replaced
        ArgumentCaptor<ServerPing> pingCaptor = ArgumentCaptor.forClass(ServerPing.class);
        verify(mockEvent).setPing(pingCaptor.capture());

        assert mockConfig.getSimple().getVersion() != null;
        assert mockConfig.getSimple().getPlayers() != null;
        assert mockConfig.getSimple().getPlayers().getCurrent() != null;
        assert mockConfig.getSimple().getPlayers().getMax() != null;
        assert mockConfig.getSimple().getMotd() != null;
        assertEquals(Component.text(mockConfig.getSimple().getMotd()), pingCaptor.getValue().getDescriptionComponent());
        assertEquals(mockConfig.getSimple().getVersion(), pingCaptor.getValue().getVersion().getName());
        assertFalse(pingCaptor.getValue().getPlayers().isEmpty());
        assertEquals(mockConfig.getSimple().getPlayers().getCurrent(), pingCaptor.getValue().getPlayers().get().getOnline());
        assertEquals(mockConfig.getSimple().getPlayers().getMax(), pingCaptor.getValue().getPlayers().get().getMax());
    }

    @ParameterizedTest
    @CsvSource({
            "example.com,Welcome to the example server!",
            "another-example.com,Welcome to another example server!",
            "random.com,Default MOTD"
    })
    void handlePingEvent_network(String hostname, String expectedMotd) {
        mockConfig.setMode(PluginMode.NETWORK);

        // inject hostname
        when(mockConnection.getRawVirtualHost()).thenReturn(Optional.of(hostname));

        PingEventHandler pingEventHandler = new PingEventHandler(mockConfigParser, Path.of("/"));
        assertDoesNotThrow(() -> pingEventHandler.onProxyPingEvent(mockEvent));

        // verify that the original ping got replaced for the given hostname
        ArgumentCaptor<ServerPing> pingCaptor = ArgumentCaptor.forClass(ServerPing.class);
        verify(mockEvent).setPing(pingCaptor.capture());

        assertEquals(Component.text(expectedMotd), pingCaptor.getValue().getDescriptionComponent());
    }

}