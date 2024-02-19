import com.google.inject.AbstractModule;

import service.ApplicationStart;
import service.EntityManagerProvider;
import play.db.jpa.JPAApi;

public class Module extends AbstractModule {

  public void configure() {
    bind(EntityManagerProvider.class).asEagerSingleton();
    bind(ApplicationStart.class).asEagerSingleton();
  }
}