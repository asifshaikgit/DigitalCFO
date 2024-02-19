package actor;

import java.util.HashMap;
import java.util.Map;

import play.libs.Json;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import actor.ExceptionActor.RunTimeExceptionMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import play.libs.Akka;
import akka.actor.*;
import akka.actor.AbstractActor.Receive;

public class ProjectTransactionActor extends AbstractActor {

	public static Map<String, ActorRef> projectRegistered = new HashMap<String, ActorRef>();
	public static ActorRef actor;

	public ProjectTransactionActor(ActorRef out) {
		actor = out;
	}

	public static Props props(ActorRef out) {
		return Props.create(ProjectTransactionActor.class, out);
	}

	// Add this overloaded method with a default parameter
	public static Props props() {
		return Props.create(ProjectTransactionActor.class, ActorRef.noSender());
	}

	public static void register(final String email, final ActorRef out) {
		try {
			ActorRef value = projectRegistered.get(email);
			if (value != null) {
				// log.log(Level.INFO, "client email is removed as key if found and again registered on as
				// key");
				projectRegistered.remove(email);
				value.tell("close", ActorRef.noSender()); // Close the existing WebSocket connection
			}
			if (actor != null) {
				actor.tell(new LoginMessage(email, out).toString(), ActorRef.noSender());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static void unregister(String email) throws Exception {
		System.out.println("Project Actor(unreg) ========================= " + actor);
		LogoutMessage logOut = new LogoutMessage();
		logOut.setEmail(email);
		actor.tell(logOut.getEmail(), ActorRef.noSender());
	}

	public static void addProject(final long id, final String name, final String number, final String startDate,
			final String endDate, final String location, final Map<String, ActorRef> orgregistereduser,
			String actionText) {
		actor.tell(new ProjectMessage(id, name, number, startDate, endDate, location,
				orgregistereduser, actionText).toString(), ActorRef.noSender());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(LoginMessage.class, registration -> {
					projectRegistered.put(registration.email, registration.channel);
				})
				.match(ProjectMessage.class, add -> {
					for (ActorRef channel : add.orgregistrered.values()) {
						ObjectNode event = Json.newObject();
						ArrayNode an = event.putArray("branchData");
						event.put("id", add.id);
						event.put("name", add.name);
						event.put("number", add.number);
						event.put("startDate", add.startDate);
						event.put("endDate", add.endDate);
						event.put("location", add.location);
						event.put("actionText", add.actionText);
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(LogoutMessage.class, logout -> {
					System.out.println(logout);
					projectRegistered.remove(logout.email);
				})
				.build();

	}

	public static class LoginMessage {
		public String email;
		public ActorRef channel;

		public LoginMessage(String email, ActorRef channel) {
			this.email = email;
			this.channel = channel;
		}

		@Override
		public String toString() {
			return email;
		}
	}

	public static class LogoutMessage {
		public String email;

		public LogoutMessage() {

		}

		public LogoutMessage(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		@Override
		public String toString() {
			return "LogoutMessage [email=" + email + "]";
		}

	}

	public static class ProjectMessage {
		public Long id;
		public String name;
		public String number;
		public String startDate;
		public String endDate;
		public String location;
		@JsonIgnore
		public Map<String, ActorRef> orgregistrered;
		public String actionText;

		public ProjectMessage(Long id, String name, String number, String startDate, String endDate, String location,
				Map<String, ActorRef> orgregistrered, String actionText) {
			this.id = id;
			this.name = name;
			this.number = number;
			this.startDate = startDate;
			this.endDate = endDate;
			this.orgregistrered = orgregistrered;
			this.location = location;
			this.actionText = actionText;
		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			ArrayNode an = event.putArray("projectdetailsData");
			event.put("id", this.id);
			event.put("name", this.name);
			event.put("number", this.number);
			event.put("startDate", this.startDate);
			event.put("endDate", this.endDate);
			event.put("location", this.location);
			event.put("actionText", this.actionText);
			return event.toString();
		}

	}
}
