package devanmejia.productshopauth.security;

import devanmejia.productshopauth.model.Role;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;

@Component
public class JWTProvider {
    @Value("${jwt.token.secret}")
    private String secretKey;
    @Value("${jwt.token.expired}")
    private Long timeValidation;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String login, Role userRole){
        Claims claims = Jwts.claims().setSubject(login);
        claims.put("role", userRole.name());
        Date currentDate = new Date();
        Date validationTime = new Date(currentDate.getTime() + timeValidation);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(currentDate)
                .setExpiration(validationTime)
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }

    public String getLogin(String token){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }
    public String getLogin(ServerWebExchange serverWebExchange){
        String authHeader = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer_")){
            return getLogin(authHeader.substring(7));
        }
        else{
            throw new IllegalArgumentException("Token is invalid");
        }
    }

    public String getRole(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().get("role", String.class);
    }


    public boolean validate(String token){
        try{
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        }catch(JwtException | IllegalArgumentException e){
            System.err.println("JWT is expired or invalid "+ e.getMessage());
            throw new IllegalArgumentException("JWT is expired or invalid "+ e.getMessage());
        }
    }
}
