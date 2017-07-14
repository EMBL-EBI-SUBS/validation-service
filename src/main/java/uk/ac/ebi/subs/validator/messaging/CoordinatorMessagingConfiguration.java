package uk.ac.ebi.subs.validator.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;

/**
 * Created by karoly on 05/07/2017.
 */
@Configuration
@ComponentScan(basePackageClasses = ValidationExchangeConfig.class)
public class CoordinatorMessagingConfiguration {

    @Bean
    public MessageConverter messageConverter() {
        return new MappingJackson2MessageConverter();
    }

    /**
     * Instantiate a {@link Queue} for validate published submissions.
     *
     * @return an instance of a {@link Queue} for validate published submissions.
     */
    @Bean
    Queue submissionSampleValidatorQueue() {
        return new Queue(Queues.SUBMISSION_SAMPLE_VALIDATOR, true);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of created submissions.
     *
     * @param submissionSampleValidatorQueue {@link Queue} for validating sample submissions before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of created submissions.
     */
    @Bean
    Binding validationForCreatedSampleSubmissionBinding(Queue submissionSampleValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionSampleValidatorQueue).to(submissionExchange)
                .with(RoutingKeys.SUBMITTABLE_SAMPLE_CREATED);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     *
     * @param submissionSampleValidatorQueue {@link Queue} for validating sample submissions before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     */
    @Bean
    Binding validationForUpdatedSampleSubmissionBinding(Queue submissionSampleValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionSampleValidatorQueue).to(submissionExchange)
                .with(RoutingKeys.SUBMITTABLE_SAMPLE_UPDATED);
    }

    @Bean
    Queue submissionStudyValidatorQueue() {
        return new Queue(Queues.SUBMISSION_STUDY_VALIDATOR, true);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of created submissions.
     *
     * @param submissionStudyValidatorQueue {@link Queue} for validating study submissions before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of created submissions.
     */
    @Bean
    Binding validationForCreatedStudySubmissionBinding(Queue submissionStudyValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionStudyValidatorQueue).to(submissionExchange)
                .with(RoutingKeys.SUBMITTABLE_STUDY_CREATED);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     *
     * @param submissionStudyValidatorQueue {@link Queue} for validating study submissions before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     */
    @Bean
    Binding validationForUpdatedStudySubmissionBinding(Queue submissionStudyValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionStudyValidatorQueue).to(submissionExchange)
                .with(RoutingKeys.SUBMITTABLE_STUDY_UPDATED);
    }

    @Bean
    Queue submissionAssayValidatorQueue() {
        return new Queue(Queues.SUBMISSION_ASSAY_VALIDATOR, true);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of created submissions.
     *
     * @param submissionAssayValidatorQueue {@link Queue} for validating assay submissions before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of created submissions.
     */
    @Bean
    Binding validationForCreatedAssaySubmissionBinding(Queue submissionAssayValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionAssayValidatorQueue).to(submissionExchange)
                .with(RoutingKeys.SUBMITTABLE_ASSAY_CREATED);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     *
     * @param submissionAssayValidatorQueue {@link Queue} for validating assay submissions before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     */
    @Bean
    Binding validationForUpdatedAssaySubmissionBinding(Queue submissionAssayValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionAssayValidatorQueue).to(submissionExchange)
                .with(RoutingKeys.SUBMITTABLE_ASSAY_UPDATED);
    }

    @Bean
    Queue submissionAssayDataValidatorQueue() {
        return new Queue(Queues.SUBMISSION_ASSAY_DATA_VALIDATOR, true);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of created submissions.
     *
     * @param submissionAssayDataValidatorQueue {@link Queue} for validating assay data submissions before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of created submissions.
     */
    @Bean
    Binding validationForCreatedAssayDataSubmissionBinding(Queue submissionAssayDataValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionAssayDataValidatorQueue).to(submissionExchange)
                .with(RoutingKeys.SUBMITTABLE_ASSAYDATA_CREATED);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     *
     * @param submissionAssayDataValidatorQueue {@link Queue} for validating assay data submissions before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of updated submissions.
     */
    @Bean
    Binding validationForUpdatedAssayDataSubmissionBinding(Queue submissionAssayDataValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionAssayDataValidatorQueue).to(submissionExchange)
                .with(RoutingKeys.SUBMITTABLE_ASSAYDATA_UPDATED);
    }
}
