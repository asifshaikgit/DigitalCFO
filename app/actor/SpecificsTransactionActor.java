package actor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Specifics;

import play.libs.Json;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import play.libs.Akka;
import akka.actor.*;

public class SpecificsTransactionActor extends AbstractActor {

    public static ActorRef actor;
    public static Map<String, ActorRef> registrered = new HashMap<String, ActorRef>();

    public static Props props(ActorRef out) {
        return Props.create(SpecificsTransactionActor.class, out);
    }

    // Add this overloaded method with a default parameter
    public static Props props() {
        return Props.create(SpecificsTransactionActor.class, ActorRef.noSender());
    }

    public SpecificsTransactionActor(ActorRef out) {
        actor = out;
    }

    public static void register(String email, ActorRef out) {
        try {
            ActorRef value = registrered.get(email);
            if (value != null) {
                // log.log(Level.INFO, "client email is removed as key if fount and again registered on as
                // key");
                registrered.remove(email);
                value.tell("close", ActorRef.noSender()); // Close the existing WebSocket connection
            }
            // log.log(Level.INFO, String.valueOf("Specifics actor registered
            // user"+registrered.values().size()));
            System.out.println("Specific Actor ========================= " + actor);
            if (actor != null) {
                actor.tell(new LoginMessage(email, out).toString(), ActorRef.noSender());
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public static void unregister(String email) throws Exception {
        System.out.println("Specific Actor(Unreg) ========================= " + actor);
        LogoutMessage logOut = new LogoutMessage();
        logOut.setEmail(email);
        actor.tell(logOut.getEmail(), ActorRef.noSender());
    }

    public static void add(final Long id, final Long parentAccountCode, final Long topLevelAccountCode,
            final List<Specifics> childSpecifics, final String name, final Long catId, final Long accountCode,
            final Map<String, ActorRef> orgregistrered, final String orgName, final String itemBranches,
            final String btnName, String hiddenPrimKey, String identDataValid) {
        try {
            actor.tell(new AddSpecificsMessage(id, parentAccountCode, topLevelAccountCode,
                    childSpecifics, name, catId,
                    accountCode, orgregistrered, orgName, itemBranches, btnName, hiddenPrimKey,
                    identDataValid).toString(), ActorRef.noSender());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LoginMessage.class, registration -> {
                    registrered.put(registration.email, registration.channel);
                })
                .match(AddSpecificsMessage.class, add -> {
                    for (ActorRef channel : add.orgregistrered.values()) {
                        ObjectNode event = Json.newObject();
                        ArrayNode an = event.putArray("coaChildData");
                        event.put("id", add.id);
                        event.put("name", add.name);
                        event.put("catId", add.catId);
                        event.put("accountCode", add.accountCode.toString());
                        event.put("organization", add.orgName);
                        event.put("itemBranches", add.itemBranches);
                        event.put("topLevelAccountCode", add.topLevelAccountCode.toString());
                        event.put("parentAccountCode", add.parentAccountCode.toString());
                        event.put("btnName", add.btnName);
                        event.put("itemHidpk", add.hiddenPrimKey);
                        event.put("identDataValid", add.identDataValid);
                        if (add.childSpecifics.size() > 0) {
                            for (Specifics specf : add.childSpecifics) {
                                ObjectNode row = Json.newObject();
                                row.put("id", specf.getId());
                                row.put("name", specf.getName());
                                row.put("accountCode", specf.getAccountCode());
                                an.add(row);
                            }
                        }
                        channel.tell(Json.toJson(event), getSelf());
                    }
                }).match(LogoutMessage.class, logout -> {
                    System.out.println(logout);
                    registrered.remove(logout.email);
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

    public static class AddSpecificsMessage {

        public Long id;

        public Long topLevelAccountCode;

        public Long parentAccountCode;

        public List<Specifics> childSpecifics;

        public String name;

        public Long catId;

        public Long accountCode;

        @JsonIgnore
        public Map<String, ActorRef> orgregistrered;

        public String orgName;

        public String itemBranches;

        public String btnName;

        public String hiddenPrimKey;

        public String identDataValid;

        public AddSpecificsMessage(Long id, Long parentAccountCode, Long topLevelAccountCode,
                List<Specifics> childSpecifics, String name, Long catId, Long accountCode,
                Map<String, ActorRef> orgregistrered, String orgName, String itemBranches,
                String btnName, String hiddenPrimKey, String identDataValid) {
            this.id = id;
            this.parentAccountCode = parentAccountCode;
            this.topLevelAccountCode = topLevelAccountCode;
            this.childSpecifics = childSpecifics;
            this.name = name;
            this.catId = catId;
            this.accountCode = accountCode;
            this.orgregistrered = orgregistrered;
            this.orgName = orgName;
            this.itemBranches = itemBranches;
            this.btnName = btnName;
            this.hiddenPrimKey = hiddenPrimKey;
            this.identDataValid = identDataValid;
        }

        @Override
        public String toString() {
            ObjectNode event = Json.newObject();
            ArrayNode an = event.putArray("coaChildData");
            event.put("id", this.id);
            event.put("name", this.name);
            event.put("catId", this.catId);
            event.put("accountCode", this.accountCode.toString());
            event.put("organization", this.orgName);
            event.put("itemBranches", this.itemBranches);
            event.put("topLevelAccountCode", this.topLevelAccountCode.toString());
            event.put("parentAccountCode", this.parentAccountCode.toString());
            event.put("btnName", this.btnName);
            event.put("itemHidpk", this.hiddenPrimKey);
            event.put("identDataValid", this.identDataValid);
            if (this.childSpecifics.size() > 0) {
                for (Specifics specf : this.childSpecifics) {
                    ObjectNode row = Json.newObject();
                    row.put("id", specf.getId());
                    row.put("name", specf.getName());
                    row.put("accountCode", specf.getAccountCode());
                    an.add(row);
                }
            }
            return event.toString();
        }
    }
}
