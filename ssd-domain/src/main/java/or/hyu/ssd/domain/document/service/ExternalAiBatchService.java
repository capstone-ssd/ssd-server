package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiBatchResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiKeywordResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiSummaryResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalAiBatchService {

    private final ExternalAiService externalAiService;

    public ExternalAiBatchResponse generateAll(ExternalDocumentIdRequest request, CustomUserDetails user) {
        ExternalAiEvaluationCardResponse evaluation = externalAiService.evaluate(request, user);
        ExternalAiSummaryResponse summary = externalAiService.summarizeBasic(request, user);
        ExternalAiKeywordResponse keyword = externalAiService.summarizeKeyword(request, user);
        return ExternalAiBatchResponse.of(evaluation.documentId(), evaluation, summary, keyword);
    }
}
