package devanmejia.productshopauth.controller;

import devanmejia.productshopauth.security.JWTProvider;
import devanmejia.productshopauth.service.UserService;
import devanmejia.productshopauth.transfer.TokenDTO;
import devanmejia.productshopauth.transfer.TokensDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth/check")
public class CheckController {
    @Autowired
    private JWTProvider jwtProvider;
    @Autowired
    private UserService userService;

    @PostMapping("/verify-code")
    public Mono<TokensDTO> checkVerifyCode(ServerWebExchange serverWebExchange, @RequestBody String code){
        return Mono.just(jwtProvider.getLogin(serverWebExchange))
                .flatMap(login -> userService.checkVerifyCode(login, code))
                .flatMap(user -> userService.createRefreshToken(user))
                .flatMap(user -> {
                    String accessToken = jwtProvider.createToken(user.getLogin(), user.getRole());
                    String refreshToken = user.getRefreshToken();
                    return Mono.just(new TokensDTO(accessToken, refreshToken));
                });
    }

    @PostMapping("/reset-code")
    public Mono<TokenDTO> checkResetCode(ServerWebExchange serverWebExchange, @RequestBody String code){
        return Mono.just(jwtProvider.getLogin(serverWebExchange))
                .flatMap(login -> userService.checkResetCode(login, code))
                .flatMap(user -> Mono.just(jwtProvider.createToken(user.getLogin(), user.getRole())))
                .map(TokenDTO::new);
    }
}
