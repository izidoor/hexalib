package fr.izidor.hexalib.domain.ddd.interfaces;

/**
 * Entité autonome : elle constitue sa propre unité de cohérence, et c'est à ce titre la seule
 * chose qu'un {@link DDDRepository} charge et sauve. Son identité est obligatoirement un
 * {@link EntityID}.
 *
 * <p>Un référentiel CRUD s'arrête ici. {@link AggregateRoot} y ajoute la gouvernance d'entités
 * filles et l'accumulation d'événements de domaine.
 *
 * <p>Le nom retient volontairement l'appariement avec {@code DDDRepository} : au sens strict du
 * vocabulaire DDD, une entité interne à un agrégat est elle aussi une entité — mais elle relève
 * ici de {@link BaseEntity}.
 */
public interface DDDEntity<ID extends EntityID<?>> extends BaseEntity<ID> {

}
