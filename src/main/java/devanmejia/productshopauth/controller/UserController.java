package devanmejia.productshopauth.controller;

import devanmejia.productshopauth.security.JWTProvider;
import devanmejia.productshopauth.service.UserService;
import devanmejia.productshopauth.transfer.TokenDTO;
import devanmejia.productshopauth.transfer.TokensDTO;
import devanmejia.productshopauth.transfer.user.LogInParam;
import devanmejia.productshopauth.transfer.user.SignUpParam;
import devanmejia.productshopauth.transfer.user.UpdatePasswordParam;
import devanmejia.productshopauth.transfer.user.UserInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth/user")
public class UserController {
    @Autowired
    private JWTProvider jwtProvider;
    @Autowired
    private UserService userService;

    @PostMapping("/logIn")
    public Mono<TokenDTO> logIn(@RequestBody LogInParam logInParam){
        return userService.logInUser(logInParam)
                .flatMap(user -> Mono.just(jwtProvider.createToken(user.getLogin(), user.getRole())))
                .map(TokenDTO::new);
    }

    @PostMapping("/signUp")
    public Mono<TokenDTO> signUp(@RequestBody SignUpParam signUpParam){
        return userService.signUpUser(signUpParam)
                .flatMap(user -> Mono.just(jwtProvider.createToken(user.getLogin(), user.getRole())))
                .map(TokenDTO::new);
    }

    @PostMapping("/update")
    public Mono<Void> updatePassword(@RequestBody UpdatePasswordParam passwordParam){
        return userService.updatePassword(passwordParam).then();
    }

    @PostMapping("/reset/status")
    public Mono<TokenDTO> resetStatus(@RequestBody String login){
        return userService.resetStatus(login)
                .flatMap(user -> Mono.just(jwtProvider.createToken(user.getLogin(), user.getRole())))
                .map(TokenDTO::new);
    }

    @PostMapping("/reset/password")
    public Mono<Void> resetPassword(ServerWebExchange serverWebExchange, @RequestBody String newPassword){
        return Mono.just(jwtProvider.getLogin(serverWebExchange))
                .flatMap(login -> userService.resetPassword(login, newPassword)).then();
    }

    @PostMapping("/refresh/{login}")
    public Mono<TokensDTO> refreshTokens(@PathVariable String login, @RequestBody String refreshToken){
        return userService.updateTokens(login, refreshToken)
                .flatMap(user -> {
                    String accessToken = jwtProvider.createToken(user.getLogin(), user.getRole());
                    return Mono.just(new TokensDTO(accessToken, user.getRefreshToken()));
                });
    }

    @GetMapping("/verify-code")
    public Mono<TokenDTO> sendVerifyCodeAgain(ServerWebExchange serverWebExchange){
        return Mono.just(jwtProvider.getLogin(serverWebExchange))
                .flatMap(login -> userService.sendVerifyCodeAgain(login))
                .flatMap(user -> Mono.just(jwtProvider.createToken(user.getLogin(), user.getRole())))
                .map(TokenDTO::new);
    }

    @GetMapping("/reset-code")
    public Mono<TokenDTO> sendResetCodeAgain(ServerWebExchange serverWebExchange){
        return Mono.just(jwtProvider.getLogin(serverWebExchange))
                .flatMap(login -> userService.sendResetCodeAgain(login))
                .flatMap(user -> Mono.just(jwtProvider.createToken(user.getLogin(), user.getRole())))
                .map(TokenDTO::new);
    }

    @GetMapping("/info/{login}")
    public Mono<UserInfoDTO> getUserInfo(@PathVariable String login){
        return userService.getUserInfo(login);
    }

}
