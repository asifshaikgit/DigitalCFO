package actor;

import akka.actor.*;
import com.fasterxml.jackson.databind.JsonNode;

public class HelloActor extends AbstractActor {

    public static Props props(ActorRef out) {
        return Props.create(HelloActor.class, out);
    }

    private final ActorRef out;

    public HelloActor(ActorRef out) {
        this.out = out;
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(String.class, message -> {
                    JsonNode jsonMessage = play.libs.Json.parse(message);
                    String value = jsonMessage.get("connect").asText();
                    out.tell("I received your JSON message. Extracted value: " + value, self());
                })
                .build();
    }
}