package actor;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import play.libs.Akka;
import play.libs.Json;
import akka.actor.*;
import akka.actor.AbstractActor.Receive;

import actor.CreatorActor.LoginMessage;
import actor.CreatorActor.LogoutMessage;
import actor.ProjectTransactionActor.ProjectMessage;

public class ExceptionActor extends AbstractActor {

	// protected static Logger log = Logger.getLogger("actors");
	public static Map<String, ActorRef> exceptionregistrered = new HashMap<String, ActorRef>();
	public static ActorRef actor;

	public static Props props(ActorRef out) {
		return Props.create(ExceptionActor.class, out);
	}

	// Add this overloaded method with a default parameter
	public static Props props() {
		return Props.create(ExceptionActor.class, ActorRef.noSender());
	}

	public ExceptionActor(ActorRef out) {
		actor = out;
	}

	public static void register(String email, ActorRef out) {
		ActorRef value = exceptionregistrered.get(email);
		if (value != null) {
			exceptionregistrered.remove(email);
			value.tell("close", ActorRef.noSender()); // Close the existing WebSocket connection
		}
		actor.tell(new LoginMessage(email, out), ActorRef.noSender());
	}

	public static void unregister(String email) throws Exception {
		actor.tell(new LogoutMessage(email), ActorRef.noSender());
	}

	public static void error(String exceptionMessage, String email, ActorRef channel) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String messageJson = objectMapper
					.writeValueAsString(new RunTimeExceptionMessage(exceptionMessage, email, channel));

			actor.tell(messageJson, ActorRef.noSender());
		} catch (JsonProcessingException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, message -> {
			actor.tell("I received your message: " + message, getSelf());
			for (Map.Entry<String, ActorRef> entry : exceptionregistrered.entrySet()) {
				System.out.println("Email: " + entry.getKey() + ", ActorRef: " + entry.getValue());
			}
		}).match(LoginMessage.class, registration -> {
			exceptionregistrered.put(registration.email, registration.channel);
			for (Map.Entry<String, ActorRef> entry : exceptionregistrered.entrySet()) {
				System.out.println("Email: " + entry.getKey() + ", ActorRef: " + entry.getValue());
			}
		}).match(LogoutMessage.class, logout -> {
			// Handle logout logic
			exceptionregistrered.remove(logout.email);
		}).match(RunTimeExceptionMessage.class, exception -> {
			ActorRef channel = exception.channel;
			ObjectNode event = Json.newObject();
			ArrayNode an = event.putArray("branchData");
			event.put("email", exception.email);
			event.put("exceptionMessage", exception.exceptionMessage);
			channel.tell(Json.toJson(event), getSelf());
		}).build();
	}

	public static class LoginMessage {
		public String email;
		public ActorRef channel;

		public LoginMessage(String email, ActorRef channel) {
			this.email = email;
			this.channel = channel;
		}
	}

	public static class LogoutMessage {
		public String email;

		public LogoutMessage(String email) {
			this.email = email;
		}
	}

	public static class RunTimeExceptionMessage {
		public String exceptionMessage;
		public String email;
		public ActorRef channel;

		public RunTimeExceptionMessage(String exceptionMessage, String email, ActorRef channel) {
			this.exceptionMessage = exceptionMessage;
			this.email = email;
			this.channel = channel;
		}
	}
}
