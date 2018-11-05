package uk.ac.ebi.subs.validator.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.messaging.ExchangeConfig;
import uk.ac.ebi.subs.messaging.Queues;

import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.*;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.*;

/**
 * Created by karoly on 05/07/2017.
 */
@Configuration
@ComponentScan(basePackageClasses = ExchangeConfig.class)
public class CoordinatorMessagingConfiguration {

    /**
     * Instantiate a {@link Queue} for validate published submissions.
     *
     * @return an instance of a {@link Queue} for validate published submissions.
     */
    @Bean
    Queue submissionSampleValidatorQueue() {
        return Queues.buildQueueWithDlx(SUBMISSION_SAMPLE_VALIDATOR);
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
                .with(SUBMITTABLE_SAMPLE_CREATED);
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
                .with(SUBMITTABLE_SAMPLE_UPDATED);
    }

    @Bean
    Queue submissionStudyValidatorQueue() {
        return Queues.buildQueueWithDlx(SUBMISSION_STUDY_VALIDATOR);
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
                .with(SUBMITTABLE_STUDY_CREATED);
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
                .with(SUBMITTABLE_STUDY_UPDATED);
    }

    @Bean
    Queue submissionAssayValidatorQueue() {
        return Queues.buildQueueWithDlx(SUBMISSION_ASSAY_VALIDATOR);
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
                .with(SUBMITTABLE_ASSAY_CREATED);
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
                .with(SUBMITTABLE_ASSAY_UPDATED);
    }

    @Bean
    Queue submissionAssayDataValidatorQueue() {
        return Queues.buildQueueWithDlx(SUBMISSION_ASSAY_DATA_VALIDATOR);
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
                .with(SUBMITTABLE_ASSAYDATA_CREATED);
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
                .with(SUBMITTABLE_ASSAYDATA_UPDATED);
    }

    @Bean
    Queue submissionProjectValidatorQueue(){ return Queues.buildQueueWithDlx(SUBMISSION_PROJECT_VALIDATOR);}

