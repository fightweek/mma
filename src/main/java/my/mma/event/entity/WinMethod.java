package my.mma.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WinMethod {

    SUB("서브미션"),
    KO_TKO("케이오/티케이오"),
    U_DEC("만장일치"),
    M_DEC("다수결"),
    S_DEC("스플릿 판정"),
    ELSE("그 외");

    private final String description;

}
