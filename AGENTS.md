# AGENTS.md

Instructions pour les agents de code travaillant sur ce dépôt. Point d'entrée unique : `CLAUDE.md`
ne fait que renvoyer ici.

## Vue d'ensemble

Monorepo de deux bibliothèques Maven (`fr.izidor`) servant de socle aux projets métier en
architecture hexagonale / DDD / CQRS :

- **`hexalib-domain`** — abstractions pures, **zéro dépendance** (ni Spring, ni Lombok). C'est le
  contrat que le domaine des applications consommatrices implémente.
- **`hexalib-infra`** — implémentation Spring (bus de commandes, transactions, publication
  d'événements, auto-configuration).

Le POM racine `hexalib-parent` (packaging `pom`) agrège les deux modules et centralise **toutes** les
versions. Rien ne doit être versionné dans les modules enfants :

- `<properties>` — `java.version`, `maven.compiler.release`, `lombok.version`, `spring-boot.version` ;
- `<dependencyManagement>` — import du BOM `spring-boot-dependencies`, plus `hexalib-domain`
  (`${project.version}`) et `lombok` ;
- `<pluginManagement>` — `maven-compiler-plugin` avec `<release>`.

Les deux modules sont versionnés en lockstep : ils héritent de la version du parent (`1.1.0-SNAPSHOT`)
et ne déclarent plus ni `<groupId>` ni `<version>`. Une montée de version se fait donc **au seul
endroit** du POM racine (`mvn versions:set -DnewVersion=... -DprocessAllModules`).

## Build

Java 25 est requis (`maven.compiler.release=25`), mais le `java` par défaut du système est en 21.
Il faut donc forcer `JAVA_HOME` :

```bash
export JAVA_HOME=$HOME/.jdks/temurin-25.0.3
```

Le réacteur gère l'ordre (parent → domain → infra), tout se construit depuis la racine :

```bash
mvn clean install              # les deux modules
mvn install -pl hexalib-infra -am   # infra + ses dépendances du réacteur
```

Construire `hexalib-infra` seul (`-pl` sans `-am`, ou `cd hexalib-infra && mvn install`) fonctionne
mais consomme le `hexalib-domain` **du dépôt local `~/.m2`**, pas les sources courantes.

### Tests

Aucun test n'existe à ce jour (`hexalib-domain/src/test/java` est vide, `hexalib-infra` n'a pas de
répertoire de test) et aucune dépendance de test n'est déclarée. Ajouter JUnit 5 au `pom.xml` du
module concerné avant d'écrire le premier test.

```bash
mvn test                          # tous les tests du module courant
mvn test -Dtest=MaClasseTest      # une classe
mvn test -Dtest=MaClasseTest#unCas
```

## Architecture

### Découpage de `hexalib-domain`

Trois domaines fonctionnels — `cqrs`, `ddd`, `hexagone` — chacun subdivisé par nature :
`interfaces/`, `abstractions/`, `annotations/`, `exceptions/`. Toute nouvelle abstraction suit cette
grille, sauf lorsqu'une hiérarchie `sealed` impose de regrouper interface et variantes dans un même
package (`cqrs/commandResult/`).

Points structurants :

- `AggregateRoot<ID>` accumule ses `DomainEvent` non encore publiés, exposés par
  `uncommittedEvents()` — le nom dit l'état : ce qui reste à publier, pas l'historique de l'agrégat.
  La lecture est une copie défensive, la mutation passe par `addEvent`/`resetEvents`.
  `AbstractAggregateRootWithEvents` en fournit l'implémentation.
- L'égalité des entités est définie **une seule fois**, dans `AbstractDDDEntity` : `equals`/`hashCode`
  sur l'`id()` seul, conformément à l'identité DDD, et sous condition de classe strictement identique.
  `AbstractAggregateRootWithEvents` en hérite — la hiérarchie des abstractions suit celle des
  interfaces (`AggregateRoot extends DDDEntity`), de sorte qu'une racine d'agrégat n'a pas sa propre
  notion d'égalité. Un `id()` non affecté rend l'entité égale à elle seule.
