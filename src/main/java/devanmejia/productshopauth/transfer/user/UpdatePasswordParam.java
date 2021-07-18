package devanmejia.productshopauth.transfer.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordParam {
    private String login;
    private String password;
    private String newPassword;
}
