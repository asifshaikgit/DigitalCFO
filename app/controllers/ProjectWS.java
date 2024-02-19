package controllers;

import actor.ProjectTransactionActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.WebSocket;
import java.util.function.Function;
import javax.inject.Inject;

public class ProjectWS extends Controller {

    private final ActorSystem actorSystem;
    private final Materializer materializer;

    @Inject
    public ProjectWS(ActorSystem actorSystem, Materializer materializer) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    public WebSocket projectsocket() {
        ActorRef in = actorSystem.actorOf(ProjectTransactionActor.props());
        ActorRef out = actorSystem.actorOf(ProjectTransactionActor.props(in));

        // Use a lambda function to create the Props instance
        Function<ActorRef, Props> propsFunction = (ActorRef ref) -> ProjectTransactionActor.props(ref);

        return WebSocket.Text.accept(
                request -> {
                    String requestMessage = request.queryString("email").orElse(null);
                    ProjectTransactionActor.register(requestMessage, out);
                    return ActorFlow.actorRef(propsFunction, actorSystem, materializer);
                });
    }
}
