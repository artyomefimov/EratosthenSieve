import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public abstract class AbstractSieveElement extends AbstractActor {
  protected ActorRef nextSieveElement;
  protected ActorCreator actorCreator;
  protected int currentNumber;

  public static class GetNewActorMessage {
    private ActorRef nextActor;
    public GetNewActorMessage(ActorRef nextActor) {
      this.nextActor = nextActor;
    }
  }
}
