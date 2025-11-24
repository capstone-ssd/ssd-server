package or.hyu.ssd.global.util;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class OptimisticRetryExecutor {

    private final PlatformTransactionManager transactionManager;

    public <T> T execute(int maxAttempts, Supplier<T> callback) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        Throwable lastEx = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return tt.execute(status -> callback.get());
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                lastEx = e;
            }
        }
        if (lastEx instanceof RuntimeException re) throw re;
        throw new RuntimeException(String.valueOf(lastEx));
    }
}

