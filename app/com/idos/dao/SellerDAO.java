package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import java.io.File;

public interface SellerDAO extends BaseDAO {
	public ObjectNode uploadSupplierItems(Request request, ObjectNode result, JsonNode json,
			MultipartFormData<File> body, EntityManager entityManager, EntityTransaction entitytransaction);
}