    @Bean
    Binding validationForCreatedProjectSubmissionBinding(Queue submissionProjectValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionProjectValidatorQueue).to(submissionExchange)
                .with(SUBMITTABLE_PROJECT_CREATED);
    }

    @Bean
    Binding validationForUpdatedProjectSubmissionBinding(Queue submissionProjectValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionProjectValidatorQueue).to(submissionExchange)
                .with(SUBMITTABLE_PROJECT_UPDATED);
    }

    @Bean
    Queue submissionAnalysisValidatorQueue() { return  Queues.buildQueueWithDlx(SUBMISSION_ANALYSIS_VALIDATOR);}

    @Bean
    Binding validationForCreatedAnalysisSubmissionBinding(Queue submissionAnalysisValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionAnalysisValidatorQueue).to(submissionExchange)
                .with(SUBMITTABLE_ANALYSIS_CREATED);
    }

    @Bean
    Binding validationForUpdatedAnalysisSubmissionBinding(Queue submissionAnalysisValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionAnalysisValidatorQueue).to(submissionExchange)
                .with(SUBMITTABLE_ANALYSIS_UPDATED);
    }

    @Bean
    Queue submissionSampleGroupValidatorQueue() { return  Queues.buildQueueWithDlx(SUBMISSION_SAMPLE_GROUP_VALIDATOR);}

    @Bean
    Binding validationForCreatedSampleGroupSubmissionBinding(Queue submissionSampleGroupValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionSampleGroupValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_SAMPLE_GROUP_CREATED);
    }

    @Bean
    Binding validationForUpdatedSampleGroupSubmissionBinding(Queue submissionSampleGroupValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submissionSampleGroupValidatorQueue).to(submissionExchange)
                .with(SUBMITTABLE_SAMPLE_GROUP_UPDATED);
    }

    @Bean
    Queue submissionProtocolValidatorQueue() { return  Queues.buildQueueWithDlx(SUBMISSION_PROTOCOL_VALIDATOR);}

    @Bean
    Binding validationForCreatedProtocolSubmissionBinding(Queue submissionProtocolValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionProtocolValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_PROTOCOL_CREATED);
    }

    @Bean
    Binding validationForUpdatedProtocolSubmissionBinding(Queue submissionProtocolValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionProtocolValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_PROTOCOL_UPDATED);
    }

    @Bean
    Queue submissionEgaDacValidatorQueue() { return  Queues.buildQueueWithDlx(SUBMISSION_EGA_DAC_VALIDATOR);}

    @Bean
    Binding validationForCreatedEgaDacSubmissionBinding(Queue submissionEgaDacValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionEgaDacValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_EGA_DAC_CREATED);
    }

    @Bean
    Binding validationForUpdatedEgaDacSubmissionBinding(Queue submissionEgaDacValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionEgaDacValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_EGA_DAC_UPDATED);
    }

    @Bean
    Queue submissionEgaDacPolicyValidatorQueue() { return  Queues.buildQueueWithDlx(SUBMISSION_EGA_DAC_POLICY_VALIDATOR);}

    @Bean
    Binding validationForCreatedEgaDacPolicySubmissionBinding(Queue submissionEgaDacPolicyValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionEgaDacPolicyValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_EGA_DAC_POLICY_CREATED);
    }

    @Bean
    Binding validationForUpdatedEgaDacPolicySubmissionBinding(Queue submissionEgaDacPolicyValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionEgaDacPolicyValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_EGA_DAC_POLICY_UPDATED);
    }

    @Bean
    Queue submissionEgaDatasetValidatorQueue() { return  Queues.buildQueueWithDlx(SUBMISSION_EGA_DATASET_VALIDATOR);}

    @Bean
    Binding validationForCreatedEgaDatasetSubmissionBinding(Queue submissionEgaDatasetValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionEgaDatasetValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_EGA_DATASET_CREATED);
    }

    @Bean
    Binding validationForUpdatedEgaDatasetSubmissionBinding(Queue submissionEgaDatasetValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder
                .bind(submissionEgaDatasetValidatorQueue)
                .to(submissionExchange)
                .with(SUBMITTABLE_EGA_DATASET_UPDATED);
    }

    /**
     * Instantiate a {@link Queue} for validate file reference existence.
     *
     * @return an instance of a {@link Queue} for validate file reference existence.
     */
    @Bean
    Queue fileReferenceValidatorQueue() {
        return Queues.buildQueueWithDlx(FILE_REF_VALIDATOR);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of file
     * reference validation.
     *
     * @param fileReferenceValidatorQueue {@link Queue} for validating file reference existence before submitting them
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of file
     * reference validation.
     */
    @Bean
    Binding validationForFileReferenceExistenceBinding(Queue fileReferenceValidatorQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(fileReferenceValidatorQueue).to(submissionExchange)
                .with(EVENT_FILE_CREATED);
    }

    /**
     * Instantiate a {@link Queue} for validate file reference existence after file has been deleted.
     *
     * @return an instance of a {@link Queue} for validate file reference existence after file has been deleted.
     */
    @Bean
    Queue fileDeletionQueue() {
        return Queues.buildQueueWithDlx(FILE_DELETION_VALIDATOR);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of the file
     * deletion.
     *
     * @param fileDeletionQueue {@link Queue} for validating file reference existence after file has been deleted
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of file
     * deletion.
     */
    @Bean
    Binding validationForFileDeletionBinding(Queue fileDeletionQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(fileDeletionQueue).to(submissionExchange)
                .with(EVENT_FILE_DELETED);
    }

    /**
     * Instantiate a {@link Queue} for validate the given submission after submittable has been deleted.
     *
     * @return an instance of a {@link Queue} for validate submission after submittable has been deleted.
     */
    @Bean
    Queue submittableDeletionQueue() {
        return Queues.buildQueueWithDlx(SUBMISSION_SUBMITTABLE_DELETED);
    }

    /**
     * Create a {@link Binding} between the submission exchange and validation queue using the routing key of the
     * submittable deletion.
     *
     * @param submittableDeletionQueue {@link Queue} for validating submission after submittable has been deleted
     * @param submissionExchange {@link TopicExchange} for submissions
     * @return a {@link Binding} between the submission exchange and validation queue using the routing key of
     * submittable deletion.
     */
    @Bean
    Binding validationForSubmittableDeletionBinding(Queue submittableDeletionQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(submittableDeletionQueue).to(submissionExchange)
                .with(EVENT_SUBMITTABLE_DELETED);
    }
}
