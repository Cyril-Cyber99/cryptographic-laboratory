package attack;

import crypto.AesGcmService;
import crypto.CryptoKeyManager;
import crypto.CryptoResult;
import crypto.NonceManager;
import util.HexUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Démonstration expérimentale de l'altération du Tag d'authentification MAC 128 bits en AES-GCM.
 */
public class GcmTagTamperingDemo {

    public static class TagTamperingResult {
        public final byte[] originalTag;
        public final byte[] tamperedTag;
        public final int modifiedByteIndex;
        public final boolean rejected;
        public final String status;
        public final String technicalDetails;

        public TagTamperingResult(byte[] originalTag,
                                  byte[] tamperedTag,
                                  int modifiedByteIndex,
                                  boolean rejected,
                                  String status,
                                  String technicalDetails) {
            this.originalTag = originalTag;
            this.tamperedTag = tamperedTag;
            this.modifiedByteIndex = modifiedByteIndex;
            this.rejected = rejected;
            this.status = status;
            this.technicalDetails = technicalDetails;
        }
    }

    public TagTamperingResult executeDemo(String message, int byteIndexToModify) {
        if (message == null || message.isEmpty()) {
            message = "MESSAGE_SOUVERAIN_PROTEGE_PAR_TAG_MAC_128_BITS";
        }
        byte[] pt = message.getBytes(StandardCharsets.UTF_8);

        SecretKey key = CryptoKeyManager.generateKey(128);
        byte[] nonce = NonceManager.generateGcmNonce();
        AesGcmService gcm = new AesGcmService();

        CryptoResult enc = gcm.encrypt(pt, key, nonce, null);
        byte[] originalTag = enc.getTag();
        byte[] tamperedTag = Arrays.copyOf(originalTag, originalTag.length);

        int idx = (byteIndexToModify >= 0 && byteIndexToModify < tamperedTag.length)
                ? byteIndexToModify
                : 2; // Modification du 3ème octet par défaut

        tamperedTag[idx] ^= (byte) 0x01; // Inversion du bit de poids faible

        // Tentative de déchiffrement avec tag altéré
        CryptoResult dec = gcm.decrypt(enc.getCiphertext(), tamperedTag, key, nonce, null);

        boolean rejected = !dec.isSuccess();
        String status = rejected ? "AUTHENTICATION FAILED (MESSAGE REJETÉ)" : "ACCEPTÉ";

        String details = "EXPÉRIENCE — MODIFICATION DU TAG MAC :\n" +
                "• Tag Original (128 bits) : " + HexUtils.bytesToSpacedHex(originalTag) + "\n" +
                "• Tag Modifié  (128 bits) : " + HexUtils.bytesToSpacedHex(tamperedTag) + "\n" +
                "• Octet modifié           : Indice " + idx + " (différence de 1 bit)\n" +
                "• Résultat Déchiffrement  : " + status + "\n" +
                "• Cause                   : Comparaison constante de tag invalide (AEADBadTagException)\n\n" +
                "CONCLUSION SCIENTIFIQUE :\n" +
                "La probabilité pour un attaquant de forger un tag valide de 128 bits sans la clé est de 2^(-128),\n" +
                "ce qui rend la falsification mathématiquement impossible en pratique.";

        return new TagTamperingResult(
                originalTag,
                tamperedTag,
                idx,
                rejected,
                status,
                details
        );
    }
}
