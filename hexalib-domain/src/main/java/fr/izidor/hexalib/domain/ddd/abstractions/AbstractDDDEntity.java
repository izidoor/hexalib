package fr.izidor.hexalib.domain.ddd.abstractions;

import fr.izidor.hexalib.domain.ddd.interfaces.DDDEntity;
import fr.izidor.hexalib.domain.ddd.interfaces.EntityID;

/**
 * Socle d'égalité des entités : {@code equals}/{@code hashCode} sur l'{@code id()} seul,
 * conformément à l'identité DDD — deux instances portant le même identifiant sont la même entité,
 * quel que soit l'état de leurs attributs.
 *
 * <p>La comparaison exige en outre une <strong>classe strictement identique</strong> : deux entités
 * bâties sur le même type d'identité mais de classes différentes ne sont jamais égales.
 *
 * <p>Une entité dont l'{@code id()} est {@code null} n'est égale qu'à elle-même : tant que
 * l'identité n'est pas affectée, il n'y a rien sur quoi comparer.
 */
public abstract class AbstractDDDEntity<ID extends EntityID<?>> implements DDDEntity<ID> {


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDDDEntity<?> that = (AbstractDDDEntity<?>) o;
        return id() != null && id().equals(that.id());
    }

    @Override
    public int hashCode() {
        return id() == null ? 0 : id().hashCode();
    }
}
