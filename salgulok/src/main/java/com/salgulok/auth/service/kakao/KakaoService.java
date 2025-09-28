package com.salgulok.auth.service.kakao;

import com.salgulok.auth.dto.response.KakaoTokenResponse;
import com.salgulok.auth.dto.summary.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    private final KakaoUtils kakaoUtils;
    private final RestClient restClient;

    public KakaoUserInfo getUserInfoFromAccessToken(String kakaoAccessToken) {
        KakaoTokenResponse accessTokenFromKakao = getAccessTokenFromKakao(kakaoAccessToken);

        String userInfoUrl = kakaoUtils.getUserInfoUrl();
        Map body = restClient.get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenFromKakao.getAccessToken())
                .retrieve()
                .body(Map.class);

        // nickname, profileImg 받지 않고 kakakoId만 필요해서 우선 이렇게 설정. 추가로 받아올 계획 있다면 dto로 분리
        Long kakaoId = ((Number) body.get("id")).longValue();

        return new KakaoUserInfo(kakaoId);
    }

    public KakaoTokenResponse getAccessTokenFromKakao(String code) {
        String tokenUrl = kakaoUtils.getTokenUrl();

        MultiValueMap<String, String> tokenRequestBody = kakaoUtils.createTokenRequestBody(code);

        return restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(tokenRequestBody)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }

}
