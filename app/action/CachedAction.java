import play.cache.SyncCacheApi;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CachedAction extends Action<Cached> {
    private final SyncCacheApi cache;

    @Inject
    public CachedAction(SyncCacheApi cache) {
        this.cache = cache;
    }

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        String cacheKey = configuration.key(); // Get the cache key from the annotation
        Optional<Result> cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            // Return the cached result if available
            return CompletableFuture.completedFuture(cachedResult.get());
        } else {
            // Call the wrapped action if not cached
            return delegate.call(request).thenApply(result -> {
                cache.set(cacheKey, result, configuration.duration());
                return result;
            });
        }
    }
}
