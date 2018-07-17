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

import static uk.ac.ebi.subs.validator.messaging.SchemaQueues.SCHEMA_ANALYSIS_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaQueues.SCHEMA_ASSAYDATA_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaQueues.SCHEMA_ASSAY_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaQueues.SCHEMA_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaQueues.SCHEMA_STUDY_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_ANALYSIS_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_ASSAYDATA_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_ASSAY_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_STUDY_VALIDATION;

@Configuration
@ComponentScan(basePackageClasses = ExchangeConfig.class)
public class SchemaMessagingConfiguration {

    @Bean
    public Queue schemaSampleValidationQueue() {
        return Queues.buildQueueWithDlx(SCHEMA_SAMPLE_VALIDATION);
    }

    @Bean
    public Binding schemaSampleValidationBinding(Queue schemaSampleValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(schemaSampleValidationQueue).to(submissionExchange).with(EVENT_SCHEMA_SAMPLE_VALIDATION);
    }

    @Bean
    public Queue schemaStudyValidationQueue() {
        return Queues.buildQueueWithDlx(SCHEMA_STUDY_VALIDATION);
    }

    @Bean
    public Binding schemaStudyValidationBinding(Queue schemaStudyValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(schemaStudyValidationQueue).to(submissionExchange).with(EVENT_SCHEMA_STUDY_VALIDATION);
    }

    @Bean
    public Queue schemaAssayValidationQueue() {
        return Queues.buildQueueWithDlx(SCHEMA_ASSAY_VALIDATION);
    }

    @Bean
    public Binding schemaAssayValidationBinding(Queue schemaAssayValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(schemaAssayValidationQueue).to(submissionExchange).with(EVENT_SCHEMA_ASSAY_VALIDATION);
    }

    @Bean
    public Queue schemaAssayDataValidationQueue() {
        return Queues.buildQueueWithDlx(SCHEMA_ASSAYDATA_VALIDATION);
    }

    @Bean
    public Binding schemaAssayDataValidationBinding(Queue schemaAssayDataValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(schemaAssayDataValidationQueue).to(submissionExchange).with(EVENT_SCHEMA_ASSAYDATA_VALIDATION);
    }

    @Bean Queue schemaAnalysisValidationQueue(){
        return Queues.buildQueueWithDlx(SCHEMA_ANALYSIS_VALIDATION);
    }

    @Bean
    public Binding schemaAnalysisValidationBinding(Queue schemaAnalysisValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(schemaAnalysisValidationQueue).to(submissionExchange).with(EVENT_SCHEMA_ANALYSIS_VALIDATION);
    }
}
