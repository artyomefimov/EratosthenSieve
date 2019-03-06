import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ActorCreator extends AbstractActor {
  private ActorSystem actorSystem;

  public static Props props(ActorSystem actorSystem) {
    return Props.create(ActorCreator.class, () -> new ActorCreator(actorSystem));
  }

  public ActorCreator (ActorSystem actorSystem) {
    this.actorSystem = actorSystem;
  }

  public static class CreateNewActorMessage {
    private int sieveElementNumber;

    public CreateNewActorMessage(int sieveElementNumber) {
      this.sieveElementNumber = sieveElementNumber;
    }
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(CreateNewActorMessage.class, createNewActor -> {
          String newActorName = "SieveElementOf" + createNewActor.sieveElementNumber;
          ActorRef newSieveElement = actorSystem.actorOf(SieveElement.props(ActorCreator.this), newActorName);

          getSender().tell(new AbstractSieveElement.GetNewActorMessage(newSieveElement), ActorRef.noSender());
        })
        .build();
  }
}
