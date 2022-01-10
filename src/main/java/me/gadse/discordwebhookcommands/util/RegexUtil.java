package me.gadse.discordwebhookcommands.util;

import java.util.regex.Pattern;

public enum RegexUtil {
    PLAYER_NAME("%player_name%"),
    PLAYER_UUID("%player_uuid%"),
    COOLDOWN("%cooldown%"),
    WEBHOOK("%webhook%"),
    CONTENT("%content%"),
    PARAGRAPH("§");

    private final Pattern pattern;

    RegexUtil(String regex) {
        pattern = Pattern.compile(regex);
    }

    public String replaceAll(String input, String replacement) {
        return pattern.matcher(input).replaceAll(replacement);
    }

}
