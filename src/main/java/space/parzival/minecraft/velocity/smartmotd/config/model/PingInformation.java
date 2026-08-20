package space.parzival.minecraft.velocity.smartmotd.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PingInformation {
    /**
     * The version string of the server.
     */
    private String version;

    /**
     * The MOTD (Message of the Day) to display to players.
     */
    private String motd;

    /**
     * The relative path of the data directory to an image file or a base64 encoded image.
     */
    private String favicon;

    /**
     * Represents what the players the server purports to have online, its maximum capacity,
     * and a sample of players on the server.
     */
    private PlayerList players;
}
