package actor;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import actor.SpecificsTransactionActor.AddSpecificsMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import play.libs.Json;
import akka.actor.*;
import akka.actor.AbstractActor.Receive;
import play.libs.Akka;

public class VendorTransactionActor extends AbstractActor {

    public static Map<String, ActorRef> vendvendregistrered = new HashMap<String, ActorRef>();
    public static ActorRef actor;

    public static Props props(ActorRef out) {
        return Props.create(VendorTransactionActor.class, out);
    }

    // Add this overloaded method with a default parameter
    public static Props props() {
        return Props.create(VendorTransactionActor.class, ActorRef.noSender());
    }

    public VendorTransactionActor(ActorRef out) {
        actor = out;
    }

    public static void register(final String email, final ActorRef out) {
        try {
            ActorRef value = vendvendregistrered.get(email);
            if (value != null) {
                // log.log(Level.INFO, "client email is removed as key if fount and again registered on as
                // key");
                vendvendregistrered.remove(email);
                value.tell("close", ActorRef.noSender()); // Close the existing WebSocket connection
            }
            // log.log(Level.INFO, String.valueOf("Vendor actor registered
            // user"+vendvendregistrered.values().size()));
            System.out.println("Vendor Actor ========================= " + actor);
            if (actor != null) {
                actor.tell(new LoginMessage(email, out).toString(), ActorRef.noSender());
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public static void unregister(String email) throws Exception {
        System.out.println("Vendor Actor (unreg)========================= " + actor);
        LogoutMessage logOut = new LogoutMessage();
        logOut.setEmail(email);
        actor.tell(logOut.getEmail(), ActorRef.noSender());
    }

    public static void add(final Long id, final Map<String, ActorRef> orgvendvendregistrered, final String name,
            final String address, final String location, final String email, final Integer grantAccess,
            final String phone, final Integer type, final String entityType, final Integer presentStatus) {

        actor.tell(new AddVendorMessage(id, orgvendvendregistrered, name, address, location, email,
                grantAccess, phone,
                type, entityType, presentStatus).toString(), ActorRef.noSender());
    }

    public static void addGroup(final Long id, final Map<String, ActorRef> orgvendvendregistrered,
            final String groupName, final Integer type, final String entityType) {

        actor.tell(new AddVendorGroupMessage(id, orgvendvendregistrered, groupName, type, entityType).toString(),
                ActorRef.noSender());

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LoginMessage.class, registration -> {
                    vendvendregistrered.put(registration.email, registration.channel);
                })
                .match(AddVendorMessage.class, add -> {
                    for (ActorRef channel : add.orgvendvendregistrered.values()) {
                        ObjectNode event = Json.newObject();
                        event.put("id", add.id);
                        event.put("name", add.name);
                        event.put("address", add.address);
                        event.put("location", add.location);
                        event.put("email", add.email);
                        event.put("grantAccess", add.grantAccess);
                        event.put("phone", add.phone);
                        event.put("type", add.type);
                        event.put("entityType", add.entityType);
                        event.put("presentStatus", add.presentStatus);
                        channel.tell(Json.toJson(event), getSelf());
                    }
                })
                .match(AddVendorGroupMessage.class, addGroup -> {
                    for (ActorRef channel : addGroup.orgvendvendregistrered.values()) {
                        ObjectNode event = Json.newObject();
                        event.put("id", addGroup.id);
                        event.put("groupname", addGroup.groupName);
                        event.put("type", addGroup.type);
                        event.put("entityType", addGroup.entityType);
                        channel.tell(Json.toJson(event), getSelf());
                    }
                })
                .match(LogoutMessage.class, logout -> {
                    System.out.println(logout);
                    vendvendregistrered.remove(logout.getEmail());
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

    public static class AddVendorMessage {
        public Long id;
        @JsonIgnore
        public Map<String, ActorRef> orgvendvendregistrered;
        public String name;
        public String address;
        public String location;
        public String email;
        public Integer grantAccess;
        public String phone;
        public Integer type;
        public String entityType;
        public Integer presentStatus;

        public AddVendorMessage(Long id, Map<String, ActorRef> orgvendvendregistrered, String name, String address,
                String location, String email, Integer grantAccess, String phone, Integer type, String entityType,
                Integer presentStatus) {
            this.id = id;
            this.orgvendvendregistrered = orgvendvendregistrered;
            this.name = name;
            this.address = address;
            this.location = location;
            this.email = email;
            this.grantAccess = grantAccess;
            this.phone = phone;
            this.type = type;
            this.entityType = entityType;
            this.presentStatus = presentStatus;
        }

        @Override
        public String toString() {
            ObjectNode event = Json.newObject();
            event.put("id", this.id);
            event.put("name", this.name);
            event.put("address", this.address);
            event.put("location", this.location);
            event.put("email", this.email);
            event.put("grantAccess", this.grantAccess);
            event.put("phone", this.phone);
            event.put("type", this.type);
            event.put("entityType", this.entityType);
            event.put("presentStatus", this.presentStatus);
            return event.toString();
        }

    }

    public static class AddVendorGroupMessage {
        public Long id;
        @JsonIgnore
        public Map<String, ActorRef> orgvendvendregistrered;
        public String groupName;
        public Integer type;
        public String entityType;

        public AddVendorGroupMessage(Long id, Map<String, ActorRef> orgvendvendregistrered, String groupName,
                Integer type, String entityType) {
            this.id = id;
            this.orgvendvendregistrered = orgvendvendregistrered;
            this.groupName = groupName;
            this.type = type;
            this.entityType = entityType;
        }

        @Override
        public String toString() {
            ObjectNode event = Json.newObject();
            event.put("id", this.id);
            event.put("groupname", this.groupName);
            event.put("type", this.type);
            event.put("entityType", this.entityType);
            return event.toString();
        }

    }
}
