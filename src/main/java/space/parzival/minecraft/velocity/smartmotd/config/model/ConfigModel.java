package space.parzival.minecraft.velocity.smartmotd.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ConfigModel {
    /**
     * The working mode of the plugin.
     */
    private PluginMode mode;

    /**
     * The information to display when the plugin mode is SIMPLE.
     */
    private PingInformation simple;

    /**
     * The information to display when the plugin mode is NETWORK.
     * Key: The hostname of the server.
     * Value: The information to display for that server.
     */
    private Map<String, PingInformation> network;
}
