package ru.hse.antiplag.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@SpringBootApplication
public class ApiGatewayApplication {
  public static void main(String[] args) {
      SpringApplication.run(ApiGatewayApplication.class, args);
  }

  /**
   * Configures basic OpenAPI metadata.
   *
   * @return OpenAPI object with API title, version, and description.
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("HSE Antiplag API Gateway")
            .version("v1.0")
            .description("API Gateway for HSE Antiplag System. \n" +
                "Handles file uploads, downloads, and analysis requests, routing them to appropriate microservices."));
  }
} 
