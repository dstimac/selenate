package net.selenate.client.user;

import static java.util.concurrent.TimeUnit.SECONDS;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.TypedActor;
import akka.actor.TypedProps;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import net.selenate.client.C;
import net.selenate.common.sessions.ISessionFactory;
import net.selenate.common.user.Preferences;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.Properties;

/**
 * TODO: sessionFactory creation with custom configuration
 */
public final class ActorFactory {
  private final C cfg;

  private final ActorSystem system;
  private final ISessionFactory sessionFactory;

  // === CONSTRUCTORS ===

  public ActorFactory() {
    this("client-system", new C());
  }

  public ActorFactory(final Properties props) {
    this("client-system", new C(props));
  }

  public ActorFactory(final String systemName, final C c) {
    this.cfg = c;

    system = ActorSystem.create(
      systemName,
      ConfigFactory.load().withValue(
        "akka.remote.netty.hostname",
        ConfigValueFactory.fromAnyRef(cfg.ClientHost)
      )
    );

    sessionFactory = TypedActor.get(system).typedActorOf(
      new TypedProps<ISessionFactory>(ISessionFactory.class),
      system.actorFor(String.format("akka://server-system@%s:%s/%s", cfg.ServerHost, cfg.ServerPort, cfg.ServerPath))
    );
  }

  // === SESSION CREATION ===

  public ActorRef createSession(final String name, final int timeout) throws IOException {
    try {
      final Future<ActorRef> sessionFuture = sessionFactory.getSession(name);
      final ActorRef result = Await.result(sessionFuture, Duration.create(timeout, SECONDS));
      return result;
    } catch (final Exception e) {
      throw new IOException(String.format("An error occured while trying to get a new session (%s)!", name), e);
    }
  }

  public ActorRef createSession(final String name, final Preferences preferences, final int timeout) throws IOException {
    try {
      final Future<ActorRef> sessionFuture = sessionFactory.getSession(name, preferences);
      final ActorRef result = Await.result(sessionFuture, Duration.create(timeout, SECONDS));
      return result;
    } catch (final Exception e) {
      throw new IOException(String.format("An error occured while trying to get a new session (%s)!", name), e);
    }
  }

  // ---------------------------------------------------------------------------

  public ActorSystem getSystem() {
    return system;
  }

  /**
   * USE: getSystem().shutdown();
   */
  @Deprecated
  public void shutdown() {
    system.shutdown();
  }

  @Deprecated
  public ActorRef getUntyped(final String name, final Class<? extends Actor> clazz) {
    return system.actorOf(new Props(clazz), name);
  }
}