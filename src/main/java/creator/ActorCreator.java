package creator;

import abstractsieve.AbstractSieveElement.GeneratedMaxActorsMessage;
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

public class ActorCreator extends AbstractActor {

  private ActorSystem actorSystem;
  private ActorRef generator;
  private LoggingAdapter log;
  private int generatedActors;
  private int maxActors;
  private List<String> generatedActorsNames;
  private long time = -1;

  public static Props props(ActorSystem actorSystem, int maxActors) {
    return Props.create(ActorCreator.class, () -> new ActorCreator(actorSystem, maxActors));
  }

  public ActorCreator(ActorSystem actorSystem, int maxActors) {
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

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(CreateNewActorMessage.class, createNewActor -> {
          setTimeIfNeeded();

          if (generatedActors < maxActors) {
            ActorRef newSieveElement = createNewSieveElement(createNewActor);

            sendNewSieveElementToRequester(newSieveElement);
          } else {
            finishWork();
          }
        })
        .match(SetGeneratorMessage.class, setGeneratorMessage ->
            generator = setGeneratorMessage.generator
        )
        .build();
  }

  private void setTimeIfNeeded() {
    if (time == -1) {
      time = System.currentTimeMillis();
      log.info("time set");
    }
  }

  private ActorRef createNewSieveElement(CreateNewActorMessage message) {
    String newActorName = generateNewSieveElementName(message);

    ActorRef newSieveElement = actorSystem
            .actorOf(SieveElement.props(getSelf(),
                    message.numberForNewActor,
                    generator),
                    newActorName);

    log.info("Created sieve element: " + newActorName);

    generatedActors++;

    return newSieveElement;
  }

  private String generateNewSieveElementName(CreateNewActorMessage message) {
    String newActorName = "HandlerOfNumber" + message.numberForNewActor;
    generatedActorsNames.add(newActorName);

    return newActorName;
  }

  private void sendNewSieveElementToRequester(ActorRef newSieveElement) {
    getSender().tell(
            new GetNewActorMessage(newSieveElement),
            ActorRef.noSender());
  }

  private void finishWork() {
    log.info("Work has finished. Creator has created all possible actors: " + maxActors
            + ". Actors names: " + generatedActorsNames +
            "\ncalculation time: " + (System.currentTimeMillis() - time) + " ms");

    generator.tell(new GeneratedMaxActorsMessage(), ActorRef.noSender());
  }
}
