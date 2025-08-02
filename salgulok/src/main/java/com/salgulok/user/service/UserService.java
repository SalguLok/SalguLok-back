package com.salgulok.user.service;

import com.salgulok.user.domain.User;
import com.salgulok.user.dto.response.UserInfoResponse;
import com.salgulok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(User user) {
        userRepository.findById(user.getUserId());
        return UserInfoResponse.from(user);
    }
}
