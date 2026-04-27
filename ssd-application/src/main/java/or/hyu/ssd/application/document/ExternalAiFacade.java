package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.command.ExternalDocumentIdCommand;
import or.hyu.ssd.document.application.result.ExternalAiChecklistResult;
import or.hyu.ssd.document.application.result.ExternalAiDocumentCheckResult;
import or.hyu.ssd.document.application.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.document.application.result.ExternalAiHealthResult;
import or.hyu.ssd.document.application.result.ExternalAiKeywordResult;
import or.hyu.ssd.document.application.result.ExternalAiSummaryResult;
import or.hyu.ssd.document.application.service.ExternalAiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAiFacade {

    private final ExternalAiService externalAiService;

    @Transactional(readOnly = true)
    public ExternalAiHealthResult health() {
        return externalAiService.health();
    }

    @Transactional
    public ExternalAiEvaluationCardResult evaluate(ExternalDocumentIdCommand command, Long memberId) {
        return externalAiService.evaluate(command, memberId);
    }

    @Transactional
    public ExternalAiSummaryResult summarizeBasic(ExternalDocumentIdCommand command, Long memberId) {
        return externalAiService.summarizeBasic(command, memberId);
    }

    @Transactional
    public ExternalAiKeywordResult summarizeKeyword(ExternalDocumentIdCommand command, Long memberId) {
        return externalAiService.summarizeKeyword(command, memberId);
    }

    @Transactional
    public ExternalAiDocumentCheckResult checkNewText(ExternalDocumentIdCommand command, Long memberId) {
        return externalAiService.checkNewText(command, memberId);
    }

    @Transactional(readOnly = true)
    public ExternalAiEvaluationCardResult getEvaluation(Long documentId, Long memberId) {
        return externalAiService.getEvaluation(documentId, memberId);
    }

    @Transactional(readOnly = true)
    public ExternalAiSummaryResult getSummary(Long documentId, Long memberId) {
        return externalAiService.getSummary(documentId, memberId);
    }

    @Transactional(readOnly = true)
    public ExternalAiKeywordResult getKeyword(Long documentId, Long memberId) {
        return externalAiService.getKeyword(documentId, memberId);
    }

    @Transactional(readOnly = true)
    public ExternalAiChecklistResult getChecklist(Long documentId, Long memberId) {
        return externalAiService.getChecklist(documentId, memberId);
    }
}
