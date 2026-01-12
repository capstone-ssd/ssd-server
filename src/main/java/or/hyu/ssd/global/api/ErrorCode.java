package or.hyu.ssd.global.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    /**
     * code는 "도메인+HTTP STATUS+인덱스" 를 통해서 설정됩니다
     * 도메인을 이용하여 에러코드를 분리해주세요
     * */



    // 회원 예외
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER40101" ,"회원을 찾지 못했습니다" ),
    MEMBER_ROLE_EXCEPTION(HttpStatus.BAD_REQUEST,"MEMBER40001","존재하지 않는 인가 권한입니다"),
    KAKAO_AUTH_CODE_INVALID(HttpStatus.UNAUTHORIZED,"MEMBER40301" ,"카카오 인가코드가 올바르지 않습니다" ),
    KAKAO_ACCESSTOKEN_INVALID(HttpStatus.UNAUTHORIZED,"MEMBER40302" ,"카카오 액세스 토큰이 올바르지 않습니다" ),

    // 문서 예외
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DOC40401", "문서를 찾지 못했습니다"),
    DOCUMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "DOC40301", "해당 문서를 수정할 권한이 없습니다"),

    // 체크리스트 예외
    CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "CHK40401", "체크리스트를 찾지 못했습니다"),
    CHECKLIST_FORBIDDEN(HttpStatus.FORBIDDEN, "CHK40301", "해당 체크리스트에 접근할 권한이 없습니다"),
    CHECKLIST_CONFLICT(HttpStatus.CONFLICT, "CHK40901", "체크리스트가 동시에 수정되었습니다. 다시 시도해 주세요"),
    EVALUATOR_CHECKLIST_PARSE_ERROR(HttpStatus.BAD_REQUEST, "CHK40002", "평가자 체크리스트 응답 파싱에 실패했습니다"),

    // 요청/라우팅 예외
    REQUEST_API_NOT_FOUND(HttpStatus.NOT_FOUND, "REQ40401", "요청하신 API를 찾지 못했습니다"),
    REQUEST_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "REQ40501", "지원하지 않는 HTTP 메서드입니다"),


    // 토큰 예외
    TOKEN_SECRET_IS_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN50001","JWT SECRET KEY가 주입되지 않았습니다"),
    ACCESS_INVALID_TYPE(HttpStatus.UNAUTHORIZED,"TOKEN40301" ,"ACCESS 토큰이 헤더가 올바르지 않습니다" ),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"TOKEN40302" ,"ACCESS 토큰이 만료되었습니다" ),
    ROLE_INVALID_TYPE(HttpStatus.BAD_REQUEST,"TOKEN40001","존재하지 않는 인가 권한입니다"),
    COOKIE_NULL(HttpStatus.UNAUTHORIZED,"TOKEN40303" ,"쿠키가 비어있습니다" ),
    REFRESH_TOKEN_NULL(HttpStatus.UNAUTHORIZED,"TOKEN40304" ,"리프레시 토큰이 비어있습니다" ),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"TOKEN40305" ,"리프레시 토큰이 만료되었습니다"),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED,"TOKEN40306" ,"JWT 시그니처가 위조되었습니다" ),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"TOKEN40307" ,"유효하지 않은 토큰입니다"),

    // 요청 바디/JSON 파싱 예외
    REQUEST_BODY_INVALID_JSON(HttpStatus.BAD_REQUEST, "REQ40001", "요청 본문 JSON 파싱에 실패했습니다. 문자열의 개행은 \\n 로 이스케이프해 주세요"),



    // 서버 예외
    SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"SERVER50001","서버에서 예외가 발생하였습니다. 개발자에게 문의해주세요");


    private final HttpStatus status;
    private final String code;
    private final String message;
}

