package net.masterzach32.swagbot.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

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
}