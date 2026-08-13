package fr.izidor.hexalib.infra.applicationCommand;

/**
 * Étape du cycle de vie d'une commande à laquelle une trace d'observabilité est écrite.
 *
 * <p>Chaque commande produit une trace {@link #RECEIVED} puis une trace {@link #COMPLETED},
 * qui sont deux enregistrements distincts et non deux états successifs d'un même
 * enregistrement.
 */
public enum CommandPhase {

    /** Commande reçue, avant toute exécution. Résultat non renseigné. */
    RECEIVED,

    /** Exécution terminée, avec ou sans succès. Résultat renseigné. */
    COMPLETED
}
