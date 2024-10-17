package com.security.jwt_spring_security_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        return new OAuth2UserPrincipal(oAuth2User, authenticationService.processOAuth2User(new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), oAuth2UserRequest.getClientRegistration().getRegistrationId())));
    }
}
