package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.command.ExternalDocumentIdCommand;
import or.hyu.ssd.document.application.result.ExternalAiBatchResult;
import or.hyu.ssd.document.application.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.document.application.result.ExternalAiKeywordResult;
import or.hyu.ssd.document.application.result.ExternalAiSummaryResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalAiBatchService {

    private final ExternalAiService externalAiService;

    public ExternalAiBatchResult generateAll(ExternalDocumentIdCommand command, Long memberId) {
        ExternalAiEvaluationCardResult evaluation = externalAiService.evaluate(command, memberId);
        ExternalAiSummaryResult summary = externalAiService.summarizeBasic(command, memberId);
        ExternalAiKeywordResult keyword = externalAiService.summarizeKeyword(command, memberId);
        return ExternalAiBatchResult.of(evaluation.documentId(), evaluation, summary, keyword);
    }
}
