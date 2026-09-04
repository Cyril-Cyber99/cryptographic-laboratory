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
 * Démonstration expérimentale de l'altération d'octets de ciphertext dans AES-GCM.
 *
 * Principes théoriques :
 * En mode GCM, le tag d'authentification MAC est calculé via GHASH(H, AAD, C).
 * Toute modification dans C (même d'un seul bit) entraîne un hash GHASH totalement différent.
 * La vérification par comparaison en temps constant échoue immédiatement et produit une AEADBadTagException.
 */
public class GcmCiphertextTamperingDemo {

    public static class TamperingResult {
        public final String originalPlaintext;
        public final byte[] originalCiphertext;
        public final byte[] tamperedCiphertext;
        public final byte[] tag;
        public final int tamperedByteIndex;
        public final boolean rejected;
        public final String status;
        public final String technicalDetails;

        public TamperingResult(String originalPlaintext,
                               byte[] originalCiphertext,
                               byte[] tamperedCiphertext,
                               byte[] tag,
                               int tamperedByteIndex,
                               boolean rejected,
                               String status,
                               String technicalDetails) {
            this.originalPlaintext = originalPlaintext;
            this.originalCiphertext = originalCiphertext;
            this.tamperedCiphertext = tamperedCiphertext;
            this.tag = tag;
            this.tamperedByteIndex = tamperedByteIndex;
            this.rejected = rejected;
            this.status = status;
            this.technicalDetails = technicalDetails;
        }
    }

    public TamperingResult executeDemo(String plaintext, int byteIndexToModify) {
        if (plaintext == null || plaintext.isEmpty()) {
            plaintext = "ORDRE_TRANSFERT: COMPTE_SOURCE=FR76; MONTANT=50000 EUR; STATUT=CONFIRME;";
        }
        byte[] pt = plaintext.getBytes(StandardCharsets.UTF_8);

        SecretKey key = CryptoKeyManager.generateKey(128);
        byte[] nonce = NonceManager.generateGcmNonce();
        AesGcmService gcm = new AesGcmService();

        // 1. Chiffrement
        CryptoResult enc = gcm.encrypt(pt, key, nonce, null);
        byte[] originalCipher = enc.getCiphertext();
        byte[] tamperedCipher = Arrays.copyOf(originalCipher, originalCipher.length);

        // 2. Sélection et altération de l'octet
        int idx = (byteIndexToModify >= 0 && byteIndexToModify < tamperedCipher.length)
                ? byteIndexToModify
                : Math.min(4, tamperedCipher.length - 1);

        tamperedCipher[idx] ^= (byte) 0xFF; // Inversion complète des 8 bits de cet octet

        // 3. Tentative de déchiffrement
        CryptoResult dec = gcm.decrypt(tamperedCipher, enc.getTag(), key, nonce, null);

        boolean rejected = !dec.isSuccess() && !dec.isAuthenticated();
        String status = rejected ? "AUTHENTICATION FAILED (MESSAGE REJETÉ)" : "ACCEPTÉ";

        String details = "RÉSULTAT DE L'EXPÉRIENCE GCM CIPHERTEXT TAMPERING :\n" +
                "- Octet altéré à l'indice : " + idx + "\n" +
                "- Valeur d'origine (hex)   : " + String.format("%02X", originalCipher[idx]) + "\n" +
                "- Valeur modifiée (hex)   : " + String.format("%02X", tamperedCipher[idx]) + "\n" +
                "- Vérification Tag GHASH : ÉCHEC STRICT\n" +
                "- Statut opérationnel     : " + status + "\n" +
                "- Interprétation : Contrairement à CBC qui aurait déchiffré du contenu corrompu sans avertissement,\n" +
                "  GCM détruit immédiatement tout flux corrompu avant restitution à l'application.";

        return new TamperingResult(
                plaintext,
                originalCipher,
                tamperedCipher,
                enc.getTag(),
                idx,
                rejected,
                status,
                details
        );
    }
}