- Les événements se déclarent via `DomainEventsFactory.enqueue(descriptor).withPayload(p).on(agg)` —
  le builder attache l'événement à l'agrégat, il ne le retourne pas. `DomainEventBase` dérive `name()`
  du nom de classe simple et délègue `aggregateID()`/`occuredOn()` à `DomainEventMetaData`.
- Les exceptions métier (`AggregatException` et ses sous-classes — orthographe française « Aggregat »,
  volontaire) portent un `CodeException` qui est l'unique canal de traduction erreur métier → statut
  HTTP. Le domaine ne connaît donc pas HTTP, seulement cet enum. Elles se répartissent en **deux
  sous-packages** sous `ddd.exceptions` : `aggregatException/` pour celles qui ne connaissent que la
  classe d'agrégat, `aggregatWithIdException/` pour celles qui portent en plus l'identifiant de
  l'instance fautive (`AggregatWithIdException.aggregateId()`, typé `EntityID<?>`). Placer une
  nouvelle exception dans la branche correspondant à ce qu'elle sait, et non par thème fonctionnel.
  Noter le cycle de packages assumé : `ddd.interfaces` dépend de `aggregatWithIdException` pour
  `getOrThrow`, qui dépend en retour de `ddd.interfaces` pour `EntityID`.
- Le message d'une exception métier se compose en **trois segments** : `AggregatException` pose
  `[NomDeLAgrégat]`, la sous-classe pose son étiquette sémantique (`NotFoundException : `), l'appelant
  fournit la fin. On lit donc `[Customer] NotFoundException : id inconnu : 3f2a…`. L'étiquette est ce
  qui survit au passage en `INTERNAL_ERROR_500` d'un log : le code seul ne dit pas la nature de
  l'erreur, le message si. Toute nouvelle sous-classe préfixe la sienne.
- Le choix du `CodeException` suit la nature de l'erreur, pas la commodité du statut :
  `UNPROCESSABLE_ENTITY_422` pour une **violation d'invariant du domaine** — argument refusé
  (`AggregatIllegalArgumentException`) ou transition d'état interdite (`AggregatStateException`) : la
  requête est bien formée, c'est le métier qui la refuse. `BAD_REQUEST_400` reste à la couche
  transport, qu'aucune exception du domaine n'occupe. Sur l'axe de l'accès, l'enum tranche une
  ambiguïté d'usage courant : `UNAUTHORIZED_401` = non **authentifié** (d'où
  `AggregatUnauthorizedException`), `FORBIDDEN_403` = authentifié mais non **autorisé**.
- `DDDRepository.find(id)` retourne un `Optional<E>` : c'est un port de lecture, l'absence n'y est pas
  une erreur. La levée d'exception est offerte par le `default getOrThrow(id)`, qui s'appuie sur
  `aggregateClass()` — d'où la présence de cet accesseur au contrat du repository. Son paramètre est
  le value object d'identité (`ID extends EntityID<?>`), pas la valeur brute : deux entités bâties sur
  le même type technique ne sont donc pas interchangeables à l'appel.
- `CommandResult` est **scellée** (`permits AggregateRootResult, DDDEntityResult`) et vit dans
  `cqrs/commandResult/` : `sealed` exige que toute la hiérarchie tienne dans un seul package, le
  projet n'ayant pas de `module-info`. C'est la même raison qui regroupe `ExecutionResult` et ses
  deux variantes dans `applicationResult/` côté infra. L'axe de variation est la présence
  d'événements, pas le succès — le domaine n'a pas de variante d'échec.
