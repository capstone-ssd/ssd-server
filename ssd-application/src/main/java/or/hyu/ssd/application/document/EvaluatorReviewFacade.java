package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.command.CreateEvaluatorReviewCommand;
import or.hyu.ssd.document.application.command.UpdateEvaluatorReviewCommand;
import or.hyu.ssd.document.application.result.EvaluatorReviewDetailResult;
import or.hyu.ssd.document.application.result.EvaluatorReviewIdResult;
import or.hyu.ssd.document.application.result.EvaluatorReviewListResult;
import or.hyu.ssd.document.application.service.EvaluatorReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvaluatorReviewFacade {

    private final EvaluatorReviewService evaluatorReviewService;

    @Transactional
    public EvaluatorReviewIdResult create(Long documentId, Long memberId, CreateEvaluatorReviewCommand command) {
        return evaluatorReviewService.create(documentId, memberId, command);
    }

    @Transactional
    public EvaluatorReviewDetailResult update(Long documentId, Long memberId, UpdateEvaluatorReviewCommand command) {
        return evaluatorReviewService.update(documentId, memberId, command);
    }

    @Transactional(readOnly = true)
    public EvaluatorReviewDetailResult getMyReview(Long documentId, Long memberId) {
        return evaluatorReviewService.getMyReview(documentId, memberId);
    }

    @Transactional(readOnly = true)
    public EvaluatorReviewListResult list(Long documentId, Long memberId) {
        return evaluatorReviewService.list(documentId, memberId);
    }

    @Transactional
    public void delete(Long documentId, Long memberId) {
        evaluatorReviewService.delete(documentId, memberId);
    }
}
