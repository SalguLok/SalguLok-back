package com.salgulok.auth.service.kakao;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@Getter
public class KakaoUtils {
    private final String clientId;
    private final String redirectUri;
    private final String tokenUrl;
    private final String userInfoUrl;

    public KakaoUtils(@Value("${kakao.client-id}") String clientId,
                      @Value("${kakao.redirect-uri}") String redirectUri,
                      @Value("${kakao.token-uri}") String tokenUri,
                      @Value("${kakao.user-info-uri}") String userInfoUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.tokenUrl = tokenUri;
        this.userInfoUrl = userInfoUri;
    }

    public MultiValueMap<String, String> createTokenRequestBody(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", this.clientId);
        body.add("redirect_uri", this.redirectUri);
        body.add("code", code);
        return body;
    }

}