- **Le transport des événements est porté par la variante, pas par l'interface** : `CommandResult`
  ne déclare que `executedOn()` et `entity()`. Seul `AggregateRootResult` porte un composant
  `uncommittedEvents`, capturé sur l'agrégat par la fabrique `of(...)` : un **instantané** figé au
  retour du handler. `DDDEntityResult` n'a aucun composant d'événements, et son constructeur canonique
  **refuse** une racine d'agrégat — Java ne sachant pas exprimer « `DDDEntity` mais pas
  `AggregateRoot` », c'est la seule barrière possible contre une perte silencieuse.
- Côté infra, `Dispatcher` choisit par `switch` sur la variante de `CommandResult` laquelle des deux
  fabriques `SuccessResult.of(...)` appeler : celle sans événements, ou celle qui relaie
  `uncommittedEvents`.

### Annotations maison, pas de stéréotypes Spring

`@CommandHandlerComponent`, `@DomainEventListenerComponent`, `@DomainEventPublisherComponent`,
`@DomainService`, `@Stub` sont définies dans `hexalib-domain` et sont **vides** : elles n'héritent pas
de `@Component`. C'est ce qui garde le domaine libre de Spring. Le suffixe `Component` des trois
premières les distingue des interfaces homonymes de `cqrs.interfaces` / `ddd.interfaces` qu'elles
marquent ; `@DomainService` et `@Stub` n'ont pas de contrepartie et n'en portent pas.

Côté infra, `DomainScanRegistrar` (un `ImportBeanDefinitionRegistrar`) scanne le package indiqué et
transforme ces annotations en `BeanDefinition`. Conséquence : **une classe de domaine n'est un bean
que si elle est annotée ET située sous `domainBasePackage`**.

Les classes `@Stub` ne sont enregistrées que si la propriété
`fr.izidor.hexalib.infra.stubs.enabled=true` — mécanisme prévu pour substituer les adaptateurs
sortants en test / dev.

### Point d'entrée de l'intégration

```java
@EnableCqrsInfra(domainBasePackage = "com.exemple.monapp.domain")
```

Cette annotation importe `CqrsInfraConfiguration` (qui `@ComponentScan` le package `commandBus`) et
`DomainScanRegistrar`. L'application consommatrice doit fournir :

