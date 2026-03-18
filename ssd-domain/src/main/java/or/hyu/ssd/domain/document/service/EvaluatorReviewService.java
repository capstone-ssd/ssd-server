package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewCreateRequest;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewDetailResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewIdResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewListResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewUpdateRequest;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorReview;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.DocumentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EvaluatorReviewService {

    private final EvaluatorReviewRepository evaluatorReviewRepository;
    private final DocumentRepository documentRepository;

    public EvaluatorReviewIdResponse create(Long documentId, CustomUserDetails user, EvaluatorReviewCreateRequest request) {
        Document document = getDocument(documentId);
        Member reviewer = getReviewer(user);

        if (evaluatorReviewRepository.existsByDocumentAndReviewer(document, reviewer)) {
            throw new DocumentException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        EvaluatorReview review = EvaluatorReview.of(
                request.feasibility(),
                request.differentiation(),
                request.financial(),
                request.comment(),
                document,
                reviewer
        );
        EvaluatorReview saved = evaluatorReviewRepository.save(review);
        recalculateAverages(document);
        return EvaluatorReviewIdResponse.of(saved.getId());
    }

    public EvaluatorReviewDetailResponse update(Long documentId, CustomUserDetails user, EvaluatorReviewUpdateRequest request) {
        Document document = getDocument(documentId);
        Member reviewer = getReviewer(user);
        EvaluatorReview review = evaluatorReviewRepository.findByDocumentAndReviewer(document, reviewer)
                .orElseThrow(() -> new DocumentException(ErrorCode.REVIEW_NOT_FOUND));

        review.updateScores(
                request.feasibility(),
                request.differentiation(),
                request.financial(),
                request.comment()
        );
        evaluatorReviewRepository.save(review);
        recalculateAverages(document);
        return EvaluatorReviewDetailResponse.of(review);
    }

    @Transactional(readOnly = true)
    public EvaluatorReviewDetailResponse getMyReview(Long documentId, CustomUserDetails user) {
        Document document = getDocument(documentId);
        Member reviewer = getReviewer(user);
        EvaluatorReview review = evaluatorReviewRepository.findByDocumentAndReviewer(document, reviewer)
                .orElseThrow(() -> new DocumentException(ErrorCode.REVIEW_NOT_FOUND));
        return EvaluatorReviewDetailResponse.of(review);
    }

    @Transactional(readOnly = true)
    public EvaluatorReviewListResponse list(Long documentId, CustomUserDetails user) {
        Document document = getOwnedDocument(documentId, user);
        List<EvaluatorReview> reviews = evaluatorReviewRepository.findAllByDocumentOrderByUpdatedAtDesc(document);
        List<EvaluatorReviewListItemResponse> items = reviews.stream()
                .map(EvaluatorReviewListItemResponse::of)
                .toList();

        double averageTotalScore = reviews.stream()
                .mapToDouble(EvaluatorReview::getScoreTotal)
                .average()
                .orElse(0.0);
        int reviewCount = reviews.size();

        return EvaluatorReviewListResponse.of(document.getId(), averageTotalScore, reviewCount, items);
    }

    public void delete(Long documentId, CustomUserDetails user) {
        Document document = getDocument(documentId);
        Member reviewer = getReviewer(user);
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

    private Document getOwnedDocument(Long documentId, CustomUserDetails user) {
        Document document = getDocument(documentId);
        Member member = getReviewer(user);
        if (document.getMember() == null || !document.getMember().getId().equals(member.getId())) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return document;
    }

    private Member getReviewer(CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return user.getMember();
    }
}
