package uk.ac.ebi.subs.validator.coordinator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubmittableHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubmittableHandler.class);

    @NonNull
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @NonNull
    private DataTypeRepository dataTypeRepository;

    @NonNull
    private CoordinatorValidationResultService coordinatorValidationResultService;

    @NonNull
    private ValidationEnvelopeFactory validationEnvelopeFactory;

    /**
     * @param submittable the submittable entity to handle
     * @param submissionId the ID of the submission the submittable belongs to
     * @param dataTypeId the ID of the data type of the submittable entity
     * @return true if it could create a {@link ValidationMessageEnvelope} with the {@link Project} entity and
     * the UUID of the {@link ValidationResult}
     */
    protected boolean handleSubmittable(Submittable submittable, String submissionId, String dataTypeId,
                                        String checklistId) {
        logger.trace("submittable {}; submissionId {}; dataTypeId {}",submittable,submissionId,dataTypeId);

        Set<ValidationAuthor> validationAuthors = new HashSet<>();

        if (dataTypeId != null) {
            Optional<DataType> optionalDataType = dataTypeRepository.findById(dataTypeId);
            optionalDataType.ifPresent( dataType ->
                validationAuthors.addAll(validationAuthorsForDataType(dataType))
            );
        }

        Optional<ValidationResult> validationResult = coordinatorValidationResultService.fetchValidationResultDocument(submittable, validationAuthors);

        if (validationResult.isPresent()) {
            ValidationMessageEnvelope<?> messageEnvelope = validationEnvelopeFactory.buildValidationMessageEnvelope(submittable, validationResult.get(),dataTypeId,checklistId);
            triggerValidationEvents(submittable, validationAuthors, messageEnvelope);
        }
        return validationResult.isPresent() && validationResult.get().getEntityUuid() != null;
    }

    private void triggerValidationEvents(Submittable submittable, Set<ValidationAuthor> authors, ValidationMessageEnvelope<?> envelope) {
        String className = submittable.getClass().getSimpleName();

        for (ValidationAuthor author : authors) {
            String routingKey = String.join(".", author.name(), className, "validation").toLowerCase();
            logger.trace("Sending {} for validation with routing key {}", submittable, routingKey);
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, routingKey, envelope);
        }
    }

    private Set<ValidationAuthor> validationAuthorsForDataType(DataType dataType) {
        Set<ValidationAuthor> authors = new HashSet<>();
        
        if (dataType.getRequiredValidationAuthors() != null) {
            authors.addAll(dataType.getRequiredValidationAuthors().stream().map(ValidationAuthor::valueOf).collect(Collectors.toList()));
        }
        if (dataType.getOptionalValidationAuthors() != null) {
            authors.addAll(dataType.getOptionalValidationAuthors().stream().map(ValidationAuthor::valueOf).collect(Collectors.toList()));
        }

        return authors;
    }
}