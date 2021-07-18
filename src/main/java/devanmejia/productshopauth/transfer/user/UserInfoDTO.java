package devanmejia.productshopauth.transfer.user;

import devanmejia.productshopauth.model.Role;
import devanmejia.productshopauth.model.State;
import devanmejia.productshopauth.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String login;
    private Role role;
    private State state;

    public static UserInfoDTO form(User user){
        return UserInfoDTO.builder()
                .login(user.getLogin())
                .role(user.getRole())
                .state(user.getState()).build();
    }
}
