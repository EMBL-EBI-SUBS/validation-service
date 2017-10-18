package uk.ac.ebi.subs.validator.flipper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.validator.data.AggregatorToFlipperEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.GlobalValidationStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a service to modify the {@code ValidationResult} status according to the entities validation result.
 */
@Service
public class StatusFlipperValidationResultService {

    public static final Logger logger = LoggerFactory.getLogger(StatusFlipperValidationResultService.class);

    private ValidationResultRepository repository;

    public StatusFlipperValidationResultService(ValidationResultRepository repository) {
        this.repository = repository;
    }

    public boolean updateValidationResult(AggregatorToFlipperEnvelope envelope) {
        ValidationResult validationResult = repository.findOne(envelope.getValidationResultUuid());

        if (validationResult != null) {
            if (validationResult.getVersion() == envelope.getValidationResultVersion()) {
                flipStatusIfRequired(validationResult);
                return true;
            }
        }
        return false;
    }

    private void flipStatusIfRequired(ValidationResult validationResult) {
        Map<ValidationAuthor, List<SingleValidationResult>> validationResults = validationResult.getExpectedResults();

        if (validationResults.values().stream().filter(list -> list.isEmpty()).collect(Collectors.toList()).isEmpty()) {
            validationResult.setValidationStatus(GlobalValidationStatus.Complete);
            repository.save(validationResult);

            logger.info("Validation result document with id {} is completed.", validationResult.getUuid());
        } else {
            logger.debug("Validation for document with id {} is still in process.", validationResult.getUuid());
        }
    }

}
