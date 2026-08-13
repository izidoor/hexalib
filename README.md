# hexalib

Socle Java pour applications en architecture hexagonale, DDD et CQRS.

| Module | Description | Dépendances |
| --- | --- | --- |
| [`hexalib-domain`](hexalib-domain) | Abstractions DDD/CQRS pures | aucune |
| [`hexalib-infra`](hexalib-infra) | Bus de commandes, transactions, événements | Spring, Lombok |

## Prérequis

- JDK 25
- Maven 3.8+

## Build

```bash
mvn clean install
```

Le POM racine `hexalib-parent` agrège les deux modules et centralise versions, BOM Spring Boot et
configuration du compilateur.

## Utilisation

```xml
<dependency>
    <groupId>fr.izidor</groupId>
    <artifactId>hexalib-infra</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>
```

`hexalib-infra` embarque `hexalib-domain` en transitif. Pour un module de domaine isolé de Spring,
ne dépendre que de `hexalib-domain`.

## Licence

Usage interne.
