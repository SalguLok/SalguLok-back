package com.salgulok.user.service;

import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import com.salgulok.log.domain.Log;
import com.salgulok.log.repository.LogRepository;
import com.salgulok.user.domain.User;
import com.salgulok.user.dto.request.UserInfoRequest;
import com.salgulok.user.dto.request.NicknameRequest;
import com.salgulok.user.dto.response.IsTravelingResponse;
import com.salgulok.user.dto.response.UserResponse;
import com.salgulok.user.dto.response.UsernameDuplicateResponse;
import com.salgulok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final LogRepository logRepository;

    @Transactional(readOnly = true)
    public UserResponse getMyInfo(User user) {
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getOtherInfo(String nickname) {
        User findUser = userRepository.findByUsername(nickname)
                .orElseThrow(() -> new SalgulokException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(findUser);
    }

    @Transactional
    public UserResponse updateUserProfile(User user, UserInfoRequest request) {
        User findUser = findByUserId(user.getUserId());
        findUser.updateUserInfo(request.getUsername(), request.getIntro(), request.getProfileImg());
        return UserResponse.from(findUser);
    }

    @Transactional
    public UserResponse createUserInfo(User user, UserInfoRequest request) {
        if(user.getUsername() != null && user.getUsername().isEmpty()){
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

    @Transactional(readOnly = true)
    public IsTravelingResponse checkIfTraveling(User user) {
        Log currentTravelLog = logRepository.findCurrentTravelLog(user.getUserId(), LocalDate.now())
                .orElse(null);
        if(currentTravelLog == null){
            return new IsTravelingResponse(false, null, null);
        }
        return new IsTravelingResponse(true, currentTravelLog.getLogId(), currentTravelLog.getRegion().getRegionId());
    }

    private User findByUserId(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new SalgulokException(ErrorCode.USER_NOT_FOUND));
    }
}
