package devanmejia.productshopauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

@Service
public class CryptoService {
    private static final String ALGORITHM = "RSA";
    @Autowired
    private WebClient webClient;
    @Value("${email.service.url}")
    private String emailSenderAPI;
    private PublicKey publicKey;

    @PostConstruct
    public void refreshPublicKey(){
        getPublicKey().map(this::parseToPublicKey)
                .subscribe(publicKey -> this.publicKey = publicKey);
    }

    private Mono<String> getPublicKey(){
        return webClient.get()
                .uri(emailSenderAPI + "/crypto/key")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class,
                        e -> Mono.error(new IllegalArgumentException("Can not get public key. " + e.getMessage())));
    }

    private PublicKey parseToPublicKey(String strKey){
        byte[] encoded = DatatypeConverter.parseHexBinary(strKey);
        X509EncodedKeySpec  keySpec = new X509EncodedKeySpec(encoded);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Can not parse key. " + e.getMessage());
        }
    }

    public Mono<byte[]> encrypt(String message) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Mono.just(cipher.doFinal(message.getBytes()));

    }
}
