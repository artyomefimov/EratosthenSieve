package sieveelement;

import abstractsieve.AbstractSieveElement;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import creator.ActorCreator.CreateNewActorMessage;
import generator.Generator.GenerateNextNumberMessage;

public class SieveElement extends AbstractSieveElement {
  private HandleNextNumberMessage receivedMessage;
  private ActorRef generator;

  public static Props props(ActorRef actorCreator, int currentNumber, ActorRef generator) {
    return Props.create(SieveElement.class, () -> new SieveElement(actorCreator, currentNumber, generator));
  }

  public SieveElement(ActorRef actorCreator, int currentNumber, ActorRef generator) {
    this.actorCreator = actorCreator;
    this.currentNumber = currentNumber;
    this.generator = generator;
    log = Logging.getLogger(getContext().getSystem(), this);
  }

  public static class HandleNextNumberMessage {
    private int number;

    public HandleNextNumberMessage(int number) {
      this.number = number;
    }
  }

  public static class GeneratedMaxActorsMessage {

  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(HandleNextNumberMessage.class, nextNumberMessage -> {
          if (isHasNoRemainder(nextNumberMessage.number, currentNumber)) {
            log.info(nextNumberMessage.number + " was handled by actor with number " + currentNumber + ". Sending a request for a new number.");
            generator.tell(new GenerateNextNumberMessage(), getSelf());
          } else {
            log.info(nextNumberMessage.number + " was not handled by actor with number " + currentNumber);
            if (nextSieveElement == null) {
              log.info("No next actor in chain. Sending a request for creating a new one.");
              receivedMessage = nextNumberMessage;
              actorCreator
                  .tell(new CreateNewActorMessage(nextNumberMessage.number), getSelf());
            } else {
              log.info("Sending number to the next actor in chain.");
              nextSieveElement.tell(nextNumberMessage, getSelf());
            }
          }
        })
        .match(GetNewActorMessage.class, newActorMessage -> {
          nextSieveElement = newActorMessage.getNextActor();
          nextSieveElement.tell(receivedMessage, ActorRef.noSender());
        })
        .match(GeneratedMaxActorsMessage.class, generatedMaxActorsMessage -> {
          log.info("Generation is ending. Last actor has number: " + currentNumber);
        })
        .build();
  }

  private boolean isHasNoRemainder(int divisible, int divider) {
    return divisible % divider == 0;
  }
}
