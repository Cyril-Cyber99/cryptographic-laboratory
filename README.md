# Laboratoire Expérimental d'Analyse Comparative AES-CBC vs AES-GCM
> **Projet de Recherche Académique — Niveau Master**  
> *Environnement expérimental pour l'analyse empirique et formelle des modes de chiffrement par blocs.*

---

## 1. Présentation Générale & Objectifs Scientifiques

Ce logiciel constitue un **laboratoire cryptographique interactif** développé pour mettre expérimentalement et théoriquement en évidence les différences fondamentales entre :
- **AES-CBC** (*Cipher Block Chaining* — NIST SP 800-38A)
- **AES-GCM** (*Galois/Counter Mode* — NIST SP 800-38D — AEAD)

### Principes Méthodologiques Stricts
Le laboratoire respecte la chaîne méthodologique :
$$\text{HYPOTHÈSE} \longrightarrow \text{PROTOCOLE} \longrightarrow \text{EXÉCUTION} \longrightarrow \text{MESURE} \longrightarrow \text{RÉSULTAT} \longrightarrow \text{INTERPRÉTATION} \longrightarrow \text{CONCLUSION}$$

- **Aucune valeur inventée** : Tout débit ou durée affiché comme résultat provient d'une mesure réelle capturée isolément avec `System.nanoTime()`.
- **Distinction formelle** : Les caractéristiques normatives sont identifiées comme **"Information théorique"**, tandis que les mesures sont estampillées **"Résultat expérimental"**.

---

## 2. Contraintes Techniques & Zéro Dépendance

- **Langage** : Java 17 (Java SE standard pur).
- **Interface & Dessin Vectoriel** : Swing, AWT, **Graphics2D** personnalisé (aucun framework, aucun JavaFX, aucun JFreeChart, aucun Plotly).
- **Cryptographie** : `javax.crypto`, `java.security` standards (`Cipher`, `KeyGenerator`, `SecureRandom`, `GCMParameterSpec`, `IvParameterSpec`).
- **I/O & Export** : `java.nio`, `java.io`, export CSV pur standard.

---

## 3. Architecture Modulaire du Projet

```
src/
├── app/
│   └── Main.java                          # Point d'entrée de l'application Swing
├── crypto/
│   ├── CryptoResult.java                  # Modèle immuable des métadonnées cryptographiques
│   ├── CryptoKeyManager.java              # Génération CSPRNG des clés AES (128, 192, 256 bits)
│   ├── NonceManager.java                  # Génération normée IV (128b) et Nonce (96b)
│   ├── AesCbcService.java                 # Service AES/CBC/PKCS5Padding & NoPadding
│   └── AesGcmService.java                 # Service AES/GCM/NoPadding (AEAD avec AAD et Tag 128b)
├── experiment/
│   ├── Experiment.java                    # Contrat d'expérience reproductible
│   ├── ExperimentResult.java              # Fiche expérimentale et sérialisation CSV
│   ├── ExperimentRunner.java              # Registre centralisé thread-safe des résultats
│   ├── CbcExperiment.java                 # Scénario expérimental CBC
│   ├── GcmExperiment.java                 # Scénario expérimental GCM avec AAD
│   ├── AttackExperiment.java              # Évaluation des attaques actives
│   └── PerformanceExperiment.java         # Moteur de benchmark avec warm-up et statistiques
├── attack/
│   ├── CbcBitFlippingDemo.java            # Attaque par retournement de bits sur CBC (malléabilité)
│   ├── GcmCiphertextTamperingDemo.java    # Altération d'octets de ciphertext sous GCM et interception
│   ├── GcmAadTamperingDemo.java           # Altération des données associées (AAD) et échec GHASH
│   ├── GcmTagTamperingDemo.java           # Falsification du tag MAC 128 bits
│   └── GcmNonceReuseDemo.java             # Simulation pédagogique de réutilisation de nonce (Two-Time Pad)
├── visualization/
│   ├── CbcFlowView.java                   # Visualisation vectorielle dynamique Graphics2D du flux CBC
│   ├── GcmFlowView.java                   # Visualisation vectorielle dynamique Graphics2D du flux GCM
│   ├── BlockView.java                     # Rendu de bloc d'octets avec ciblage interactif
│   ├── ComparisonView.java                # Face-à-face architectural synchrone
│   ├── PerformanceChart.java              # Moteur de graphiques vectoriels (débits, temps, courbes)
│   ├── SecurityMatrixView.java            # Matrice visuelle vectorielle de conformité aux critères de sécurité
│   └── ExperimentTimelineView.java        # Progression vectorielle en 13 étapes pour le jury
├── gui/
│   ├── MainFrame.java                     # Fenêtre principale (Header, Sidebar, CardLayout, StatusBar)
│   ├── DashboardPanel.java                # Tableau de bord distinguant Théorie et Mesures réelles
│   ├── CbcPanel.java                      # Console d'expérimentation AES-CBC
│   ├── GcmPanel.java                      # Console d'expérimentation AES-GCM
│   ├── AttackPanel.java                   # Laboratoire d'attaques actives contrôlées
│   ├── PerformancePanel.java              # Console de benchmark et graphiques vectoriels
│   ├── ComparisonPanel.java               # Synthèse comparative exhaustive en 4 axes
│   ├── ExperimentPanel.java               # Mode Recherche et fiches d'expériences reproductibles
│   ├── ResultsPanel.java                  # Archives, export CSV et générateur de rapports académiques
│   └── JuryDemoPanel.java                 # Mode Démonstration guidée en 13 étapes pour soutenance
└── test/
    └── CryptoValidationTest.java          # Suite de validation interne des 7 invariants cryptographiques
```

