package com.idos.dao;

import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jfree.util.Log;
import akka.NotUsed;
import actor.AdminActor;
import actor.CreatorActor;
import actor.ProjectTransactionActor;
import actor.SpecificsTransactionActor;
import actor.VendorTransactionActor;

import play.libs.Json;
import play.mvc.WebSocket;
import play.mvc.WebSocket.*;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import java.util.logging.Level;
import akka.actor.*;

public class OnlineDAOImpl implements OnlineDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public ObjectNode getOnlineIdosUsers(ObjectNode result, JsonNode json,
			Users user, EntityManager entityManager) {
		log.log(Level.FINE, ">>>> Start");
		ArrayNode onlineUsersan = result.putArray("onlineUsersData");
		Map<String, ActorRef> adminRegistered = AdminActor.adminRegistered;
		Map<String, ActorRef> expenseregistrered = CreatorActor.expenseregistrered;
		Map<String, ActorRef> projectRegistered = ProjectTransactionActor.projectRegistered;
		Map<String, ActorRef> registrered = SpecificsTransactionActor.registrered;
		Map<String, ActorRef> vendvendregistrered = VendorTransactionActor.vendvendregistrered;
		ObjectNode row = Json.newObject();
		row.put("adminRegistered", adminRegistered.size());
		row.put("expenseregistrered", expenseregistrered.size());
		row.put("projectRegistered", projectRegistered.size());
		row.put("specificsRegistrered", registrered.size());
		row.put("vendorRegistrered", vendvendregistrered.size());
		onlineUsersan.add(row);
		log.log(Level.FINE, ">>>> End");
		return result;
	}
}
