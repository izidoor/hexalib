package fr.izidor.hexalib.infra.applicationCommand;

/**
 * Port de persistance du journal des commandes.
 *
 * <p>Ce journal relève de l'<strong>observabilité</strong>, pas de la conformité : il sert à
 * comprendre ce que l'application a fait, pas à en apporter la preuve. C'est ce qui justifie son
 * caractère best-effort décrit plus bas.
 *
 * <p>Aucune implémentation n'est fournie : c'est à l'application consommatrice de l'écrire.
 * Le contrat ci-dessous est ce sur quoi {@code LoggingMiddleware} s'appuie.
 *
 * <h2>Journal append-only</h2>
 *
 * <p>{@link #save} est appelé <strong>deux fois par commande</strong> : une fois à la réception
 * ({@link CommandPhase#RECEIVED}), une fois l'exécution terminée ({@link CommandPhase#COMPLETED}).
 * Les deux appels portent le même {@link ApplicationCommand#id()}.
 *
 * <p>Chaque appel doit produire un <strong>nouvel enregistrement</strong>. Une implémentation ne
 * doit jamais remplacer ni mettre à jour une trace existante : l'{@code id} de la commande n'est
 * donc pas une clé primaire utilisable seule — combiner avec {@link ApplicationCommand#phase()},
 * ou utiliser une clé technique.
 *
 * <p>Il en découle qu'aucune idempotence n'est requise, et qu'aucun ordre d'arrivée n'est imposé :
 * une trace {@code COMPLETED} écrite avant sa {@code RECEIVED} reste correcte. Une implémentation
 * peut donc être asynchrone sans précaution d'ordonnancement particulière.
 *
 * <h2>Isolation des pannes</h2>
 *
 * <p>{@code LoggingMiddleware} appelle {@link #save} en dehors de la transaction métier, et le
 * second appel a lieu <strong>après son commit</strong>. Une exception propagée signalerait un échec
 * à l'appelant pour une commande pourtant appliquée, l'exposant à un rejeu. Les {@code RuntimeException}
 * sont donc interceptées et journalisées en {@code ERROR} : le journal est délibérément best-effort.
 *
 * <p>Une implémentation <strong>asynchrone</strong> échappe à ce filet, puisque l'échec survient
 * après le retour de {@link #save}, sur un autre thread. Elle reprend alors à sa charge la
 * journalisation contextualisée des échecs — au minimum {@link ApplicationCommand#commandName()},
 * {@link ApplicationCommand#id()} et {@link ApplicationCommand#userId()}, sans quoi les traces
 * perdues le sont sans moyen de rattrapage.
 */
public interface AppCommandRepository {

    void save(ApplicationCommand applicationCommand);

}
