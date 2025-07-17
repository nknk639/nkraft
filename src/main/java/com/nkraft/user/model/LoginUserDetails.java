package com.nkraft.user.model;

import com.nkraft.user.entity.NkraftUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@RequiredArgsConstructor
@Getter
public class LoginUserDetails implements UserDetails {

    private final NkraftUser nkraftUser;

    //ユーザーに与えられている権限を返します。
    //今回は権限を使用しないため、空のコレクションを返します。
    
    //@return 権限のコレクション
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * ユーザーのパスワードを返します。
     *
     * @return パスワード
     **/
    @Override
    public String getPassword() {
        return this.nkraftUser.getPassword();
    }

    /**
     * ユーザーのユーザー名を返します。
     *
     * @return ユーザー名
     */
    @Override
    public String getUsername() {
        return this.nkraftUser.getUsername();
    }

    // アカウントの有効期限、ロック、資格情報の有効期限、有効/無効に関するメソッドは、
    // 今回のアプリケーションでは使用しないため、全て true を返します。

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
}