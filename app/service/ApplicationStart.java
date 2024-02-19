package service;

import com.idos.cache.IdosConfigParamCache;
import com.idos.cache.OrganizationConfigCache;
import com.idos.util.IdosConstants;
import com.idos.util.RSAEncodeDecode;

import play.inject.ApplicationLifecycle;
import play.Environment;
import service.EntityManagerProvider;
import javax.inject.*;
import java.security.Key;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;
import play.db.jpa.JPAApi;

// This creates an `ApplicationStart` object once at start-up.
@Singleton
public class ApplicationStart {
    // private final IdosConstants idosConstants;
    // private final RSAEncodeDecode rsaEncodeDecode;
    private String publicKey;
    private String privateKey;

    // @Inject
    // public ApplicationStart(IdosConstants idosConstants, RSAEncodeDecode
    // rsaEncodeDecode) {
    // this.idosConstants = idosConstants;
    // this.rsaEncodeDecode = rsaEncodeDecode;
    // }
    // Inject the application's Environment upon start-up and register hook(s) for
    // shut-down.
    @Inject
    public ApplicationStart(ApplicationLifecycle lifecycle, Environment environment, JPAApi jpaApi) {
        // this.idosConstants = idosConstants;
        // this.rsaEncodeDecode = rsaEncodeDecode;
        // Shut-down hook
        System.out.println(">>>>>>>>>>> Inside Applciation Start <<<<<<<<<<");
        try {
            Map<String, Key> keyMap = RSAEncodeDecode.initKey();
            publicKey = RSAEncodeDecode.getPublicKey(keyMap);
            privateKey = RSAEncodeDecode.getPrivateKey(keyMap);
            IdosConstants.PUBLICK = publicKey;
            IdosConstants.PRIVATEK = privateKey;
            OrganizationConfigCache organizationConfigCache = new OrganizationConfigCache();
            organizationConfigCache.initialize();
            IdosConfigParamCache idosConfigParamCache = new IdosConfigParamCache();
            idosConfigParamCache.initialize();
            // JPAApiProvider.initialize(jpaApi);
            String message = "Application has started! DCFO";
            // Logger.info(message);
            System.out.println(message);
        } catch (Exception ex) {
            // Logger.error("Error", ex);
            ex.printStackTrace();
        }
        lifecycle.addStopHook(() -> {
            String message = "Application shutdown...!!! DCFO";
            // EntityManagerProvider.close();
            System.out.println(message);
            return CompletableFuture.completedFuture(null);
        });
    }
}
