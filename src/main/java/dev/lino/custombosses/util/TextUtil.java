package dev.lino.custombosses.util;

import net.md_5.bungee.api.ChatColor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})|<#([A-Fa-f0-9]{6})>");

    private TextUtil() {}

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexGroup = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            ChatColor hexColor = ChatColor.of("#" + hexGroup);
            matcher.appendReplacement(buffer, hexColor.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static List<String> colorize(List<String> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<String> colored = new ArrayList<>(list.size());
        for (String line : list) {
            colored.add(colorize(line));
        }
        return colored;
    }

    public static String stripColor(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.stripColor(colorize(text));
    }
}
