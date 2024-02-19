package controllers;

import play.libs.streams.ActorFlow;
import play.mvc.*;
import akka.actor.*;
import akka.stream.*;
import javax.inject.Inject;

import actor.HelloActor;

public class HelloWS extends BaseController {

    private final ActorSystem actorSystem;
    private final Materializer materializer;

    @Inject
    public HelloWS(ActorSystem actorSystem, Materializer materializer) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    public WebSocket sayHello() {
        return WebSocket.Text.accept(
                request -> ActorFlow.actorRef(HelloActor::props, actorSystem, materializer));
    }
}
