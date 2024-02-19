package controllers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import model.Organization;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idos.util.DateUtil;
import javax.transaction.Transactional;
import javax.inject.Inject;
import play.libs.Json;
import play.mvc.WebSocket;
import actor.AdminActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import play.libs.streams.ActorFlow;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import service.ExceptionService;

public class NotificationWS extends BaseController {
	public ExceptionService expService;
	private final ActorSystem actorSystem;
	private final Materializer materializer;

	@Inject
	public NotificationWS(ActorSystem actorSystem, Materializer materializer) {
		this.actorSystem = actorSystem;
		this.materializer = materializer;
	}

	public WebSocket notificationsocket() {
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
