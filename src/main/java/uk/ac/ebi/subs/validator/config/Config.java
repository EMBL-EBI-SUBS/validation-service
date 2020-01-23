package uk.ac.ebi.subs.validator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.validator.schema.JsonSchemaValidationService;

@Configuration(value = "ValidatorConfiguration")
public class Config {

    @Bean
    public JsonSchemaValidationService jsonSchemaValidationService(
            @Value("${validator.schema.url}") String jsonSchemaValidatorUrl,
            RestTemplate restTemplate) {
        return new JsonSchemaValidationService(jsonSchemaValidatorUrl, restTemplate);
    }
}
