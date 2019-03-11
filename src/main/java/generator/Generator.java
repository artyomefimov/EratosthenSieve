package generator;

import abstractsieve.AbstractSieveElement;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import creator.ActorCreator.CreateNewActorMessage;
import sieveelement.SieveElement.HandleNextNumberMessage;

public class Generator extends AbstractSieveElement {
    private int messagesInChain;

  public static Props props(ActorRef actorCreator, int messagesInChain) {
    return Props.create(Generator.class, () -> new Generator(actorCreator, messagesInChain));
  }

  public Generator(ActorRef actorCreator, int messagesInChain) {
    this.actorCreator = actorCreator;
    currentNumber = 0;
    log = Logging.getLogger(getContext().getSystem(), this);
    this.getContext().watch(actorCreator);
    this.messagesInChain = messagesInChain;
  }

  private int getNextNumber() {
    return currentNumber == 0 ? 3 : currentNumber + 2;
  }

  public static class GenerateNextNumberMessage {

  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(GenerateNextNumberMessage.class, generateNextNumber -> {
          currentNumber = getNextNumber();
          log.info("Generated number: " + currentNumber);
          if (nextSieveElement == null) {
            actorCreator
                .tell(new CreateNewActorMessage(currentNumber), getSelf());
          } else {
            nextSieveElement
                .tell(new HandleNextNumberMessage(currentNumber), getSelf());
          }
        })
        .match(Terminated.class, terminated -> {
          if (terminated.actor() == actorCreator) {
            log.info("creator terminated");
          }
        })
        .match(GetNewActorMessage.class, newActorMessage -> {
          nextSieveElement = newActorMessage.getNextActor();
                for (int i = 0; i < messagesInChain; i++)
                {
                    currentNumber = getNextNumber();
                    nextSieveElement
                            .tell(new HandleNextNumberMessage(currentNumber), getSelf());
                }
        })
        .build();
  }
}
