package devanmejia.productshopauth.transfer.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountForm {
    private String userLogin;
    private String userFirstName;
    private String userLastName;
    private Date userBirthDate;

    public static AccountForm form(SignUpParam param){
        return AccountForm.builder()
                .userBirthDate(param.getBirthDate())
                .userFirstName(param.getFirstName())
                .userLastName(param.getLastName())
                .userLogin(param.getLogin()).build();
    }
}
