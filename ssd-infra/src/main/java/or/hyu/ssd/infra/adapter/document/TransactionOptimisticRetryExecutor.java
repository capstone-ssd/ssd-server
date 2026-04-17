package or.hyu.ssd.infra.adapter.document;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.support.OptimisticRetryExecutor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TransactionOptimisticRetryExecutor implements OptimisticRetryExecutor {

    private final PlatformTransactionManager transactionManager;

    @Override
    public <T> T execute(int maxAttempts, Supplier<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Throwable lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return transactionTemplate.execute(status -> callback.get());
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException exception) {
                lastException = exception;
                if (attempt < maxAttempts) {
                    sleepBackoff(attempt);
                }
            }
        }

        if (lastException instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException(String.valueOf(lastException));
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(50L * attempt);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interruptedException);
        }
    }
}
