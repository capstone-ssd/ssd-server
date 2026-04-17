package or.hyu.ssd.document.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.EvaluatorReview;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.document.application.command.CreateEvaluatorReviewCommand;
import or.hyu.ssd.document.application.command.UpdateEvaluatorReviewCommand;
import or.hyu.ssd.document.application.result.EvaluatorReviewDetailResult;
import or.hyu.ssd.document.application.result.EvaluatorReviewListResult;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.member.domain.entity.Role;
import or.hyu.ssd.member.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluatorReviewServiceTest {

    @Mock
    private EvaluatorReviewRepository evaluatorReviewRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private EvaluatorReviewService evaluatorReviewService;

    @Test
    @DisplayName("이미 작성한 리뷰가 있으면 생성은 409 예외를 던진다")
    void create_throwsConflictWhenReviewAlreadyExists() {
        // given
        Member member = member(1L, "reviewer@example.com");
        Document document = document(10L, member(2L, "owner@example.com"));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(evaluatorReviewRepository.existsByDocumentAndReviewer(document, member)).thenReturn(true);

        // when
        // then
        assertThatThrownBy(() -> evaluatorReviewService.create(
                10L,
                member.getId(),
                new CreateEvaluatorReviewCommand(80, 90, 70, "좋은 사업입니다")
        ))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("이미 작성한 리뷰가 존재합니다");
    }

    @Test
    @DisplayName("리뷰 생성은 null 요청 본문을 거부한다")
    void create_rejectsNullCommand() {
        // given
        Member member = member(1L, "reviewer@example.com");

        // when
        // then
        assertThatThrownBy(() -> evaluatorReviewService.create(10L, member.getId(), null))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("리뷰 요청 본문이 비어 있습니다");
    }

    @Test
    @DisplayName("리뷰 수정은 기존 리뷰를 갱신하고 문서 평균을 재집계한다")
    void update_recalculatesAverages() {
        // given
        Member owner = member(1L, "owner@example.com");
        Member reviewer = member(2L, "reviewer@example.com");
        Document document = document(10L, owner);
        EvaluatorReview review = EvaluatorReview.of(60, 70, 80, "초기 의견", document, reviewer);
        EvaluatorReview other = EvaluatorReview.of(90, 90, 90, "다른 의견", document, member(3L, "third@example.com"));

        // when
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(memberRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(evaluatorReviewRepository.findByDocumentAndReviewer(document, reviewer)).thenReturn(Optional.of(review));
        when(evaluatorReviewRepository.findAllByDocument(document)).thenReturn(List.of(review, other));
        when(evaluatorReviewRepository.save(any(EvaluatorReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // then
        EvaluatorReviewDetailResult response = evaluatorReviewService.update(
                10L,
                reviewer.getId(),
                new UpdateEvaluatorReviewCommand(100, 80, 60, "수정 의견")
        );

        assertThat(response.feasibility()).isEqualTo(100);
        assertThat(response.comment()).isEqualTo("수정 의견");
        assertThat(document.getReviewFeasibilityAvg()).isEqualTo(95.0);
        assertThat(document.getReviewDifferentiationAvg()).isEqualTo(85.0);
        assertThat(document.getReviewFinancialAvg()).isEqualTo(75.0);
        assertThat(document.getReviewTotalAvg()).isEqualTo(85.0);
        assertThat(document.getReviewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("리뷰 수정은 null 요청 본문을 거부한다")
    void update_rejectsNullCommand() {
        // given
        Member reviewer = member(2L, "reviewer@example.com");

        // when
        // then
        assertThatThrownBy(() -> evaluatorReviewService.update(10L, reviewer.getId(), null))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("리뷰 요청 본문이 비어 있습니다");
    }

    @Test
    @DisplayName("리뷰 삭제 후 남은 리뷰가 없으면 평균값은 0으로 초기화된다")
    void delete_resetsSummaryToZeroWhenNoReviewsRemain() {
        // given
        Member owner = member(1L, "owner@example.com");
        Member reviewer = member(2L, "reviewer@example.com");
        Document document = document(10L, owner);
        EvaluatorReview review = EvaluatorReview.of(60, 70, 80, "초기 의견", document, reviewer);

        // when
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(memberRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(evaluatorReviewRepository.findByDocumentAndReviewer(document, reviewer)).thenReturn(Optional.of(review));
        when(evaluatorReviewRepository.findAllByDocument(document)).thenReturn(List.of());

        // then
        evaluatorReviewService.delete(10L, reviewer.getId());

        verify(evaluatorReviewRepository).delete(review);
        assertThat(document.getReviewFeasibilityAvg()).isEqualTo(0.0);
        assertThat(document.getReviewDifferentiationAvg()).isEqualTo(0.0);
        assertThat(document.getReviewFinancialAvg()).isEqualTo(0.0);
        assertThat(document.getReviewTotalAvg()).isEqualTo(0.0);
        assertThat(document.getReviewCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("문서 리뷰 목록 조회는 문서 작성자만 가능하다")
    void list_isOwnerOnly() {
        // given
        Member owner = member(1L, "owner@example.com");
        Member other = member(2L, "other@example.com");
        Document document = document(10L, owner);

        // when
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(memberRepository.findById(other.getId())).thenReturn(Optional.of(other));

        // then
        assertThatThrownBy(() -> evaluatorReviewService.list(10L, other.getId()))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("해당 문서를 수정할 권한이 없습니다");
    }

    @Test
    @DisplayName("문서 리뷰 목록 조회는 문서 캐시값이 stale여도 실데이터 기준 평균을 반환한다")
    void list_returnsSummaryAndItems() {
        // given
        Member owner = member(1L, "owner@example.com");
        Document document = document(10L, owner);
        document.updateReviewSummary(100.0, 100.0, 100.0, 100.0, 1);
        EvaluatorReview first = EvaluatorReview.of(80, 70, 60, "의견1", document, member(2L, "first@example.com"));
        EvaluatorReview second = EvaluatorReview.of(90, 80, 70, "의견2", document, member(3L, "second@example.com"));

        // when
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(evaluatorReviewRepository.findAllByDocumentOrderByUpdatedAtDesc(document)).thenReturn(List.of(first, second));

        // then
        EvaluatorReviewListResult response = evaluatorReviewService.list(10L, owner.getId());

        assertThat(response.documentId()).isEqualTo(10L);
        assertThat(response.averageTotalScore()).isEqualTo(75.0);
        assertThat(response.reviewCount()).isEqualTo(2);
        assertThat(response.reviews()).hasSize(2);
        assertThat(response.reviews().get(0).reviewId()).isNull();
        assertThat(response.reviews().get(0).reviewerName()).isEqualTo("사용자2");
        assertThat(response.reviews().get(0).reviewerEmail()).isEqualTo("first@example.com");
        assertThat(response.reviews().get(0).feasibility()).isEqualTo(80);
        assertThat(response.reviews().get(0).differentiation()).isEqualTo(70);
        assertThat(response.reviews().get(0).financial()).isEqualTo(60);
        assertThat(response.reviews().get(0).totalScore()).isEqualTo(70.0);
        assertThat(response.reviews().get(0).comment()).isEqualTo("의견1");
    }

    private Document document(Long id, Member owner) {
        return Document.builder()
                .id(id)
                .title("문서")
                .content("본문")
                .member(owner)
                .bookmark(false)
                .build();
    }

    private Member member(Long id, String email) {
        return Member.builder()
                .id(id)
                .name("사용자" + id)
                .email(email)
                .profileImageUrl("")
                .profileImageKey(null)
                .role(Role.ROLE_AUTHOR)
                .build();
    }
}
