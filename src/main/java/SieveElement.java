import akka.actor.Props;

public class SieveElement extends AbstractSieveElement {

  public static Props props(ActorCreator actorCreator) {
    return Props.create(SieveElement.class, () -> new SieveElement(actorCreator));
  }

  public SieveElement(ActorCreator actorCreator) {
    this.actorCreator = actorCreator;
  }

  public static class HandleNextNumberMessage {
    private int nextNumber;
    public HandleNextNumberMessage(int nextNumber) {
      this.nextNumber = nextNumber;
    }
  }

  @Override
  public Receive createReceive() {
    return null;
  }
}
