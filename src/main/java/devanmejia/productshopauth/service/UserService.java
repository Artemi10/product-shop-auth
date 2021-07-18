package devanmejia.productshopauth.service;

import devanmejia.productshopauth.model.Priority;
import devanmejia.productshopauth.model.Role;
import devanmejia.productshopauth.model.User;
import devanmejia.productshopauth.repository.UserRepository;
import devanmejia.productshopauth.transfer.user.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    @Autowired
    private AccountService accountService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    public Mono<User> logInUser(LogInParam logInParam){
        String login = logInParam.getLogin();
        return userRepository.getUserByLogin(login)
                .filter(user -> encoder.matches(logInParam.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Login and password combination is incorrect")))
                .flatMap(user -> saveVerifyCode(user, RandomStringUtils.randomAlphanumeric(6)));
    }

    public Mono<User> signUpUser(SignUpParam signUpParam){
        String login = signUpParam.getLogin();
        return userRepository.getUserByLogin(login)
                .flatMap(user -> Mono.error(new IllegalArgumentException(String.format("%s has already been registered", login))))
                .switchIfEmpty(
                        accountService.registerNewUserAccount(AccountForm.form(signUpParam))
                                .flatMap(account -> saveSignUpUser(signUpParam))
                                .flatMap(user -> saveVerifyCode(user, RandomStringUtils.randomAlphanumeric(6)))).cast(User.class);
    }
    private Mono<User> saveSignUpUser(SignUpParam signUpParam){
        String encodedPassword = encoder.encode(signUpParam.getPassword());
        signUpParam.setPassword(encodedPassword);
        return userRepository.save(new User(signUpParam));
    }

    public Mono<User> updatePassword(UpdatePasswordParam updatePasswordParam){
        return userRepository.getUserByLogin(updatePasswordParam.getLogin())
                .filter(user -> encoder.matches(updatePasswordParam.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Login and password combination is incorrect")))
                .flatMap(user -> {
                    user.setPassword(encoder.encode(updatePasswordParam.getNewPassword()));
                    return userRepository.save(user);
                });
    }

    public Mono<User> resetStatus(String login){
        return userRepository.getUserByLogin(login)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Login is incorrect")))
                .flatMap(user -> saveResetCode(user, RandomStringUtils.randomAlphanumeric(8)));
    }

    public Mono<User> resetPassword(String login, String newPassword){
        return userRepository.getUserByLogin(login)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Login is incorrect")))
                .flatMap(user -> {
                    user.setPassword(encoder.encode(newPassword));
                    user.setRole(Role.ROLE_USER);
                    return userRepository.save(user);
                });
    }

    private Mono<User> saveResetCode(User user, String code){
        user.setResetCode(encoder.encode(code));
        user.setRole(Role.ROLE_RESET_USER);
        return emailService.sendResetMessage(user, code)
                .then(userRepository.save(user));
    }

    private Mono<User> saveVerifyCode(User user, String code){
        user.setVerifyCode(encoder.encode(code));
        user.setRole(Role.ROLE_UNAUTH_USER);
        return emailService.sendVerifyMessage(user, code)
                .then(userRepository.save(user));
    }

    public Mono<User> checkVerifyCode(String login, String code){
        return userRepository.getUserByLogin(login)
                .filter(user -> encoder.matches(code, user.getVerifyCode()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Verify code is invalid")))
                .flatMap(user -> {
                    if (user.getPriority().equals(Priority.USER)){
                        user.setRole(Role.ROLE_USER);
                    }
                    else if (user.getPriority().equals(Priority.ADMIN)){
                        user.setRole(Role.ROLE_ADMIN);
                    }
                    return userRepository.save(user);
                });
    }
    public Mono<User> checkResetCode(String login, String code){
        return userRepository.getUserByLogin(login)
                .filter(user -> encoder.matches(code, user.getResetCode()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Reset code is invalid")))
                .flatMap(user -> {
                    user.setRole(Role.ROLE_CHANGE_PASSWORD);
                    return userRepository.save(user);
                });
    }

    public Mono<User> updateTokens(String login, String refreshToken){
        return userRepository.getUserByLogin(login)
                .filter(user -> user.getRefreshToken().equals(refreshToken) && user.getRole().equals(Role.ROLE_USER))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Refresh token is invalid")))
                .flatMap(this::createRefreshToken);
    }

    public Mono<User> createRefreshToken(User user){
        user.setRefreshToken(RandomStringUtils.randomAlphanumeric(22));
        return userRepository.save(user);
    }

    public Mono<User> sendVerifyCodeAgain(String login){
        return userRepository.getUserByLogin(login)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Login is incorrect")))
                .flatMap(user -> saveVerifyCode(user, RandomStringUtils.randomAlphanumeric(6)));
    }

    public Mono<User> sendResetCodeAgain(String login){
        return userRepository.getUserByLogin(login)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Login is incorrect")))
                .flatMap(user -> saveResetCode(user, RandomStringUtils.randomAlphanumeric(8)));
    }

    public Mono<UserInfoDTO> getUserInfo(String login){
        return userRepository.getUserByLogin(login)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Login is incorrect")))
                .map(UserInfoDTO::form);
    }
}
