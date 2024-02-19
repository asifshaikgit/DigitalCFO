package com.idos.util;

public class UnauthorizedAPIAccessException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public static String unauthorizedApiAccessException;
	
	public UnauthorizedAPIAccessException(){
		unauthorizedApiAccessException="You Are Not Authorized To Access IDOS API.";
	}
}
