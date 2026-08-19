package fr.izidor.hexalib.domain.ddd.interfaces;

/**
 * Value object enveloppant l'identifiant technique d'un agrégat.
 *
 * <p>Les implémentations doivent définir {@code equals}/{@code hashCode} sur la valeur enveloppée —
 * un {@code record} suffit. Les identifiants servent de clé dans les repositories.
 *
 * @param <T> type de la valeur enveloppée (UUID, Long, String…)
 */
public interface EntityID<T> {

    T value();
}
