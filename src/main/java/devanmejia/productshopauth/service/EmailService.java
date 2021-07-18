package devanmejia.productshopauth.service;

import devanmejia.productshopauth.model.User;
import devanmejia.productshopauth.security.JWTProvider;
import devanmejia.productshopauth.transfer.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EmailService {
    @Value("${email.service.url}")
    private String emailSenderAPI;
    @Autowired
    private WebClient webClient;
    @Autowired
    private CryptoService cryptoService;
    @Autowired
    private JWTProvider jwtProvider;

    public Mono<Void> sendResetMessage(User user, String code) {
        return sendMessage(user, code, "/reset");
    }
    public Mono<Void> sendVerifyMessage(User user, String code) {
        return sendMessage(user, code, "/verify");
    }

    private Mono<Void> sendMessage(User user, String code, String api) {
        try {
            return cryptoService.encrypt(code)
                    .map(encryptedCode -> new MessageDTO(user.getEmail(), encryptedCode))
                    .flatMap(params -> sendRequest(user, params, code, api));
        } catch (Exception e){
            return Mono.error(e);
        }
    }

    private Mono<Void> sendRequest(User user, MessageDTO params, String code, String api){
        String token = jwtProvider.createToken(user.getLogin(), user.getRole());
        return webClient.post()
                .uri(emailSenderAPI + api)
                .header(HttpHeaders.AUTHORIZATION, "Bearer_" + token)
                .body(BodyInserters.fromValue(params))
                .exchangeToMono(response -> {
                    if (response.statusCode().is5xxServerError()){
                        cryptoService.refreshPublicKey();
                        return sendMessage(user, code, api);
                    }
                    if (response.statusCode().is4xxClientError()){
                        return response.bodyToMono(String.class)
                                .flatMap(message -> Mono.error(new IllegalArgumentException(String.format("Can not send email to %s. %s", params.getEmail(), message))));
                    }
                    else
                        return Mono.empty();
                });
    }
}
