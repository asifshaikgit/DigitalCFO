package controllers;

import actor.CreatorActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.WebSocket;
import play.mvc.Result;
import java.util.function.Function;

import javax.inject.Inject;

public class WorkflowWS extends Controller {
    private final ActorSystem actorSystem;
    private final Materializer materializer;

    @Inject
    public WorkflowWS(ActorSystem actorSystem, Materializer materializer) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    public WebSocket workflowsocket() {
        ActorRef in = actorSystem.actorOf(CreatorActor.props());
        ActorRef out = actorSystem.actorOf(CreatorActor.props(in));

        // Use a lambda function to create the Props instance
        Function<ActorRef, Props> propsFunction = (ActorRef ref) -> CreatorActor.props(ref);

        return WebSocket.Text.accept(
                request -> {
                    String requestMessage = request.queryString("email").orElse(null);
                    CreatorActor.register(requestMessage, out);
                    return ActorFlow.actorRef(propsFunction, actorSystem, materializer);
                });
    }
}