- un `PlatformTransactionManager` (Spring Boot le fournit dès qu'une source de données est présente) ;
- une implémentation de `AppCommandRepository` (journal d'observabilité des commandes) — aucune
  implémentation par défaut n'est livrée.

### Bus de commandes et journal

Ces deux sujets, qui concentrent l'essentiel de la logique d'infra, ont leur documentation dédiée :
[`docs/commandBus.md`](docs/commandBus.md) et [`docs/commandLogs.md`](docs/commandLogs.md).

## Docs

Le répertoire [`docs/`](docs/) contient la documentation de conception, hors README. Ces fichiers
portent les *raisons* des choix, pas seulement leur description : les consulter avant de modifier le
code concerné.

| Fichier | Contenu |
| --- | --- |
| [`docs/commandBus.md`](docs/commandBus.md) | Chaîne de middlewares du `CommandBus` et ordre figé dans son constructeur, avec les conséquences de cet ordre (publication des événements après commit, conversion des exceptions par `UnitOfWorkMiddleware`) ; résolution des handlers et listeners par égalité stricte de classe ; modèle de résultat à deux niveaux (`CommandResult` du domaine, `ExecutionResult` scellé de l'infra). |
| [`docs/commandLogs.md`](docs/commandLogs.md) | Journal des commandes écrit par `LoggingMiddleware` via `AppCommandRepository` : positionnement en observabilité et non en conformité, cycle `INIT` → `withResult`, modèle append-only discriminé par `CommandPhase`, sémantique de `completedOn`, et isolation des pannes par `saveQuietly`. |

### Tenir la doc à jour

**Toute modification du code doit être répercutée dans la documentation correspondante, dans le même
changement.** Concrètement, selon ce qui est touché :

- comportement du bus, des middlewares ou du modèle de résultat → `docs/commandBus.md` ;
- journal des commandes, `AppCommand`, `AppCommandRepository` → `docs/commandLogs.md` ;
- API publique d'un module → le `README.md` du module concerné, et sa javadoc ;
- build, structure des POMs, conventions transverses → ce fichier (`AGENTS.md`).

Quand une modification invalide une affirmation existante, la corriger plutôt que d'en ajouter une à
côté.

**En cas de nouvelle fonctionnalité qui n'entre dans aucun fichier existant, demander à l'utilisateur
s'il faut créer un nouveau `docs/<sujet>.md`** — ne pas le créer d'office, et ne pas non plus la
diluer dans un fichier dont ce n'est pas le sujet. Si un nouveau fichier est créé, ajouter sa ligne
au tableau ci-dessus.

### Style d'écriture

**La doc décrit le contrat actuel, et rien d'autre.** Elle s'écrit comme si le code avait toujours
été dans son état présent :

- **aucune trace des états antérieurs** — pas de « désormais », « ne … plus », « anciennement »,
  « remplace X », ni de comparaison avec une API disparue. Un lecteur qui découvre le dépôt n'a pas
  connaissance du passé, et l'historique est dans git ;
- **aucune trace des correctifs** — un bug corrigé ne laisse rien derrière lui : ni description du
  symptôme, ni mise en garde du type « ne pas réintroduire », ni justification défensive du code qui
  le remplace ;
- **ne pas justifier le code existant** ligne à ligne. On documente les **points d'architecture** :
  contrats, invariants, frontières entre modules, et les *raisons* d'un choix de conception qui ne se
  lisent pas dans le code (ordre figé des middlewares, scellement d'une hiérarchie, sens d'un nom).
  Le reste appartient au code et à sa javadoc ;
- **plus aucune référence à du code qui n'existe plus** : à chaque modification, vérifier que les
  types et méthodes cités entre backticks existent encore.

Ces règles valent aussi pour la javadoc.

## Conventions

- **Accesseurs fluides partout** : records Java, ou Lombok `@Accessors(fluent = true)` côté infra.
  Jamais de préfixe `get`. Les interfaces du domaine sont écrites dans ce style (`id()`, `payload()`).
- **Lombok uniquement dans `hexalib-infra`** (`@Slf4j`, `@RequiredArgsConstructor`, `@Builder`,
  `@Getter`). Le domaine reste en Java pur — ne pas y introduire Lombok. C'est pourquoi
  l'`annotationProcessorPaths` de `maven-compiler-plugin` est déclaré dans le POM d'infra et **non**
  dans le `pluginManagement` du parent : le remonter appliquerait le processeur à `hexalib-domain`.
- Fabriques statiques `of(...)` plutôt que constructeurs publics sur les records.
- Jackson 3 (`tools.jackson.core`), pas `com.fasterxml.jackson`.
- **Trois niveaux d'entité, opt-in** : `BaseEntity<ID>` (identité seule, `ID` non contraint) →
  `DDDEntity<ID extends EntityID<?>>` (autonome, cible d'un `DDDRepository`) → `AggregateRoot<ID>`
  (+ entités filles + événements). On ne paie que le niveau déclaré : un référentiel CRUD est un
  simple `record` implémentant `DDDEntity`, sans héritage ni événements. Réserve de vocabulaire
  assumée : une entité interne est une entité au sens DDD sans être une `DDDEntity` — le nom retient
  l'appariement avec `DDDRepository`, le sens large est porté par `BaseEntity`.
- **L'identité d'une entité autonome est un value object** : l'application choisit le type brut
  (`UUID`, `Long`…) mais doit l'emballer dans un `EntityID<T>` — un `record`, pour l'`equals`/
  `hashCode` dont dépendent les clés de `RepoInMemory`. L'accesseur du wrapper s'appelle `value()`
  et non `id()`, sans quoi les appels deviennent `customer.id().id()`.
