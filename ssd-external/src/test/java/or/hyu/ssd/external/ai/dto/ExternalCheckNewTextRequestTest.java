package or.hyu.ssd.external.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalCheckNewTextRequestTest {

    @Test
    @DisplayName("check new text 요청은 변경 블록 목록을 blocks 필드로 전송한다")
    void request_usesBlocksField() {
        assertThat(jsonPropertyValue(ExternalCheckNewTextRequest.class, "docId")).isEqualTo("doc_id");
        assertThat(jsonPropertyValue(ExternalCheckNewTextRequest.class, "blocks")).isEqualTo("blocks");
        assertThat(jsonPropertyValue(ExternalCheckNewTextBlockRequest.class, "blockId")).isEqualTo("block_id");
    }

    private String jsonPropertyValue(Class<? extends Record> recordType, String componentName) {
        RecordComponent component = Arrays.stream(recordType.getRecordComponents())
                .filter(recordComponent -> recordComponent.getName().equals(componentName))
                .findFirst()
                .orElseThrow();
        JsonProperty componentAnnotation = component.getAnnotation(JsonProperty.class);
        if (componentAnnotation != null) {
            return componentAnnotation.value();
        }
        JsonProperty accessorAnnotation = component.getAccessor().getAnnotation(JsonProperty.class);
        if (accessorAnnotation != null) {
            return accessorAnnotation.value();
        }
        try {
            Field field = recordType.getDeclaredField(componentName);
            return field.getAnnotation(JsonProperty.class).value();
        } catch (NoSuchFieldException e) {
            throw new AssertionError("record component field를 찾을 수 없습니다: " + componentName, e);
        }
    }
}
