package com.idos.util;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 13-07-2016.
 */
public class DBResourceUtil {
    public static void closeDBConnection(EntityManager em) {
        if (em != null & em.isOpen()) {
            em.close();
        }
    }
}
