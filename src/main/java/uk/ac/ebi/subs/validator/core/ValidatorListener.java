package uk.ac.ebi.subs.validator.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.validator.core.handlers.AnalysisHandler;
import uk.ac.ebi.subs.validator.core.handlers.AssayDataHandler;
import uk.ac.ebi.subs.validator.core.handlers.AssayHandler;
import uk.ac.ebi.subs.validator.core.handlers.SampleHandler;
import uk.ac.ebi.subs.validator.core.handlers.StudyHandler;
import uk.ac.ebi.subs.validator.core.messaging.Queues;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;

import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ebi.subs.validator.messaging.ValidatorsCommonRoutingKeys.EVENT_VALIDATION_ERROR;
import static uk.ac.ebi.subs.validator.messaging.ValidatorsCommonRoutingKeys.EVENT_VALIDATION_SUCCESS;

@Service
@RequiredArgsConstructor
public class ValidatorListener {
    private static Logger logger = LoggerFactory.getLogger(ValidatorListener.class);

    @NonNull
    private AssayHandler assayHandler;
    @NonNull
    private AssayDataHandler assayDataHandler;
    @NonNull
    private SampleHandler sampleHandler;
    @NonNull
    private StudyHandler studyHandler;
    @NonNull
    private AnalysisHandler analysisHandler;

    @NonNull
    private RabbitMessagingTemplate rabbitMessagingTemplate;

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

    @RabbitListener(queues = Queues.CORE_ANALYSIS_VALIDATION)
    public void handleStudyValidationRequest(AnalysisValidationEnvelope envelope) {
        logger.debug("Analysis validation request received with ID: {}.", envelope.getEntityToValidate().getId());

        SingleValidationResultsEnvelope singleValidationResultsEnvelope = analysisHandler.handleValidationRequest(envelope);
        sendResults(singleValidationResultsEnvelope);
    }

    private void sendResults(SingleValidationResultsEnvelope envelope) {
        List<SingleValidationResult> errorResults = envelope.getSingleValidationResults().stream().filter(svr -> svr.getValidationStatus().equals(SingleValidationResultStatus.Error)).collect(Collectors.toList());
        if (errorResults.size() > 0) {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_VALIDATION_ERROR, envelope);
        } else {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_VALIDATION_SUCCESS, envelope);
        }
    }
}
