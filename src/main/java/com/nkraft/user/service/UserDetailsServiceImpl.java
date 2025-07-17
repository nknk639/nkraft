package com.nkraft.user.service;


import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nkraft.user.model.LoginUserDetails;
import com.nkraft.user.repository.NkraftUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final NkraftUserRepository nkraftUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return nkraftUserRepository.findByUsername(username)
            .map(user -> new LoginUserDetails(user)) // LoginUserDetailsを返すように変更
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}