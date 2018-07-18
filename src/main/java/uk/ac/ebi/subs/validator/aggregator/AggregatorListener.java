package uk.ac.ebi.subs.validator.aggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.validator.data.AggregatorToFlipperEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.messaging.AggregatorQueues;
import uk.ac.ebi.subs.validator.messaging.AggregatorRoutingKeys;

/**
 * This class is listening on events on the validation result {@code Queue}.
 * When processing a published event it will update the {@code {@link uk.ac.ebi.subs.validator.data.ValidationResult}}
 * document with the validation results and publish a message of the updated document's UUID
 * to the validation result document update queue.
 *
 * Created by karoly on 05/05/2017.
 */
@Service
public class AggregatorListener {

    private static Logger logger = LoggerFactory.getLogger(AggregatorListener.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    AggregatorValidationResultService aggregatorValidationResultService;

    public AggregatorListener(RabbitMessagingTemplate rabbitMessagingTemplate,
                              AggregatorValidationResultService aggregatorValidationResultService) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.aggregatorValidationResultService = aggregatorValidationResultService;
    }

    @RabbitListener(queues = AggregatorQueues.VALIDATION_RESULT)
    public void handleValidationResult(SingleValidationResultsEnvelope singleValidationResultsEnvelope) {
        logger.debug("Received single validation results from {}.", singleValidationResultsEnvelope.getValidationAuthor());

        logger.debug("Trying to update Validation Result Document in MongoDB...");
        boolean success = aggregatorValidationResultService.updateValidationResult(singleValidationResultsEnvelope);

        if (singleValidationResultsEnvelope.getValidationAuthor().equals(ValidationAuthor.FileContent)) {
            logger.debug("File Content Validation finished. No need to send message to the Status Flipper component.");
            return;
        }

        if(success) {
            sendValidationResultDocumentUpdate(singleValidationResultsEnvelope);
        } else {
            logger.info("Ignoring obsolete validation results.");
        }
    }

    private void sendValidationResultDocumentUpdate(SingleValidationResultsEnvelope singleValidationResultsEnvelope) {
        logger.debug("Sending message: validation result {} document has been updated in MongoDB.", singleValidationResultsEnvelope.getValidationResultUUID());

        AggregatorToFlipperEnvelope envelope = new AggregatorToFlipperEnvelope(
                singleValidationResultsEnvelope.getValidationResultUUID(),
                singleValidationResultsEnvelope.getValidationResultVersion()
        );

        rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, AggregatorRoutingKeys.EVENT_VALIDATION_RESULT_DOCUMENT_UPDATED, envelope);
    }
}
