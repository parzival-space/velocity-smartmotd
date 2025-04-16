package space.parzival.minecraft.velocity.smartmotd.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholderParserTest {
    private static final Map<String, Function<Integer, String>> placeholders = Map.of(
            "X", String::valueOf
    );

    @Test
    void injectPlaceholders_shouldReplace_String() {
        String placeholderString = "Example {{X}}";
        String expectedString = "Example 42";

        String result = PlaceholderParser.injectPlaceholders(placeholderString, placeholders, 42);
        assertEquals(expectedString, result, "Placeholder string should be replaced with the value");
    }

    @Test
    void injectPlaceholders_shouldReplace_Component() {
        Component placeholderComponent = MiniMessage.miniMessage().deserialize("<red>Example {{X}}</red>");
        Component expectedComponent = MiniMessage.miniMessage().deserialize("<red>Example 42</red>");

        Component result = PlaceholderParser.injectPlaceholders(placeholderComponent, placeholders, 42);
        assertEquals(expectedComponent, result, "Placeholder component should be replaced with the value");
    }
}