package uk.ac.ebi.subs.validator.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.messaging.ExchangeConfig;
import uk.ac.ebi.subs.messaging.Queues;

@Configuration
@ComponentScan(basePackageClasses = ExchangeConfig.class)
public class SchemaMessagingConfiguration {

    @Bean
    public Queue schemaValidationQueue() {
        return Queues.buildQueueWithDlx(SchemaQueues.SCHEMA_VALIDATION);
    }

    @Bean
    public Binding schemaValidationBinding(Queue schemaValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(schemaValidationQueue).to(submissionExchange).with(SchemaRoutingKeys.EVENT_SCHEMA_VALIDATION);
    }

}
