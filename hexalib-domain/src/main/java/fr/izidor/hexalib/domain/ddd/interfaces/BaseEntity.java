package fr.izidor.hexalib.domain.ddd.interfaces;

/**
 * Tout ce qui possède une identité. Le type de l'identifiant n'est pas contraint : une entité
 * interne à un agrégat peut se contenter d'une identité locale ({@code BaseEntity<Integer>}).
 *
 * <p>Une entité qui s'arrête ici n'est pas éligible à un {@link DDDRepository} — c'est
 * {@link DDDEntity} qui ouvre ce droit.
 */
public interface BaseEntity<ID> {

    ID id();

}
