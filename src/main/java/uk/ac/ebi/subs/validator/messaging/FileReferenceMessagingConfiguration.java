package uk.ac.ebi.subs.validator.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.messaging.ExchangeConfig;

import static uk.ac.ebi.subs.messaging.Queues.buildQueueWithDlx;
import static uk.ac.ebi.subs.validator.messaging.FileReferenceQueues.FILE_REFERENCE_ANALYSIS_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.FileReferenceQueues.FILE_REFERENCE_ASSAYDATA_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.FileReferenceRoutingKeys.EVENT_ANALYSIS_FILEREF_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.FileReferenceRoutingKeys.EVENT_ASSAYDATA_FILEREF_VALIDATION;


@Configuration
@ComponentScan(basePackageClasses = ExchangeConfig.class)
public class FileReferenceMessagingConfiguration {

    @Bean
    public Queue fileReferenceAssayDataValidationQueue() {
        return buildQueueWithDlx(FILE_REFERENCE_ASSAYDATA_VALIDATION);
    }

    @Bean
    public Binding fileReferenceAssayDataValidationBinding(Queue fileReferenceAssayDataValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(fileReferenceAssayDataValidationQueue).to(submissionExchange).with(EVENT_ASSAYDATA_FILEREF_VALIDATION);
    }

    @Bean
    public Queue fileReferenceAnalysisValidationQueue() {
        return buildQueueWithDlx(FILE_REFERENCE_ANALYSIS_VALIDATION);
    }

    @Bean
    public Binding fileReferenceAnalysisValidationBinding(Queue fileReferenceAnalysisValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(fileReferenceAnalysisValidationQueue).to(submissionExchange).with(EVENT_ANALYSIS_FILEREF_VALIDATION);
    }

    @Bean
    public Queue fileReferenceValidationQueue() {
        return buildQueueWithDlx(FileReferenceQueues.FILE_REFERENCE_VALIDATION);
    }

    @Bean
    public Binding fileReferenceValidationBinding(Queue fileReferenceValidationQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(fileReferenceValidationQueue)
                .to(submissionExchange).with(FileReferenceRoutingKeys.EVENT_FILE_REFERENCE_VALIDATION);
    }
}
