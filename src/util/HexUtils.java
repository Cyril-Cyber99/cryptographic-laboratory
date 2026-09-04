package util;

/**
 * Utilitaires pour la manipulation et la conversion de chaînes et tableaux d'octets hexadécimaux.
 * Indispensable pour l'affichage pédagogique des blocs AES de 128 bits (16 octets).
 */
public final class HexUtils {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private HexUtils() {
        // Constructeur privé pour classe utilitaire
    }

    /**
     * Convertit un tableau d'octets en chaîne hexadécimale majuscule continue.
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Convertit un tableau d'octets en chaîne hexadécimale avec espaces entre octets.
     * Exemple : "4A 3F 00 1B"
     */
    public static String bytesToSpacedHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
            if (i < bytes.length - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    /**
     * Formate un tableau d'octets en blocs AES de 16 octets (32 caractères hex) par ligne.
     */
    public static String formatBlocks(byte[] data, int blockSize) {
        if (data == null || data.length == 0) {
            return "[Données vides]";
        }
        StringBuilder sb = new StringBuilder();
        int totalBlocks = (int) Math.ceil((double) data.length / blockSize);

        for (int b = 0; b < totalBlocks; b++) {
            int start = b * blockSize;
            int length = Math.min(blockSize, data.length - start);
            byte[] blockBytes = new byte[length];
            System.arraycopy(data, start, blockBytes, 0, length);

            sb.append(String.format("Bloc %02d [%02d octets] : %s", b + 1, length, bytesToSpacedHex(blockBytes)));
            if (b < totalBlocks - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Convertit une chaîne hexadécimale (avec ou sans espaces) en tableau d'octets.
     */
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null) {
            return new byte[0];
        }
        String clean = hexString.replaceAll("\\s+", "").toUpperCase();
        if (clean.length() % 2 != 0) {
            clean = "0" + clean;
        }
        int len = clean.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(clean.charAt(i), 16);
            int low = Character.digit(clean.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Caractère hexadécimal invalide détecté : " + clean.substring(i, i + 2));
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    /**
     * Effectue l'opération XOR (ou exclusif) bit à bit entre deux tableaux d'octets de même taille.
     */
    public static byte[] xor(byte[] a, byte[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Les tableaux pour le XOR ne peuvent être nuls");
        }
        int len = Math.min(a.length, b.length);
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }
}
