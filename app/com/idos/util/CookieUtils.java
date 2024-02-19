package com.idos.util;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.*;
import play.mvc.Http.Cookie;
import play.mvc.Http.CookieBuilder;
import java.time.Duration;

public class CookieUtils {
    public static Result discardCookie(String cookieName) {
        Duration expirationDuration = Duration.ZERO;
        Result result = Results.ok("Cookie '"+ cookieName +"' discarded");        
        Cookie discardedCookie = Cookie.builder(cookieName,"").withMaxAge(expirationDuration).build();
        result = result.withCookies(discardedCookie);        
        return result;
    }

    public Result setCookie(String cookieName) {
        Duration expirationDuration = Duration.ofHours(1);
       //int expirationInSeconds = 3600;
        Cookie settlecookie = Cookie.builder(cookieName,"").withMaxAge(expirationDuration) .withSecure(false) .withHttpOnly(true) .build();
        Result result = Results.ok("Cookie '"+ cookieName +"' succesfull");
        result=result.withCookies(settlecookie);
        return result;
    }

}