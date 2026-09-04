package experiment;

/**
 * Contrat pour toute expérience de laboratoire reproductible.
 * Respecte la démarche méthodologique :
 * HYPOTHÈSE -> PROTOCOLE -> EXÉCUTION -> MESURE -> RÉSULTAT -> INTERPRÉTATION -> CONCLUSION
 */
public interface Experiment {

    String getId();

    String getTitle();

    String getObjective();

    String getHypothesis();

    ExperimentResult run();
}
