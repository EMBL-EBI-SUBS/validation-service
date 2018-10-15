package uk.ac.ebi.subs.validator.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.messaging.SchemaQueues;
import uk.ac.ebi.subs.validator.schema.model.SchemaValidationMessageEnvelope;

import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ebi.subs.validator.messaging.ValidatorsCommonRoutingKeys.EVENT_VALIDATION_ERROR;
import static uk.ac.ebi.subs.validator.messaging.ValidatorsCommonRoutingKeys.EVENT_VALIDATION_SUCCESS;

@Service
public class JsonSchemaValidatorListener {
    private static Logger logger = LoggerFactory.getLogger(JsonSchemaValidatorListener.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;
    private JsonSchemaValidationHandler validationHandler;

    public JsonSchemaValidatorListener(RabbitMessagingTemplate rabbitMessagingTemplate, JsonSchemaValidationHandler validationHandler) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.validationHandler = validationHandler;
    }

    @RabbitListener(queues = SchemaQueues.SCHEMA_VALIDATION)
    public void handleSampleValidationRequest(SchemaValidationMessageEnvelope envelope) {
        logger.debug("Schema validation request received: {}.", envelope);

        SingleValidationResultsEnvelope resultsEnvelope = validationHandler.handleSubmittableValidation(envelope);
        sendResults(resultsEnvelope);
    }

    private void sendResults(SingleValidationResultsEnvelope envelope) {
        List<SingleValidationResult> errorResults = envelope.getSingleValidationResults()
                .stream()
                .filter(svr -> svr.getValidationStatus().equals(SingleValidationResultStatus.Error))
                .collect(Collectors.toList());

        if (errorResults.size() > 0) {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_VALIDATION_ERROR, envelope);
        } else {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_VALIDATION_SUCCESS, envelope);
        }
    }
}
