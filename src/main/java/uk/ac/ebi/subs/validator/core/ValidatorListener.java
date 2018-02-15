package uk.ac.ebi.subs.validator.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.validator.core.handlers.AssayDataHandler;
import uk.ac.ebi.subs.validator.core.handlers.AssayHandler;
import uk.ac.ebi.subs.validator.core.handlers.SampleHandler;
import uk.ac.ebi.subs.validator.core.handlers.StudyHandler;
import uk.ac.ebi.subs.validator.core.messaging.Queues;
import uk.ac.ebi.subs.validator.core.messaging.RoutingKeys;
import uk.ac.ebi.subs.validator.data.*;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidatorListener {
    private static Logger logger = LoggerFactory.getLogger(ValidatorListener.class);

    @Autowired
    private AssayHandler assayHandler;
    @Autowired
    private AssayDataHandler assayDataHandler;
    @Autowired
    private SampleHandler sampleHandler;
    @Autowired
    private StudyHandler studyHandler;

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    public ValidatorListener(RabbitMessagingTemplate rabbitMessagingTemplate) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
    }

    @RabbitListener(queues = Queues.CORE_ASSAY_VALIDATION)
    public void handleAssayValidationRequest(AssayValidationMessageEnvelope envelope) {
        logger.debug("Assay validation request received with ID: {}.", envelope.getEntityToValidate().getId());

        SingleValidationResultsEnvelope singleValidationResultsEnvelope = assayHandler.handleValidationRequest(envelope);
        sendResults(singleValidationResultsEnvelope);
    }

    @RabbitListener(queues = Queues.CORE_ASSAYDATA_VALIDATION)
    public void handleAssayDataValidationRequest(AssayDataValidationMessageEnvelope envelope) {
        logger.debug("AssayData validation request received with ID: {}.", envelope.getEntityToValidate().getId());

        SingleValidationResultsEnvelope singleValidationResultsEnvelope = assayDataHandler.handleValidationRequest(envelope);
        sendResults(singleValidationResultsEnvelope);
    }

    @RabbitListener(queues = Queues.CORE_SAMPLE_VALIDATION)
    public void handleSampleValidationRequest(SampleValidationMessageEnvelope envelope) {
        logger.debug("Sample validation request received with ID: {}.", envelope.getEntityToValidate().getId());

        SingleValidationResultsEnvelope singleValidationResultsEnvelope = sampleHandler.handleValidationRequest(envelope);
        sendResults(singleValidationResultsEnvelope);
    }

    @RabbitListener(queues = Queues.CORE_STUDY_VALIDATION)
    public void handleStudyValidationRequest(StudyValidationMessageEnvelope envelope) {
        logger.debug("Study validation request received with ID: {}.", envelope.getEntityToValidate().getId());

        SingleValidationResultsEnvelope singleValidationResultsEnvelope = studyHandler.handleValidationRequest(envelope);
        sendResults(singleValidationResultsEnvelope);
    }

    private void sendResults(SingleValidationResultsEnvelope envelope) {
        List<SingleValidationResult> errorResults = envelope.getSingleValidationResults().stream().filter(svr -> svr.getValidationStatus().equals(SingleValidationResultStatus.Error)).collect(Collectors.toList());
        if (errorResults.size() > 0) {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, RoutingKeys.EVENT_VALIDATION_ERROR, envelope);
        } else {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, RoutingKeys.EVENT_VALIDATION_SUCCESS, envelope);
        }
    }
}
