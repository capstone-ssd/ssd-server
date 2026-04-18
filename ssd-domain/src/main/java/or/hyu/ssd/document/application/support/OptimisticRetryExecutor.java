package or.hyu.ssd.document.application.support;

import java.util.function.Supplier;

public interface OptimisticRetryExecutor {

    <T> T execute(int maxAttempts, Supplier<T> callback);
}
