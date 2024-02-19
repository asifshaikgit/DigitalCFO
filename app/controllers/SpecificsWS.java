package controllers;

import java.util.function.Function;

import javax.inject.Inject;

import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.WebSocket;
import actor.SpecificsTransactionActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;

public class SpecificsWS extends Controller {

	private final ActorSystem actorSystem;
	private final Materializer materializer;

	@Inject
	public SpecificsWS(ActorSystem actorSystem, Materializer materializer) {
		this.actorSystem = actorSystem;
		this.materializer = materializer;
	}

	public WebSocket specificssocket() {
		ActorRef in = actorSystem.actorOf(SpecificsTransactionActor.props());
		ActorRef out = actorSystem.actorOf(SpecificsTransactionActor.props(in));

		// Use a lambda function to create the Props instance
		Function<ActorRef, Props> propsFunction = (ActorRef ref) -> SpecificsTransactionActor.props(ref);

		return WebSocket.Text.accept(
				request -> {
					String requestMessage = request.queryString("email").orElse(null);
					SpecificsTransactionActor.register(requestMessage, out);
					return ActorFlow.actorRef(propsFunction, actorSystem, materializer);
				});
	}
}
