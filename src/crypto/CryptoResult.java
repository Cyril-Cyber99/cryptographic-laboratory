package crypto;

import util.FormatUtils;
import util.HexUtils;

import java.util.Arrays;

/**
 * Encapsulation immuable d'un résultat d'opération cryptographique (chiffrement/déchiffrement).
 * Contient l'ensemble des métadonnées requises pour l'audit et l'analyse comparée.
 */
public class CryptoResult {

    private final String mode;
    private final byte[] plaintext;
    private final byte[] ciphertext;
    private final byte[] key;
    private final byte[] ivOrNonce;
    private final byte[] aad;
    private final byte[] tag;
    private final long encryptionTimeNs;
    private final long decryptionTimeNs;
    private final boolean authenticated;
    private final boolean success;
    private final String errorMessage;

    public CryptoResult(String mode,
                        byte[] plaintext,
                        byte[] ciphertext,
                        byte[] key,
                        byte[] ivOrNonce,
                        byte[] aad,
                        byte[] tag,
                        long encryptionTimeNs,
                        long decryptionTimeNs,
                        boolean authenticated,
                        boolean success,
                        String errorMessage) {
        this.mode = mode;
        this.plaintext = plaintext != null ? Arrays.copyOf(plaintext, plaintext.length) : null;
        this.ciphertext = ciphertext != null ? Arrays.copyOf(ciphertext, ciphertext.length) : null;
        this.key = key != null ? Arrays.copyOf(key, key.length) : null;
        this.ivOrNonce = ivOrNonce != null ? Arrays.copyOf(ivOrNonce, ivOrNonce.length) : null;
        this.aad = aad != null ? Arrays.copyOf(aad, aad.length) : null;
        this.tag = tag != null ? Arrays.copyOf(tag, tag.length) : null;
        this.encryptionTimeNs = encryptionTimeNs;
        this.decryptionTimeNs = decryptionTimeNs;
        this.authenticated = authenticated;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public String getMode() {
        return mode;
    }

    public byte[] getPlaintext() {
        return plaintext != null ? Arrays.copyOf(plaintext, plaintext.length) : null;
    }

    public byte[] getCiphertext() {
        return ciphertext != null ? Arrays.copyOf(ciphertext, ciphertext.length) : null;
    }

    public byte[] getKey() {
        return key != null ? Arrays.copyOf(key, key.length) : null;
    }

    public byte[] getIvOrNonce() {
        return ivOrNonce != null ? Arrays.copyOf(ivOrNonce, ivOrNonce.length) : null;
    }

    public byte[] getAad() {
        return aad != null ? Arrays.copyOf(aad, aad.length) : null;
    }

    public byte[] getTag() {
        return tag != null ? Arrays.copyOf(tag, tag.length) : null;
    }

    public long getEncryptionTimeNs() {
        return encryptionTimeNs;
    }

    public long getDecryptionTimeNs() {
        return decryptionTimeNs;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFormattedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RÉSULTAT CRYPTOGRAPHIQUE [AES-").append(mode).append("] ===\n");
        sb.append("Statut               : ").append(success ? "SUCCÈS" : "ÉCHEC (" + errorMessage + ")").append("\n");
        sb.append("Authentifié          : ").append(authenticated ? "OUI" : "NON (Non applicable ou vérification échouée)").append("\n");
        sb.append("Taille Plaintext     : ").append(plaintext != null ? plaintext.length + " octets" : "N/A").append("\n");
        sb.append("Taille Ciphertext    : ").append(ciphertext != null ? ciphertext.length + " octets" : "N/A").append("\n");
        if (ivOrNonce != null) {
            sb.append("IV / Nonce (").append(ivOrNonce.length * 8).append(" bits)  : ").append(HexUtils.bytesToSpacedHex(ivOrNonce)).append("\n");
        }
        if (aad != null && aad.length > 0) {
            sb.append("AAD (").append(aad.length).append(" octets)        : ").append(new String(aad)).append(" [").append(HexUtils.bytesToSpacedHex(aad)).append("]\n");
        }
        if (tag != null) {
            sb.append("Tag MAC (").append(tag.length * 8).append(" bits)   : ").append(HexUtils.bytesToSpacedHex(tag)).append("\n");
        }
        sb.append("Temps Chiffrement    : ").append(FormatUtils.formatDurationNs(encryptionTimeNs)).append("\n");
        if (decryptionTimeNs > 0) {
            sb.append("Temps Déchiffrement  : ").append(FormatUtils.formatDurationNs(decryptionTimeNs)).append("\n");
        }
        return sb.toString();
    }
}
