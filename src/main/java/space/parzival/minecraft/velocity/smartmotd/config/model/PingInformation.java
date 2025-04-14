package space.parzival.minecraft.velocity.smartmotd.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Data
@NoArgsConstructor
public class PingInformation {
    /**
     * The version string of the server.
     */
    private @Nullable String version;

    /**
     * The MOTD (Message of the Day) to display to players.
     */
    private @Nullable String motd;

    /**
     * The relative path of the data directory to an image file or a base64 encoded image.
     */
    private @Nullable String favicon;

    /**
     * Represents what the players the server purports to have online, its maximum capacity,
     * and a sample of players on the server.
     */
    private @Nullable PlayerList players;
}
