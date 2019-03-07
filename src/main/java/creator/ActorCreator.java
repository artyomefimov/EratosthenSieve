package creator;

import abstractsieve.AbstractSieveElement.GetNewActorMessage;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.ArrayList;
import java.util.List;
import sieveelement.SieveElement;
import sieveelement.SieveElement.GeneratedMaxActorsMessage;

public class ActorCreator extends AbstractActor {
  private ActorSystem actorSystem;
  private ActorRef generator;
  private LoggingAdapter log;
  private int generatedActors;
  private int maxActors;
  private List<String> generatedActorsNames;

  public static Props props(ActorSystem actorSystem, int maxActors) {
    return Props.create(ActorCreator.class, () -> new ActorCreator(actorSystem, maxActors));
  }

  public ActorCreator (ActorSystem actorSystem, int maxActors) {
    this.actorSystem = actorSystem;
    log = Logging.getLogger(getContext().getSystem(), this);
    generatedActors = 0;
    this.maxActors = maxActors;
    generatedActorsNames = new ArrayList<>();
  }

  public static class SetGeneratorMessage {
    private ActorRef generator;

    public SetGeneratorMessage(ActorRef generator) {
      this.generator = generator;
    }
  }

  public static class CreateNewActorMessage {
    private int numberForNewActor;

    public CreateNewActorMessage(int numberForNewActor) {
      this.numberForNewActor = numberForNewActor;
    }
  }

  public static class EndGenerationMessage {
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(EndGenerationMessage.class, endGeneration ->
            log.info("Creator has created " + generatedActors + " actors")
        )
        .match(CreateNewActorMessage.class, createNewActor -> {
          generatedActors++;
          if (generatedActors <= maxActors) {
            String newActorName = "HandlerOfNumber" + createNewActor.numberForNewActor;
            generatedActorsNames.add(newActorName);
            ActorRef newSieveElement = actorSystem
                .actorOf(SieveElement.props(getSelf(), createNewActor.numberForNewActor, generator), newActorName);
            log.info("created actor: " + newActorName);

            getSender().tell(
                new GetNewActorMessage(newSieveElement),
                ActorRef.noSender());
          } else {
            log.info("Work has finished. Creator has created all possible actors: " + maxActors + ". Actors names: " + generatedActorsNames);
            getSender().tell(new GeneratedMaxActorsMessage(), ActorRef.noSender());
          }
        })
        .match(SetGeneratorMessage.class, setGeneratorMessage ->
          this.generator = setGeneratorMessage.generator
        )
        .build();
  }
}
