package devanmejia.productshopauth.service;

import devanmejia.productshopauth.transfer.user.AccountForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AccountService {
    @Autowired
    private WebClient webClient;
    @Value("${system.service.url}")
    private String systemAPI;

    public Mono<AccountForm> registerNewUserAccount(AccountForm accountForm){
        return webClient.post()
                .uri(systemAPI + "/account")
                .body(BodyInserters.fromValue(accountForm))
                .exchangeToMono(response -> {
                    if (!response.statusCode().is2xxSuccessful()){
                        return response.bodyToMono(String.class)
                                .flatMap(message -> Mono.error(new IllegalArgumentException(message)));
                    }
                    else{
                        return Mono.just(accountForm);
                    }
                });
    }
}
