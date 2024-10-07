package com.draft.restapi.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.draft.restapi.auth.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthUserDetail extends User implements UserDetails {
    private static final long serialVersionUID = 1L;

	public AuthUserDetail(User user) {
        super(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        getRoles().forEach(role -> {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        });
        return grantedAuthorities;
    }
}
