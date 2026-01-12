package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewItemResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewRequest;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewSummaryResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorReview;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EvaluatorReviewService {

    private final EvaluatorReviewRepository evaluatorReviewRepository;
    private final DocumentRepository documentRepository;

    /**
     * 리뷰 저장/수정 + 전체 평균 계산
     */
    public EvaluatorReviewResponse submit(Long documentId, CustomUserDetails user, EvaluatorReviewRequest request) {
        Document doc = getDocument(documentId);
        Member reviewer = getReviewer(user);

        // 세가지 항목에 대한 점수를 가져옵니다
        int feasibility = request.feasibility();
        int differentiation = request.differentiation();
        int financial = request.financial();

        // 점수가 예상 범위 내에 존재하는지 검증합니다
        validateScore(feasibility);
        validateScore(differentiation);
        validateScore(financial);

        // 기존의 평가가 존재하면 업데이트, 처음이라면 생성합니다
        Optional<EvaluatorReview> existing = evaluatorReviewRepository.findByDocumentAndReviewer(doc, reviewer);
        EvaluatorReview saved = existing
                .map(r -> updateReview(r, feasibility, differentiation, financial, request.comment()))
                .orElseGet(() -> createReview(doc, reviewer, feasibility, differentiation, financial, request.comment()));

        evaluatorReviewRepository.save(saved);

        // 문서에 대한 모든 리뷰를 가져와서 모든 리뷰에 대한 평균을 계산합니다
        List<EvaluatorReview> reviews = evaluatorReviewRepository.findAllByDocument(doc);
        applyAverages(doc, reviews);

        List<EvaluatorReviewItemResponse> reviewResponses = reviews.stream()
                .map(EvaluatorReviewItemResponse::of)
                .collect(Collectors.toList());
        EvaluatorReviewSummaryResponse summary = toSummary(doc, reviews.size());

        return EvaluatorReviewResponse.of(summary, reviewResponses);
    }

    /**
     * 리뷰 목록 + 평균 조회
     */
    @Transactional(readOnly = true)
    public EvaluatorReviewResponse get(Long documentId, CustomUserDetails user) {
        Document doc = getDocument(documentId);
        ensureReadable(doc, user);

        List<EvaluatorReview> reviews = evaluatorReviewRepository.findAllByDocument(doc);
        List<EvaluatorReviewItemResponse> reviewResponses = reviews.stream()
                .map(EvaluatorReviewItemResponse::of)
                .collect(Collectors.toList());
        EvaluatorReviewSummaryResponse summary = toSummary(doc, reviews.size());

        return EvaluatorReviewResponse.of(summary, reviewResponses);
    }

    private EvaluatorReview createReview(Document doc, Member reviewer, int feasibility, int differentiation, int financial, String comment) {
        return EvaluatorReview.of(feasibility, differentiation, financial, comment, doc, reviewer);
    }

    private EvaluatorReview updateReview(EvaluatorReview review, int feasibility, int differentiation, int financial, String comment) {
        review.updateScores(feasibility, differentiation, financial, comment);
        return review;
    }

    /**
     * 하나의 문서에 대해서 평균값을 계산하는 헬퍼 메서드입니다
     * */
    private void applyAverages(Document doc, List<EvaluatorReview> reviews) {
        if (reviews.isEmpty()) {
            doc.updateReviewSummary(null, null, null, null, 0);
            return;
        }

        double feasibilityAvg = reviews.stream().mapToInt(EvaluatorReview::getScoreFeasibility).average().orElse(0);
        double differentiationAvg = reviews.stream().mapToInt(EvaluatorReview::getScoreDifferentiation).average().orElse(0);
        double financialAvg = reviews.stream().mapToInt(EvaluatorReview::getScoreFinancial).average().orElse(0);
        double totalAvg = (feasibilityAvg + differentiationAvg + financialAvg) / 3.0;

        doc.updateReviewSummary(feasibilityAvg, differentiationAvg, financialAvg, totalAvg, reviews.size());
    }

    private EvaluatorReviewSummaryResponse toSummary(Document doc, int reviewCount) {
        double feasibility = doc.getReviewFeasibilityAvg() != null ? doc.getReviewFeasibilityAvg() : 0.0;
        double differentiation = doc.getReviewDifferentiationAvg() != null ? doc.getReviewDifferentiationAvg() : 0.0;
        double financial = doc.getReviewFinancialAvg() != null ? doc.getReviewFinancialAvg() : 0.0;
        double total = doc.getReviewTotalAvg() != null ? doc.getReviewTotalAvg() : 0.0;
        int count = doc.getReviewCount() != null ? doc.getReviewCount() : reviewCount;
        return EvaluatorReviewSummaryResponse.of(doc.getId(), feasibility, differentiation, financial, total, count);
    }

    private void validateScore(int score) {
        if (score < 0 || score > 100) {
            throw new UserExceptionHandler(ErrorCode.REVIEW_INVALID_SCORE);
        }
    }

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private Member getReviewer(CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
        }
        return user.getMember();
    }

    private void ensureReadable(Document doc, CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
        }
        Member member = user.getMember();
        boolean isOwner = doc.getMember() != null && doc.getMember().getId().equals(member.getId());
        boolean hasReview = evaluatorReviewRepository.existsByDocumentAndReviewer(doc, member);
        if (!isOwner && !hasReview) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
    }
}
