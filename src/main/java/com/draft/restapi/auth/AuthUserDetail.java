package com.draft.restapi.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.draft.restapi.auth.entity.SignedUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthUserDetail extends SignedUser implements UserDetails {

	private static final long serialVersionUID = -7568522007172192358L;

	public AuthUserDetail(SignedUser user) {
        super(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        
        // TODO for hasRole
        
        return grantedAuthorities;
    }
    
    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }
}
