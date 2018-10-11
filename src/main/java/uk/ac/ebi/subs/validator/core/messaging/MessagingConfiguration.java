package uk.ac.ebi.subs.validator.core.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.messaging.ExchangeConfig;

import static uk.ac.ebi.subs.messaging.Queues.buildQueueWithDlx;

@Configuration
@ComponentScan(basePackageClasses = ExchangeConfig.class)
public class MessagingConfiguration {

    @Bean
    public Queue coreAssayValidationQueue() {
        return buildQueueWithDlx(Queues.CORE_ASSAY_VALIDATION);
    }

    @Bean
    public Binding coreAssayValidationBinding(Queue coreAssayValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(coreAssayValidationQueue).to(submissionExchange).with(RoutingKeys.EVENT_CORE_ASSAY_VALIDATION);
    }

    @Bean
    public Queue coreAssayDataValidationQueue() {
        return buildQueueWithDlx(Queues.CORE_ASSAYDATA_VALIDATION);
    }

    @Bean
    public Binding coreAssayDataValidationBinding(Queue coreAssayDataValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(coreAssayDataValidationQueue).to(submissionExchange).with(RoutingKeys.EVENT_CORE_ASSAYDATA_VALIDATION);
    }

    @Bean
    public Queue coreSampleValidationQueue() {
        return buildQueueWithDlx(Queues.CORE_SAMPLE_VALIDATION);
    }

    @Bean
    public Binding coreSampleValidationBinding(Queue coreSampleValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(coreSampleValidationQueue).to(submissionExchange).with(RoutingKeys.EVENT_CORE_SAMPLE_VALIDATION);
    }

    @Bean
    public Queue coreStudyValidationQueue() {
        return buildQueueWithDlx(Queues.CORE_STUDY_VALIDATION);
    }

    @Bean
    public Binding coreStudyValidationBinding(Queue coreStudyValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(coreStudyValidationQueue).to(submissionExchange).with(RoutingKeys.EVENT_CORE_STUDY_VALIDATION);
    }

    @Bean
    public Queue coreAnalysisValidationQueue() {
        return buildQueueWithDlx(Queues.CORE_ANALYSIS_VALIDATION);
    }

    @Bean
    public Binding coreAnalysisValidationBinding(Queue coreAnalysisValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(coreAnalysisValidationQueue).to(submissionExchange).with(RoutingKeys.EVENT_CORE_ANALYSIS_VALIDATION);
    }

    @Bean
    public Queue coreSampleGroupValidationQueue() {
        return buildQueueWithDlx(Queues.CORE_SAMPLE_GROUP_VALIDATION);
    }

    @Bean
    public Binding coreSampleGroupValidationBinding(Queue coreSampleGroupValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(coreSampleGroupValidationQueue).to(submissionExchange).with(RoutingKeys.EVENT_CORE_SAMPLE_GROUP_VALIDATION);
    }

    @Bean
    public Queue coreEgaDatasetValidationQueue() {
        return buildQueueWithDlx(Queues.CORE_EGA_DATASET_VALIDATION);
    }

    @Bean
    public Binding coreEgaDatasetValidationBinding(Queue coreEgaDatasetValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(coreEgaDatasetValidationQueue).to(submissionExchange).with(RoutingKeys.EVENT_CORE_EGA_DATASET_VALIDATION);
    }

    @Bean
    public Queue coreEgaDacPolicyValidationQueue() {
        return buildQueueWithDlx(Queues.CORE_EGA_DAC_POLICY_VALIDATION);
    }

    @Bean
    public Binding coreEgaDacPolicyValidationBinding(Queue coreEgaDacPolicyValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(coreEgaDacPolicyValidationQueue).to(submissionExchange).with(RoutingKeys.EVENT_CORE_EGA_DAC_POLICY_VALIDATION);
    }

}
