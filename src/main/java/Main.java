import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import creator.ActorCreator;
import creator.ActorCreator.SetGeneratorMessage;
import generator.Generator;
import generator.Generator.GenerateNextNumberMessage;

public class Main {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("eratosthen_sieve");
    try {

      int maxActors = 25;
      int messagesInChain = 1;

      ActorRef actorCreator = system.actorOf(ActorCreator.props(system, maxActors));
      ActorRef generator = system.actorOf(Generator.props(actorCreator, messagesInChain));

      actorCreator.tell(new SetGeneratorMessage(generator), ActorRef.noSender());

      generator.tell(new GenerateNextNumberMessage(), ActorRef.noSender());

      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      system.terminate();
    }
  }
}
