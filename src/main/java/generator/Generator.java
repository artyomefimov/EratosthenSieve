package generator;

import abstractsieve.AbstractSieveElement;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import creator.ActorCreator.CreateNewActorMessage;
import sieveelement.SieveElement.HandleNextNumberMessage;

public class Generator extends AbstractSieveElement {

  public static Props props(ActorRef actorCreator) {
    return Props.create(Generator.class, () -> new Generator(actorCreator));
  }

  public Generator(ActorRef actorCreator) {
    this.actorCreator = actorCreator;
    currentNumber = 0;
    log = Logging.getLogger(getContext().getSystem(), this);
    this.getContext().watch(actorCreator);
  }

  private int getNextNumber() {
    return currentNumber == 0 ? 3 : currentNumber + 2;
  }

  public static class GenerateNextNumberMessage {

  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(GenerateNextNumberMessage.class,
            generateNextNumber -> generateNumberAndSendToChain())
        .match(GetNewActorMessage.class, this::getActorAndStartGeneration)
        .match(GeneratedMaxActorsMessage.class, this::sendFinishMessageToChain)
        .build();
  }

  private void generateNumberAndSendToChain() {
    generateNextNumber();

    if (nextSieveElement != null) {
      sendNumberToFirstInChain();
    } else {
      requestFirstElementInChain();
    }
  }

  private void requestFirstElementInChain() {
    actorCreator
        .tell(new CreateNewActorMessage(currentNumber), getSelf());
  }

  private void getActorAndStartGeneration(GetNewActorMessage message) {
    nextSieveElement = message.getNextActor();
    generateNumberAndSendToChain();
  }

  private void sendFinishMessageToChain(GeneratedMaxActorsMessage message) {
    log.info("Generation is ending. Last actor has number: " + currentNumber);

    nextSieveElement.tell(message, ActorRef.noSender());
    getContext().stop(getSelf());
  }

  private void generateNextNumber() {
    currentNumber = getNextNumber();

    log.info("Generated number: " + currentNumber);
  }

  private void sendNumberToFirstInChain() {
    nextSieveElement
        .tell(new HandleNextNumberMessage(currentNumber), getSelf());
  }
}
