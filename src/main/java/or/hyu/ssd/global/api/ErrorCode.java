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


    // 문서 예외


    // 토큰 예외
    TOKEN_SECRET_IS_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN50001","JWT SECRET KEY가 주입되지 않았습니다"),

    // 서버 예외
    SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"SERVER50001","서버에서 예외가 발생하였습니다. 개발자에게 문의해주세요");


    private final HttpStatus status;
    private final String code;
    private final String message;
}

