package abstractsieve;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

public abstract class AbstractSieveElement extends AbstractActor {
  protected ActorRef nextSieveElement;
  protected ActorRef actorCreator;
  protected int currentNumber;
  protected LoggingAdapter log;

  public static class GetNewActorMessage {
    private ActorRef nextActor;
    public GetNewActorMessage(ActorRef nextActor) {
      this.nextActor = nextActor;
    }

    public ActorRef getNextActor() {
      return nextActor;
    }
  }
}
