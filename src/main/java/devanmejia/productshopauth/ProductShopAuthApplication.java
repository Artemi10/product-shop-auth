package devanmejia.productshopauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ProductShopAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductShopAuthApplication.class, args);
    }

}
