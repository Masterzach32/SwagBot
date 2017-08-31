package xyz.swagbot.utils;/*
 * SwagBot - Created on 8/30/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zach Kozar
 * @version 8/30/2017
 */
public class Utils {

    /**
     * Return a list of key/value pairs indicated by key=value
     *
     * @param args
     * @param start
     * @param end
     * @return
     */
    public static Map<String, String> getParams(String[] args, int start, int end) {
        Map<String, String> mapArgs = new LinkedHashMap<>();

        for (int i = start; i < end; i++) {
            String s = args[i];

            if (!s.contains("="))
                continue;

            int index = s.indexOf("=");
            mapArgs.put(s.substring(0, index), index == s.length() - 1 ? "" : s.substring(index + 1));
        }

        return mapArgs;
    }

    /**
     * Return a list of key/value pairs indicated by key=value
     *
     * @param args
     * @param start
     * @return
     */
    public static Map<String, String> getParams(String[] args, int start) {
        return getParams(args, start, args.length);
    }

    /**
     * Concatenate a string from an array.
     *
     * @param args
     * @param start
     * @param end
     * @return
     */
    public static String getContent(String[] args, int start, int end) {
        String content = "";
        for (int i = start; i < Math.min(end, args.length); i++) {
            content += args[i];
            if (i != Math.min(end, args.length) - 1) {
                content += " ";
            }
        }

        return content;
    }

    /**
     * Concatenate a string from an array.
     *
     * @param args
     * @param start
     * @return
     */
    public static String getContent(String[] args, int start) {
        return getContent(args, start, args.length);
    }

    /**
     * Delimits the string by the regex, trimming and removing tokens that are null or empty.
     *
     * @param content
     * @param regex
     * @return
     */
    public static String[] delimitWithoutEmpty(String content, String regex) {
        List<String> list = new ArrayList<>(Arrays.asList(content.split(regex)));
        list = list.stream().map(String::trim).filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());

        return list.toArray(new String[list.size()]);
    }

    public static boolean startsWithVowel(String s) {
        s = s.toLowerCase(Locale.ROOT);
        return s.startsWith("a") || s.startsWith("e") || s.startsWith("i") || s.startsWith("o") || s.startsWith("u");
    }

    public static String stripExtension(String fileName) {
        if (fileName.lastIndexOf('.') > 0) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }

        return fileName;
    }

    /**
     * Formats as D:HH:MM:SS, with at least one zero in minutes slot, and one zero in hours if hours exist
     *
     * @param seconds
     * @return
     */
    public static String formatTime(float seconds) {
        StringBuilder sb = new StringBuilder();

        int days = (int) (seconds / (3600 * 24));
        int hours = (int) (seconds / 3600 % 24);
        int minutes = (int) (seconds / 60 % 60);
        int modSeconds = (int) (seconds % 60);

        if (days > 0) {
            sb.append(days);
            sb.append(":");
        }

        if (hours > 0) {
            if (hours < 10)
                sb.append("0");
            sb.append(hours);
            sb.append(":");
        } else if (days > 0) {
            sb.append("00:");
        }

        if (minutes > 0) {
            if (minutes < 10 && (hours > 0 || days > 0))
                sb.append("0");
            sb.append(minutes);
        } else {
            sb.append("0");

            if (hours > 0 || days > 0)
                sb.append("0");
        }

        sb.append(":");

        if (modSeconds < 10)
            sb.append("0");
        sb.append(modSeconds);

        if (seconds < 1f && seconds > 0f) {
            return "0:0" + String.format("%.3f", Math.max(seconds, 0.001f));
        }

        return sb.toString();
    }

    public static String trail(String string, int cutoff) {
        if (string.length() > cutoff) {
            return string.substring(0, cutoff) + "...";
        }

        return string;
    }

    public static float getSecondsFromFormattedTime(String input) {
        float current = 0;

        String[] split = input.split(":");

        try {
            for (int i = split.length - 1, num = 0; i >= 0; i--, num++) {
                String s = split[i];
                switch (num) {
                    case 0:
                        current += Float.parseFloat(s);
                        break;
                    case 1:
                        current += Long.parseLong(s) * 60;
                        break;
                    case 2:
                        current += Long.parseLong(s) * 60 * 60;
                        break;
                    case 3:
                        current += Long.parseLong(s) * 60 * 60 * 24;
                        break;
                }
            }
        } catch (NumberFormatException e) {
            return Float.NaN;
        }

        return current;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public static float lerp(float x, float y, float alpha) {
        return (float) lerp((double) x, y, alpha);
    }

    public static double lerp(double x, double y, double alpha) {
        return x + alpha * (y - x);
    }

    public static String repeat(String s, int times) {
        if (times == 0)
            return "";
        if (times < 0)
            throw new IllegalArgumentException("Times must be positive!");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }

        return sb.toString();
    }

    /**
     * Case insensitively finds an enum
     *
     * @param what
     * @param clazz
     * @return
     */
    public static <E extends Enum> E findEnum(String what, Class<E> clazz) {
        E[] all = clazz.getEnumConstants();
        for (E e : all) {
            if (e.name().equalsIgnoreCase(what))
                return e;
        }

        return null;
    }

    public static <E extends Enum> E findEnum(String what, Class<E> clazz, E def) {
        E result = findEnum(what, clazz);

        if (result == null) {
            return def;
        } else
            return result;
    }
}