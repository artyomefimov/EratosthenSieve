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
  private boolean isWaitingForNextElement;

  public static Props props(ActorRef actorCreator, int currentNumber, ActorRef generator) {
    return Props
        .create(SieveElement.class, () -> new SieveElement(actorCreator, currentNumber, generator));
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

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(HandleNextNumberMessage.class, nextNumberMessage -> {
          if (isWaitingForNextElement) {
            enqueueNumberMessage(nextNumberMessage);
          } else {
            if (isDividedWithNoRemainder(nextNumberMessage.number, currentNumber)) {
              finishHandlingAndRequestNewNumber(nextNumberMessage);
            } else {
              delegateHandlingToAnotherActor(nextNumberMessage);
            }
          }
        })
        .match(GetNewActorMessage.class, this::setNextActorAndDelegateNumberMessage)
        .match(GeneratedMaxActorsMessage.class, this::forwardMessageAndStopSelf)
        .build();
  }

  private void enqueueNumberMessage(HandleNextNumberMessage message) {
    getSelf().tell(message, getSelf());
  }

  private boolean isDividedWithNoRemainder(int divisible, int divider) {
    return divisible % divider == 0;
  }

  private void finishHandlingAndRequestNewNumber(HandleNextNumberMessage message) {
    log.info(message.number + " was handled by actor with number " + currentNumber
        + ". Sending a request for a new number.");

    generator.tell(new GenerateNextNumberMessage(), getSelf());
  }

  private void delegateHandlingToAnotherActor(HandleNextNumberMessage message) {
    log.info(message.number + " was not handled by actor with number " + currentNumber);

    if (nextSieveElement == null) {
      requestNewActor(message);
    } else {
      delegateHandlingToNextActor(message);
    }
  }

  private void requestNewActor(HandleNextNumberMessage message) {
    log.info("No next actor in chain. Sending a request for creating a new one.");

    receivedMessage = message;
    actorCreator
        .tell(new CreateNewActorMessage(message.number), getSelf());

    isWaitingForNextElement = true;
  }

  private void delegateHandlingToNextActor(HandleNextNumberMessage message) {
    log.info("Sending number to the next actor in chain.");

    nextSieveElement.tell(message, getSelf());
  }

  private void setNextActorAndDelegateNumberMessage(GetNewActorMessage message) {
    nextSieveElement = message.getNextActor();
    log.info("Next sieve element is: " + nextSieveElement.toString());

    isWaitingForNextElement = false;
    nextSieveElement.tell(receivedMessage, ActorRef.noSender());
  }

  private void forwardMessageAndStopSelf(GeneratedMaxActorsMessage message) {
    if (nextSieveElement != null) {
      nextSieveElement.tell(message, ActorRef.noSender());
    }

    log.info("Stopping...");
    getContext().stop(getSelf());
  }
}
