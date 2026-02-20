package com.kt.onrace.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.BusinessErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER));

        return CustomUserDetails.from(user);
    }
}