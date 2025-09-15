package com.salgulok.user.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.user.domain.User;
import com.salgulok.user.dto.request.UserInfoRequest;
import com.salgulok.user.dto.request.NicknameRequest;
import com.salgulok.user.dto.response.UserResponse;
import com.salgulok.user.dto.response.UsernameDuplicateResponse;
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
    public UserResponse getMyInfo(User user) {
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUserProfile(User user, UserInfoRequest request) {
        User findUser = findByUserId(user.getUserId());
        findUser.updateUserInfo(request.getUsername(), request.getIntro(), request.getProfileImg());
        return UserResponse.from(findUser);
    }

    @Transactional
    public UserResponse createUserInfo(User user, UserInfoRequest request) {
        if(user.getIntro() != null || user.getProfileImg() != null || user.getUsername() != null){
            throw new SalgulokException(ErrorCode.USER_INFO_EXIST);
        }
        User findUser = findByUserId(user.getUserId());
        findUser.updateUserInfo(request.getUsername(), request.getIntro(), request.getProfileImg());
        return UserResponse.from(findUser);
    }

    @Transactional(readOnly = true)
    public UsernameDuplicateResponse checkUsernameDuplicate(NicknameRequest request) {
        boolean isDuplicate = userRepository.existsByUsername(request.getUsername());
        return new UsernameDuplicateResponse(isDuplicate);
    }

    private User findByUserId(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.USER_NOT_FOUND));
    }
}
