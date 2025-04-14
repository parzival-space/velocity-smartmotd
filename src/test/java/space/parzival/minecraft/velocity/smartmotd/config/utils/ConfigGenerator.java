package space.parzival.minecraft.velocity.smartmotd.config.utils;

import space.parzival.minecraft.velocity.smartmotd.config.model.ConfigModel;
import space.parzival.minecraft.velocity.smartmotd.config.model.PingInformation;
import space.parzival.minecraft.velocity.smartmotd.config.model.PlayerList;
import space.parzival.minecraft.velocity.smartmotd.config.model.PluginMode;

import java.util.List;
import java.util.Map;

public class ConfigGenerator {
    public static PlayerList getPlayerList() {
        PlayerList playerList = new PlayerList();
        playerList.setCurrent(50);
        playerList.setMax(100);
        playerList.setSamples(
                List.of(
                        "Player1",
                        "Player2",
                        "Player3",
                        "Player4",
                        "Player5"
                )
        );
        return playerList;
    }

    public static PingInformation getPingInformation() {
        return getPingInformation(
                "1.16.5",
                "Welcome to the server!",
                "https://example.com/favicon.png",
                getPlayerList()
        );
    }

    public static PingInformation getPingInformation(String version, String motd, String favicon, PlayerList playerList) {
        PingInformation pingInformation = new PingInformation();
        pingInformation.setVersion("1.16.5");
        pingInformation.setMotd("Welcome to the customized server!");
        pingInformation.setFavicon("https://example.com/custom_favicon.png");
        pingInformation.setPlayers(playerList);
        return pingInformation;
    }

    public static ConfigModel getConfigModel() {
        ConfigModel configModel = new ConfigModel();
        configModel.setMode(PluginMode.SIMPLE);
        configModel.setSimple(getPingInformation());
        configModel.setNetwork(Map.of(
                "example.com", getPingInformation(
                        "1.16.5",
                        "Welcome to the example server!",
                        "https://example.com/favicon.png",
                        getPlayerList()),
                "another-example.com", getPingInformation(
                        "1.17.1",
                        "Welcome to another example server!",
                        "https://another-example.com/favicon.png",
                        getPlayerList())
        ));
        return configModel;
    }
}
