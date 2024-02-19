// AdminActor.java

package actor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import play.libs.Json;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import akka.actor.*;

public class AdminActor extends AbstractActor {

    public static ActorRef actor;
    public static Map<String, ActorRef> adminRegistered = new HashMap<String, ActorRef>();
    // public static Map<String, ActorRef> adminRegistered = new
    // ConcurrentHashMap<>();

    public static Props props(ActorRef out) {
        return Props.create(AdminActor.class, out);
    }

    // Add this overloaded method with a default parameter
    public static Props props() {
        return Props.create(AdminActor.class, ActorRef.noSender());
    }

    public AdminActor(ActorRef out) {
        actor = out;
    }

    public static void register(String email, ActorRef out) {
        try {
            ActorRef value = adminRegistered.get(email);
            if (value != null) {
                adminRegistered.remove(email);
                value.tell("close", ActorRef.noSender()); // Close the existing WebSocket connection
            }
            System.out.println("Admin Actor ========================= " + actor);
            if (actor != null) {
                actor.tell(new LoginMessage(email, out).toString(), ActorRef.noSender());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void unregister(String email) throws Exception {
        System.out.println("Admin Actor(unreg) ========================= " + actor);
        LogoutMessage logOut = new LogoutMessage();
        logOut.setEmail(email);
        actor.tell(logOut.getEmail(), ActorRef.noSender());
    }

    public static void notification(Map<String, Object> notificationMap) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String messageJson = objectMapper.writeValueAsString(new NotificationMessage(notificationMap));

            actor.tell(messageJson, ActorRef.noSender());
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    public static void addBranch(long id, String branchName, String branchgstin, String country, String location,
            String phoneNumber, Map<String, ActorRef> orgregistereduser, String oldName, String actionText) {
        actor.tell(new BranchMessage(id, branchName, branchgstin, country,
                location, phoneNumber, orgregistereduser, oldName, actionText).toString(), ActorRef.noSender());
    }

    public static void addProject(long id, String name, String number, String startDate, String endDate,
            String location, Map<String, ActorRef> orgregistereduser, String actionText) {
        actor.tell(
                new ProjectMessage(id, name, number, startDate, endDate, location, orgregistereduser,
                        actionText).toString(),
                ActorRef.noSender());
    }

    public static void chat(String from, String to, String message, String name) {
        actor.tell(new ChatMessage(from, to, message, name).toString(), ActorRef.noSender());
    }

    public static void online(boolean result, ArrayNode node) {
        actor.tell(new OnlineUsersMessage(result, node).toString(), ActorRef.noSender());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, message -> {

        }).match(LoginMessage.class, registration -> {
            adminRegistered.put(registration.email, registration.channel);

        }).match(BranchMessage.class, addBranch -> {
            for (ActorRef channel : addBranch.orgregistrered.values()) {
                // Handle addBranch logic
                ObjectNode event = Json.newObject();
                ArrayNode an = event.putArray("branchData");
                event.put("id", addBranch.id);
                event.put("branchName", addBranch.branchName);
                event.put("branchgstin", addBranch.branchgstin);
                event.put("country", addBranch.country);
                event.put("location", addBranch.location);
                event.put("phoneNumber", addBranch.phoneNumber);
                event.put("oldName", addBranch.oldName);
                event.put("actionText", addBranch.actionText);
                channel.tell(Json.toJson(event), getSelf());
            }
        }).match(ProjectMessage.class, addProject -> {
            for (ActorRef channel : addProject.orgregistrered.values()) {
                // Handle addProject logic
                ObjectNode event = Json.newObject();
                ArrayNode an = event.putArray("branchData");
                event.put("id", addProject.id);
                event.put("name", addProject.name);
                event.put("number", addProject.number);
                event.put("startDate", addProject.startDate);
                event.put("endDate", addProject.endDate);
                event.put("location", addProject.location);
                event.put("actionText", addProject.actionText);
                channel.tell(Json.toJson(event), getSelf());
            }
        }).match(LogoutMessage.class, logout -> {
            // Handle logout logic
            System.out.println(logout);
            adminRegistered.remove(logout.email);
        }).match(ChatMessage.class, chat -> {
            // Handle chat logic
            ObjectNode event = Json.newObject();
            event.put("type", "chat");
            event.put("from", chat.from);
            event.put("to", chat.to);
            event.put("message", chat.message);
            event.put("name", chat.name);
            ActorRef channel = adminRegistered.get(chat.to);
            if (channel != null) {
                channel.tell(Json.toJson(event), getSelf());
            }
        }).match(OnlineUsersMessage.class, online -> {
            // Handle online users logic
            for (ActorRef channel : adminRegistered.values()) {
                ObjectNode event = Json.newObject();
                event.put("type", "onlineUsers");
                event.put("result", online.result);
                event.put("users", online.node);
                channel.tell(Json.toJson(event), getSelf());
            }
        }).match(NotificationMessage.class, nMessaage -> {
            Map<String, Object> notificationMap = nMessaage.notificationMap;
        }).build();
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
    }

    public static class BranchMessage {
        public Long id;
        public String branchName;
        public String branchgstin;
        public String country;
        public String location;
        public String phoneNumber;
        @JsonIgnore
        public Map<String, ActorRef> orgregistrered;
        public String oldName;
        public String actionText;

        public BranchMessage(Long id, String branchName, String branchgstin, String country, String location,
                String phoneNumber, Map<String, ActorRef> orgregistrered, String oldName, String actionText) {
            this.id = id;
            this.branchName = branchName;
            this.branchgstin = branchgstin == null ? "" : branchgstin;
            this.country = country;
            this.location = location;
            this.phoneNumber = phoneNumber;
            this.orgregistrered = orgregistrered;
            this.oldName = oldName;
            this.actionText = actionText;
        }

        @Override
        public String toString() {
            ObjectNode event = Json.newObject();
            ArrayNode an = event.putArray("branchData");
            event.put("id", this.id);
            event.put("branchName", this.branchName);
            event.put("branchgstin", this.branchgstin);
            event.put("country", this.country);
            event.put("location", this.location);
            event.put("phoneNumber", this.phoneNumber);
            event.put("oldName", this.oldName);
            event.put("actionText", this.actionText);
            return event.toString();
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
            ArrayNode an = event.putArray("branchData");
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

    public static class NotificationMessage {
        public Map<String, Object> notificationMap = new HashMap<String, Object>();

        public NotificationMessage(Map<String, Object> notificationMap) {
            this.notificationMap = notificationMap;
        }
    }

    public static class ChatMessage {
        public String from;
        public String to;
        public String message;
        public String name;

        public ChatMessage(String from, String to, String message, String name) {
            this.from = from;
            this.to = to;
            this.message = message;
            this.name = name;
        }

        @Override
        public String toString() {
            ObjectNode event = Json.newObject();
            event.put("type", "chat");
            event.put("from", this.from);
            event.put("to", this.to);
            event.put("message", this.message);
            event.put("name", this.name);
            return event.toString();
        }
    }

    public static class OnlineUsersMessage {
        public boolean result;
        public ArrayNode node;

        public OnlineUsersMessage(boolean result, ArrayNode node) {
            this.result = result;
            this.node = node;
        }

        @Override
        public String toString() {
            ObjectNode event = Json.newObject();
            event.put("type", "onlineUsers");
            event.put("result", this.result);
            event.put("users", this.node);
            return event.toString();
        }

    }

}
