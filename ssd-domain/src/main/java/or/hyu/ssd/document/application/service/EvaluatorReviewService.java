package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.EvaluatorReview;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.document.application.command.CreateEvaluatorReviewCommand;
import or.hyu.ssd.document.application.command.UpdateEvaluatorReviewCommand;
import or.hyu.ssd.document.application.result.EvaluatorReviewDetailResult;
import or.hyu.ssd.document.application.result.EvaluatorReviewIdResult;
import or.hyu.ssd.document.application.result.EvaluatorReviewListItemResult;
import or.hyu.ssd.document.application.result.EvaluatorReviewListResult;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.member.repository.MemberRepository;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluatorReviewService {

    private final EvaluatorReviewRepository evaluatorReviewRepository;
    private final DocumentRepository documentRepository;
    private final MemberRepository memberRepository;

    public EvaluatorReviewIdResult create(Long documentId, Long memberId, CreateEvaluatorReviewCommand command) {
        validateCommand(command);
        Document document = getDocument(documentId);
        Member reviewer = getReviewer(memberId);

        if (evaluatorReviewRepository.existsByDocumentAndReviewer(document, reviewer)) {
            throw new DocumentException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        EvaluatorReview review = EvaluatorReview.of(
                command.feasibility(),
                command.differentiation(),
                command.financial(),
                command.comment(),
                document,
                reviewer
        );
        EvaluatorReview saved = evaluatorReviewRepository.save(review);
        recalculateAverages(document);
        return EvaluatorReviewIdResult.of(saved.getId());
    }

    public EvaluatorReviewDetailResult update(Long documentId, Long memberId, UpdateEvaluatorReviewCommand command) {
        validateCommand(command);
        Document document = getDocument(documentId);
        Member reviewer = getReviewer(memberId);
        EvaluatorReview review = evaluatorReviewRepository.findByDocumentAndReviewer(document, reviewer)
                .orElseThrow(() -> new DocumentException(ErrorCode.REVIEW_NOT_FOUND));

        review.updateScores(
                command.feasibility(),
                command.differentiation(),
                command.financial(),
                command.comment()
        );
        evaluatorReviewRepository.save(review);
        recalculateAverages(document);
        return EvaluatorReviewDetailResult.of(review);
    }

    public EvaluatorReviewDetailResult getMyReview(Long documentId, Long memberId) {
        Document document = getDocument(documentId);
        Member reviewer = getReviewer(memberId);
        EvaluatorReview review = evaluatorReviewRepository.findByDocumentAndReviewer(document, reviewer)
                .orElseThrow(() -> new DocumentException(ErrorCode.REVIEW_NOT_FOUND));
        return EvaluatorReviewDetailResult.of(review);
    }

    public EvaluatorReviewListResult list(Long documentId, Long memberId) {
        Document document = getOwnedDocument(documentId, memberId);
        List<EvaluatorReview> reviews = evaluatorReviewRepository.findAllByDocumentOrderByUpdatedAtDesc(document);
        List<EvaluatorReviewListItemResult> items = reviews.stream()
                .map(EvaluatorReviewListItemResult::of)
                .toList();

        double averageTotalScore = reviews.stream()
                .mapToDouble(EvaluatorReview::getScoreTotal)
                .average()
                .orElse(0.0);
        int reviewCount = reviews.size();

        return EvaluatorReviewListResult.of(document.getId(), averageTotalScore, reviewCount, items);
    }

    public void delete(Long documentId, Long memberId) {
        Document document = getDocument(documentId);
        Member reviewer = getReviewer(memberId);
        EvaluatorReview review = evaluatorReviewRepository.findByDocumentAndReviewer(document, reviewer)
                .orElseThrow(() -> new DocumentException(ErrorCode.REVIEW_NOT_FOUND));

        evaluatorReviewRepository.delete(review);
        recalculateAverages(document);
    }

    private void recalculateAverages(Document document) {
        List<EvaluatorReview> reviews = evaluatorReviewRepository.findAllByDocument(document);
        if (reviews.isEmpty()) {
            document.updateReviewSummary(0.0, 0.0, 0.0, 0.0, 0);
            return;
        }

        double feasibilityAvg = reviews.stream().mapToInt(EvaluatorReview::getScoreFeasibility).average().orElse(0.0);
        double differentiationAvg = reviews.stream().mapToInt(EvaluatorReview::getScoreDifferentiation).average().orElse(0.0);
        double financialAvg = reviews.stream().mapToInt(EvaluatorReview::getScoreFinancial).average().orElse(0.0);
        double totalAvg = reviews.stream().mapToDouble(EvaluatorReview::getScoreTotal).average().orElse(0.0);

        document.updateReviewSummary(feasibilityAvg, differentiationAvg, financialAvg, totalAvg, reviews.size());
    }

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private Document getOwnedDocument(Long documentId, Long memberId) {
        Document document = getDocument(documentId);
        Member member = getReviewer(memberId);
        if (document.getMember() == null || !document.getMember().getId().equals(member.getId())) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return document;
    }

    private Member getReviewer(Long memberId) {
        if (memberId == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new DocumentException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateCommand(CreateEvaluatorReviewCommand command) {
        if (command == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "리뷰 요청 본문이 비어 있습니다");
        }
    }

    private void validateCommand(UpdateEvaluatorReviewCommand command) {
        if (command == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "리뷰 요청 본문이 비어 있습니다");
        }
    }
}
