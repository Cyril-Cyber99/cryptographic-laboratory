package experiment;

import attack.*;

/**
 * Encapsule l'exécution contrôlée d'une expérience d'attaque pour la traçabilité scientifique.
 */
public class AttackExperiment implements Experiment {

    public enum Type {
        CBC_BIT_FLIPPING,
        GCM_CIPHERTEXT_TAMPERING,
        GCM_AAD_TAMPERING,
        GCM_TAG_TAMPERING,
        GCM_NONCE_REUSE
    }

    private final String id;
    private final Type attackType;
    private final String customPayload;

    public AttackExperiment(String id, Type attackType, String customPayload) {
        this.id = id;
        this.attackType = attackType;
        this.customPayload = customPayload;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return "Expérience d'altération active : " + attackType.name();
    }

    @Override
    public String getObjective() {
        return "Évaluer la réaction du mode cryptographique face à une altération active des données ou des paramètres.";
    }

    @Override
    public String getHypothesis() {
        switch (attackType) {
            case CBC_BIT_FLIPPING:
                return "CBC sans MAC ne détecte pas le bit-flipping et propage la modification dans le bloc déchiffré suivant.";
            case GCM_CIPHERTEXT_TAMPERING:
                return "GCM intercepte immédiatement toute modification d'octet de ciphertext et rejette le message.";
            case GCM_AAD_TAMPERING:
                return "L'AAD n'est pas chiffrée mais son altération provoque l'échec de vérification du tag GHASH.";
            case GCM_TAG_TAMPERING:
                return "Un tag falsifié est rejeté à la détection par comparaison en temps constant.";
            case GCM_NONCE_REUSE:
                return "La réutilisation d'un nonce produit des keystreams identiques annihilant le secret (C1 ^ C2 = P1 ^ P2).";
            default:
                return "Test d'intégrité";
        }
    }

    @Override
    public ExperimentResult run() {
        switch (attackType) {
            case CBC_BIT_FLIPPING: {
                CbcBitFlippingDemo demo = new CbcBitFlippingDemo();
                CbcBitFlippingDemo.BitFlippingResult r = demo.executeDemo(customPayload);
                return new ExperimentResult(
                        id, "AES", "CBC",
                        r.originalCiphertext != null ? r.originalCiphertext.length : 32,
                        128, 16, 0, 0, 0, 0,
                        "NOT_APPLICABLE", "BIT_FLIPPING", "UNDETECTED (FALSIFICATION RÉUSSIE)",
                        getTitle(), getObjective(), getHypothesis(),
                        "L'altération de C[0] a corrompu P[0] et inversé le bit ciblé dans P[1] sans qu'aucune exception ne soit levée.",
                        "AES-CBC ne fournit pas d'authentification native. L'intégrité n'est pas garantie."
                );
            }
            case GCM_CIPHERTEXT_TAMPERING: {
                GcmCiphertextTamperingDemo demo = new GcmCiphertextTamperingDemo();
                GcmCiphertextTamperingDemo.TamperingResult r = demo.executeDemo(customPayload, 4);
                return new ExperimentResult(
                        id, "AES", "GCM",
                        r.originalCiphertext.length, 128, 12, 0, 0, 0, 0,
                        "REJECTED", "CIPHERTEXT_TAMPERING", r.rejected ? "DETECTED & REJECTED" : "ACCEPTED",
                        getTitle(), getObjective(), getHypothesis(),
                        "Le tag GHASH calculé au déchiffrement ne correspondait pas au tag émis.",
                        "GCM protège l'intégrité du ciphertext et rejette catégoriquement le message altéré."
                );
            }
            case GCM_AAD_TAMPERING: {
                GcmAadTamperingDemo demo = new GcmAadTamperingDemo();
                GcmAadTamperingDemo.AadTamperingResult r = demo.executeDemo("USER=ADMIN", "USER=GUEST", customPayload);
                return new ExperimentResult(
                        id, "AES", "GCM",
                        r.ciphertext.length, 128, 12, r.originalAad.length(), 0, 0, 0,
                        "REJECTED", "AAD_TAMPERING", r.rejected ? "DETECTED & REJECTED" : "ACCEPTED",
                        getTitle(), getObjective(), getHypothesis(),
                        "La modification des métadonnées d'en-tête en clair (AAD) a rendu le tag MAC invalide.",
                        "L'AAD n'est pas chiffrée, mais elle est authentifiée."
                );
            }
            case GCM_TAG_TAMPERING: {
                GcmTagTamperingDemo demo = new GcmTagTamperingDemo();
                GcmTagTamperingDemo.TagTamperingResult r = demo.executeDemo(customPayload, 2);
                return new ExperimentResult(
                        id, "AES", "GCM",
                        32, 128, 12, 0, 0, 0, 0,
                        "REJECTED", "TAG_TAMPERING", r.rejected ? "DETECTED & REJECTED" : "ACCEPTED",
                        getTitle(), getObjective(), getHypothesis(),
                        "L'inversion d'un seul bit dans le tag MAC de 128 bits a entraîné le rejet immédiat.",
                        "La falsification d'un tag de 128 bits est mathématiquement infaisable sans la clé secrète."
                );
            }
            case GCM_NONCE_REUSE: {
                GcmNonceReuseDemo demo = new GcmNonceReuseDemo();
                GcmNonceReuseDemo.NonceReuseResult r = demo.executeDemo(null, null);
                return new ExperimentResult(
                        id, "AES", "GCM",
                        r.ciphertext1.length, 128, 12, 0, 0, 0, 0,
                        "COMPROMISED", "NONCE_REUSE", r.xorMatches ? "VULNERABILITY_CONFIRMED" : "SECURE",
                        getTitle(), getObjective(), getHypothesis(),
                        "La réutilisation de la même paire (Clé, Nonce) a produit un masque de flux (keystream) identique.",
                        "L'attaquant accède directement à P1 XOR P2. L'unicité absolue du Nonce sous GCM est vitale."
                );
            }
            default:
                throw new IllegalArgumentException("Type d'attaque inconnu");
        }
    }
}
