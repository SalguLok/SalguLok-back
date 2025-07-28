package com.salgulok.auth.service.principal;

import com.salgulok.user.repository.UserRepository;
import com.salgulok.global.exception.ErrorCode;
import com.salgulok.global.exception.SalgulokException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findById(Long.parseLong(userId))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new SalgulokException(ErrorCode.USER_NOT_FOUND));
    }
}

