package org.example.autopark.simpleuser;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SimpleUserDetails implements UserDetails {
    private final SimpleUser simpleUser;

    public SimpleUserDetails(SimpleUser simpleUser) {
        this.simpleUser = simpleUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.simpleUser.getPassword();
    }

    @Override
    public String getUsername() {
        return this.simpleUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    public SimpleUser getSimpleUser(){
        return this.simpleUser;
    }
}
