package devanmejia.productshopauth.model;

import devanmejia.productshopauth.transfer.user.SignUpParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@Data
@Document("users")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    private String id;
    private String login;
    private String password;
    private Role role;
    private State state;
    private String email;
    private String refreshToken;
    private String verifyCode;
    private String resetCode;
    private Priority priority;


    public User(SignUpParam signUpParam) {
        this.login = signUpParam.getLogin();
        this.password = signUpParam.getPassword();
        this.email = signUpParam.getEmail();
        this.state = State.ACTIVE;
        this.role = Role.ROLE_UNAUTH_USER;
        this.priority = Priority.USER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.name());
        return Collections.singletonList(grantedAuthority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !state.equals(State.BANNED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return state.equals(State.ACTIVE);
    }
}
