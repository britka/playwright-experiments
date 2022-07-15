package com.provectus;

import io.github.rushuat.ocell.annotation.FieldExclude;
import io.github.rushuat.ocell.annotation.FieldName;
import lombok.Data;

@Data
public class TimeStat {
    @FieldExclude
    public static String SHEET_NAME = "Stats";

    @FieldName("PW + Selenide")
    private long pwPlusSelenide;

    @FieldName("PW pure")
    private long pwPure;

    @FieldName("Selenide pure")
    private long selenidePure;

    @FieldName("Selenide js input")
    private long selenideJSInput;

    @FieldName("Selenide over debug port")
    private long selenideOverDebugPort;
}
