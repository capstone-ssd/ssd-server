package or.hyu.ssd.common.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class MdcScopeTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("with(String, String)은 null 값을 MDC 제거로 처리한다")
    void with_removesMdcWhenValueIsNull() {
        // given
        MDC.put(LoggingMdcKey.REQUEST_ID, "before-request-id");

        // when
        try (MdcScope ignored = MdcScope.with(LoggingMdcKey.REQUEST_ID, null)) {
            // then
            assertThat(MDC.get(LoggingMdcKey.REQUEST_ID)).isNull();
        }

        assertThat(MDC.get(LoggingMdcKey.REQUEST_ID)).isEqualTo("before-request-id");
    }
}
