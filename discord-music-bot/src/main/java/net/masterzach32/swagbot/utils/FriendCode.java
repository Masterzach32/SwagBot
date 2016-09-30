package net.masterzach32.swagbot.utils;

public class FriendCode {

    /**
     * Converts a Discord ID (64-bit) to a friend code (XXXX-XXXX-XXXX-XXXX)
     *
     * @param id
     * @return the code, or null if it's malformed
     */
    public static String idToCode(String id) {
        try {
            long parsed = Long.parseUnsignedLong(id);

            return String.format("%04X", (int) ((parsed & 0xFFFF000000000000L) >> 48)) + "-" +
                    String.format("%04X", (int) ((parsed & 0x0000FFFF00000000L) >> 32)) + "-" +
                    String.format("%04X", (int) ((parsed & 0x00000000FFFF0000L) >> 16)) + "-" +
                    String.format("%04X", (int) (parsed & 0x000000000000FFFFL));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts a friend code (XXXX-XXXX-XXXX-XXXX) to a Discord ID (64-bit)
     *
     * @param code
     * @return the ID, or null if it's malformed
     */
    public static String codeToID(String code) {
        code = code.trim();
        if (code.length() == 19 && code.matches("((([0-9A-Fa-f]){4}-){3})([0-9A-Fa-f]){4}")) {
            return Long.toUnsignedString(Long.parseLong(code.replace("-", ""), 16), 10);
        } else
            return null;
    }

}