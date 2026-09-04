package attack;

import crypto.AesGcmService;
import crypto.CryptoKeyManager;
import crypto.CryptoResult;
import crypto.NonceManager;
import util.HexUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Démonstration pédagogique de la vulnérabilité critique de réutilisation du Nonce en mode AES-GCM (Two-Time Pad).
 *
 * AVERTISSEMENTS FORMELS :
 * - "SIMULATION PÉDAGOGIQUE"
 * - "NE PAS REPRODUIRE EN PRODUCTION"
 * - Conçu strictement pour illustrer le risque mathématique d'un nonce réutilisé sous un chiffrement de flux (CTR).
 *
 * Explication mathématique :
 * En mode GCM, le chiffrement du texte est assuré par le mode CTR (Counter) :
 * C1 = P1 XOR Keystream
 * C2 = P2 XOR Keystream
 * Si la même clé et le même Nonce sont réutilisés, le Keystream généré est IDENTIQUE.
 * Par conséquent :
 * C1 XOR C2 = (P1 XOR Keystream) XOR (P2 XOR Keystream) = P1 XOR P2.
 * L'attaquant élimine complètement le chiffrement AES et obtient le XOR direct des deux clairs !
 * De plus, Joux (2006) a démontré que la réutilisation du nonce permet de retrouver la clé d'authentification GHASH H.
 */
public class GcmNonceReuseDemo {

    public static class NonceReuseResult {
        public final String plaintext1;
        public final String plaintext2;
        public final byte[] nonce;
        public final byte[] ciphertext1;
        public final byte[] ciphertext2;
        public final byte[] xorCiphertexts;
        public final byte[] xorPlaintexts;
        public final boolean xorMatches;
        public final String mathematicalExplanation;

        public NonceReuseResult(String plaintext1,
                                String plaintext2,
                                byte[] nonce,
                                byte[] ciphertext1,
                                byte[] ciphertext2,
                                byte[] xorCiphertexts,
                                byte[] xorPlaintexts,
                                boolean xorMatches,
                                String mathematicalExplanation) {
            this.plaintext1 = plaintext1;
            this.plaintext2 = plaintext2;
            this.nonce = nonce;
            this.ciphertext1 = ciphertext1;
            this.ciphertext2 = ciphertext2;
            this.xorCiphertexts = xorCiphertexts;
            this.xorPlaintexts = xorPlaintexts;
            this.xorMatches = xorMatches;
            this.mathematicalExplanation = mathematicalExplanation;
        }
    }

    public NonceReuseResult executeDemo(String p1, String p2) {
        if (p1 == null || p1.isEmpty()) {
            p1 = "CONFIDENTIEL: BUDGET_DEFENSE_2026=1500000000";
        }
        if (p2 == null || p2.isEmpty()) {
            p2 = "TOP_SECRET: CODES_MISSILE_ZONE_ALPHA=9876543";
        }

        // Calibrer sur la même longueur pour la démonstration du XOR direct
        int maxLen = Math.max(p1.length(), p2.length());
        p1 = String.format("%-" + maxLen + "s", p1);
        p2 = String.format("%-" + maxLen + "s", p2);

        byte[] p1Bytes = p1.getBytes(StandardCharsets.UTF_8);
        byte[] p2Bytes = p2.getBytes(StandardCharsets.UTF_8);

        // Clé unique et Nonce FIXE délibérément réutilisé (anti-pattern critique)
        SecretKey sharedKey = CryptoKeyManager.generateKey(128);
        byte[] reusedNonce = NonceManager.generateGcmNonce();
        AesGcmService gcm = new AesGcmService();

        // Chiffrement 1
        CryptoResult enc1 = gcm.encrypt(p1Bytes, sharedKey, reusedNonce, null);
        // Chiffrement 2 avec le MÊME nonce et la MÊME clé
        CryptoResult enc2 = gcm.encrypt(p2Bytes, sharedKey, reusedNonce, null);

        byte[] c1 = enc1.getCiphertext();
        byte[] c2 = enc2.getCiphertext();

        // Calculs XOR
        byte[] xorC = HexUtils.xor(c1, c2);
        byte[] xorP = HexUtils.xor(p1Bytes, p2Bytes);

        boolean matches = java.util.Arrays.equals(xorC, xorP);

        String explanation = "SIMULATION PÉDAGOGIQUE — RISQUE DE RÉUTILISATION DE NONCE EN AES-GCM :\n" +
                "====================================================================\n" +
                "ATTENTION : NE PAS REPRODUIRE EN PRODUCTION !\n\n" +
                "Démonstration de la relation mathématique :\n" +
                "P1 : \"" + p1.trim() + "\"\n" +
                "P2 : \"" + p2.trim() + "\"\n\n" +
                "Flux de chiffrement :\n" +
                "P1 ── XOR ── Keystream ──> C1 : " + HexUtils.bytesToSpacedHex(c1) + "\n" +
                "P2 ── XOR ── Keystream ──> C2 : " + HexUtils.bytesToSpacedHex(c2) + "\n\n" +
                "Calcul de l'interception :\n" +
                "C1 XOR C2 = " + HexUtils.bytesToSpacedHex(xorC) + "\n" +
                "P1 XOR P2 = " + HexUtils.bytesToSpacedHex(xorP) + "\n\n" +
                "Égalité stricte vérifiée : " + (matches ? "OUI (C1 XOR C2 == P1 XOR P2)" : "NON") + "\n\n" +
                "CONSÉQUENCE DE SÉCURITÉ :\n" +
                "1. La confidentialité est instantanément perdue par analyse de fréquence ou criblage sur P1 XOR P2.\n" +
                "2. La clé d'authentification GHASH (H) peut être résolue par factorisation de polynôme dans GF(2^128),\n" +
                "   permettant à l'attaquant de forger de faux messages authentifiés.";

        return new NonceReuseResult(
                p1,
                p2,
                reusedNonce,
                c1,
                c2,
                xorC,
                xorP,
                matches,
                explanation
        );
    }
}
