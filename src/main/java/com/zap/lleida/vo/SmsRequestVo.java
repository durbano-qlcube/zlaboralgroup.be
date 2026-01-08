package com.zap.lleida.vo;

import lombok.Data;

@Data
public class SmsRequestVo {
    private Sms sms;

    @Data
    public static class Sms {
        private String user;
        private Dst dst;
        private String txt;
    }

    @Data
    public static class Dst {
        private String[] num;
    }
}
