package controllers;

import javax.inject.Inject;

import actor.AdminActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.WebSocket;
import play.mvc.Result;
import play.mvc.Results;
import java.util.function.Function;

public class BranchWS extends BaseController {
    private final ActorSystem actorSystem;
    private final Materializer materializer;

    @Inject
    public BranchWS(ActorSystem actorSystem, Materializer materializer) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    public WebSocket branchsocket() {
        ActorRef in = actorSystem.actorOf(AdminActor.props());
        ActorRef out = actorSystem.actorOf(AdminActor.props(in));

        // Use a lambda function to create the Props instance
        Function<ActorRef, Props> propsFunction = (ActorRef ref) -> AdminActor.props(ref);

        return WebSocket.Text.accept(
                request -> {
                    String requestMessage = request.queryString("email").orElse(null);
                    AdminActor.register(requestMessage, out);
                    return ActorFlow.actorRef(propsFunction, actorSystem, materializer);
                });
    }
}
