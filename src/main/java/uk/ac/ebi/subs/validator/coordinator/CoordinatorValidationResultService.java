package uk.ac.ebi.subs.validator.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.fileupload.File;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.GlobalValidationStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;
import uk.ac.ebi.subs.validator.util.BlankValidationResultMaps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CoordinatorValidationResultService {
    private static Logger logger = LoggerFactory.getLogger(CoordinatorValidationResultService.class);

    private ValidationResultRepository repository;

    public CoordinatorValidationResultService(ValidationResultRepository repository) {
        this.repository = repository;
    }

    public Optional<ValidationResult> fetchValidationResultDocument(Submittable submittable, Collection<ValidationAuthor> authorsRequired) {
        Optional<ValidationResult> optionalValidationResult = findAndUpdateValidationResult(submittable);

        if (optionalValidationResult.isPresent()) {
            ValidationResult validationResult = optionalValidationResult.get();
            logger.trace("Validation result document has been persisted into MongoDB with ID: {}", validationResult.getUuid());
            validationResult.setExpectedResults(BlankValidationResultMaps.generateDefaultMap(authorsRequired));

            repository.save(validationResult);
        }

        return optionalValidationResult;
    }

    public Optional<ValidationResult> fetchValidationResultDocument(File file) {
        Optional<ValidationResult> optionalValidationResult = findAndUpdateValidationResult(file);
        ValidationResult validationResult = null;

        if (optionalValidationResult.isPresent()) {
            validationResult = optionalValidationResult.get();

            List<SingleValidationResult> fileContentValidationResults =
                    validationResult.getExpectedResults().get(ValidationAuthor.FileContent);

            Map<ValidationAuthor, List<SingleValidationResult>> expectedResultsForFile =
                    BlankValidationResultMaps.forFile();
            if (fileContentValidationResults != null) {
                expectedResultsForFile.put(ValidationAuthor.FileContent, fileContentValidationResults);
            }

            validationResult.setExpectedResults(expectedResultsForFile);

            repository.save(validationResult);
        }
        return Optional.ofNullable(validationResult);
    }

    private Optional<ValidationResult> findAndUpdateValidationResult(Submittable submittable) {
        String submittableUuid = submittable.getId();
        return getValidationResult(submittableUuid);
    }

    private Optional<ValidationResult> findAndUpdateValidationResult(File file) {
        String fileId = file.getId();
        return getValidationResult(fileId);
    }

    private Optional<ValidationResult> getValidationResult(String entityId) {
        ValidationResult validationResult = repository.findByEntityUuid(entityId);
        if (validationResult != null) {
            validationResult.setValidationStatus(GlobalValidationStatus.Pending);
            validationResult.setVersion(validationResult.getVersion() + 1);
            logger.trace("ValidationResult has been changed to status: {} and version: {}",
                    validationResult.getValidationStatus().name(), validationResult.getVersion());
        } else {
            logger.error(String.format("Could not find ValidationResult for submittable with ID: %s", entityId));
        }
        return Optional.ofNullable(validationResult);
    }

}