---

## 4. Guide de Compilation et d'Exécution

Le projet ne nécessite aucun outil de build tiers (Maven ou Gradle ne sont pas requis).

### Avec les scripts Windows (.bat) :
- **Compiler** : Double-cliquer sur `compile.bat`
- **Exécuter** : Double-cliquer sur `run.bat`
- **Valider les tests** : Double-cliquer sur `test.bat`

### Avec PowerShell (.ps1) :
```powershell
.\compile.ps1
.\run.ps1
.\test.ps1
```

### En ligne de commande manuelle :
```bash
# Compilation
javac -encoding UTF-8 -d bin -sourcepath src src/app/Main.java src/test/CryptoValidationTest.java

# Lancement de l'IHM Swing
java -Dfile.encoding=UTF-8 -cp bin app.Main

# Lancement des tests automatisés
java -Dfile.encoding=UTF-8 -cp bin test.CryptoValidationTest
```

---

## 5. Modules Majeurs & Démonstrations

### 5.1 Visualisations Vectorielles Graphics2D
Toutes les visualisations sont redessinées dynamiquement selon les dimensions de la fenêtre (`getWidth()`, `getHeight()`) avec anti-aliasing activé :
- **`CbcFlowView`** : Chaînage par bloc ($P_i \oplus C_{i-1} \to \text{AES}_K \to C_i$) avec contrôleur d'animation pas-à-pas (Précédent, Suivant, Lecture, Pause, Réinitialiser).
- **`GcmFlowView`** : Décomposition du flux de compteur CTR, absorption GHASH de l'AAD et du Ciphertext dans $\text{GF}(2^{128})$, et masquage final du Tag MAC. Inspection au clic sur chaque composant.
- **`PerformanceChart`** : Tracé complet d'axes gradués, grille, barres comparatives CBC vs GCM et légendes pour le débit (Mo/s), temps de chiffrement et temps de déchiffrement.

### 5.2 Expériences d'Attaques Contrôlées
1. **Altération de Ciphertext** : Montre que CBC déchiffre du contenu corrompu sans alerte, tandis que GCM lève immédiatement une `AEADBadTagException` et rejette le message.
2. **Altération AAD** : Démontre la règle scientifique : *"L'AAD n'est pas chiffrée, mais elle est authentifiée."*
3. **Altération de Tag MAC** : Démontre la résistance au forgeage (probabilité de succès : $2^{-128}$).
4. **CBC Bit-Flipping** : Inversion ciblée de bits dans le bloc clair suivant $P[i]$ en modifiant $C[i-1]$.
5. **Réutilisation de Nonce (Two-Time Pad)** : Simulation pédagogique vérifiant $C_1 \oplus C_2 = P_1 \oplus P_2$.

### 5.3 Mode Soutenance Jury (13 Étapes)
Parcours guidé conçu pour une présentation en quelques minutes devant un jury de Master :
1. Présentation AES-CBC
2. Présentation AES-GCM
3. Chiffrement simultané d'un échantillon
4. Visualisation vectorielle CBC
5. Visualisation vectorielle GCM
6. Altération de Ciphertext
7. Vérification d'authenticité GCM
8. Altération de l'AAD
9. Falsification du Tag MAC
10. Démonstration de réutilisation de nonce
11. Benchmark en temps réel
12. Rendu graphique vectoriel des débits
13. Conclusions scientifiques déduites des mesures

---

## 6. Exportation des Résultats (CSV & Fiches)

L'export CSV respecte rigoureusement les colonnes demandées :
`experiment_id,date,algorithm,mode,message_size,key_size,iv_or_nonce_size,aad_size,encryption_time_ns,decryption_time_ns,throughput_mb_s,authentication_status,attack_type,result`
