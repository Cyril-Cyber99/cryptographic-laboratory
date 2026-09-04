package attack;

import crypto.AesGcmService;
import crypto.CryptoKeyManager;
import crypto.CryptoResult;
import crypto.NonceManager;
import util.HexUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Démonstration expérimentale de l'altération des données associées non chiffrées (AAD) en mode AES-GCM.
 *
 * Principes théoriques :
 * L'AAD (Additional Authenticated Data) transite en clair (ex: en-têtes réseau, métadonnées de routage).
 * Bien que non chiffrée, elle est absorbée par le polynôme GHASH avant les blocs de ciphertext.
 * Modifier l'AAD sans modifier la clé secrète invalide mathématiquement le tag MAC.
 */
public class GcmAadTamperingDemo {

    public static class AadTamperingResult {
        public final String originalAad;
        public final String tamperedAad;
        public final String plaintext;
        public final byte[] ciphertext;
        public final byte[] originalTag;
        public final boolean rejected;
        public final String status;
        public final String technicalDetails;

        public AadTamperingResult(String originalAad,
                                  String tamperedAad,
                                  String plaintext,
                                  byte[] ciphertext,
                                  byte[] originalTag,
                                  boolean rejected,
                                  String status,
                                  String technicalDetails) {
            this.originalAad = originalAad;
            this.tamperedAad = tamperedAad;
            this.plaintext = plaintext;
            this.ciphertext = ciphertext;
            this.originalTag = originalTag;
            this.rejected = rejected;
            this.status = status;
            this.technicalDetails = technicalDetails;
        }
    }

    public AadTamperingResult executeDemo(String originalAad, String tamperedAad, String message) {
        if (originalAad == null || originalAad.isEmpty()) {
            originalAad = "USER=ADMIN;CLEARANCE=TOP_SECRET";
        }
        if (tamperedAad == null || tamperedAad.isEmpty()) {
            tamperedAad = "USER=GUEST;CLEARANCE=PUBLIC";
        }
        if (message == null || message.isEmpty()) {
            message = "ACCES_AUTORISE_AU_SERVEUR_CENTRAL";
        }

        SecretKey key = CryptoKeyManager.generateKey(128);
        byte[] nonce = NonceManager.generateGcmNonce();
        AesGcmService gcm = new AesGcmService();

        // 1. Chiffrement avec l'AAD d'origine
        byte[] aadBytesOrig = originalAad.getBytes(StandardCharsets.UTF_8);
        byte[] ptBytes = message.getBytes(StandardCharsets.UTF_8);
        CryptoResult enc = gcm.encrypt(ptBytes, key, nonce, aadBytesOrig);

        // 2. Tentative de déchiffrement avec l'AAD falsifiée
        byte[] aadBytesTampered = tamperedAad.getBytes(StandardCharsets.UTF_8);
        CryptoResult dec = gcm.decrypt(enc.getCiphertext(), enc.getTag(), key, nonce, aadBytesTampered);

        boolean rejected = !dec.isSuccess();
        String status = rejected ? "AUTHENTICATION FAILED (REJET GHASH)" : "ACCEPTÉ";

        String details = "EXPÉRIENCE — MODIFICATION DE L'AAD (GCM) :\n" +
                "• AAD Originale (transmise en clair)  : " + originalAad + "\n" +
                "• AAD Altérée par un tiers             : " + tamperedAad + "\n" +
                "• Tag MAC généré à l'émission          : " + HexUtils.bytesToSpacedHex(enc.getTag()) + "\n" +
                "• Déchiffrement avec AAD altérée       : " + status + "\n" +
                "• Diagnostic : " + dec.getErrorMessage() + "\n\n" +
                "CONCLUSION SCIENTIFIQUE :\n" +
                "L'AAD n'est pas chiffrée, mais elle est authentifiée.\n" +
                "Toute tentative d'usurpation de privilèges dans les métadonnées est interceptée avec certitude.";

        return new AadTamperingResult(
                originalAad,
                tamperedAad,
                message,
                enc.getCiphertext(),
                enc.getTag(),
                rejected,
                status,
                details
        );
    }
}
