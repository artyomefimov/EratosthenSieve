import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Generator extends AbstractSieveElement {
  private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public static Props props(ActorCreator actorCreator) {
    return Props.create(Generator.class, () -> new Generator(actorCreator));
  }

  public Generator(ActorCreator actorCreator) {
    this.actorCreator = actorCreator;
    currentNumber = 1;
  }

  private int getNextNumber() {
    return currentNumber + 2;
  }

  public static class GenerateNextNumberMessage {

  }

  public static class EndGenerationMessage {

  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(EndGenerationMessage.class, endGeneration ->
          log.debug("Generation is ending. Last generated number is: " + currentNumber)
        )
        .match(GenerateNextNumberMessage.class, generateNextNumber -> {
          currentNumber = getNextNumber();
          getSender().tell(new SieveElement.HandleNextNumberMessage(currentNumber), getSelf());
        })
        .build();
  }
}
