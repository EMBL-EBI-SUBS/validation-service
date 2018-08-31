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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubmittableHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubmittableHandler.class);

    private Collection<ValidationAuthor> standardAuthors = Arrays.asList(ValidationAuthor.Core, ValidationAuthor.JsonSchema);

    @NonNull
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @NonNull
    private DataTypeRepository dataTypeRepository;

    @NonNull
    private CoordinatorValidationResultService coordinatorValidationResultService;

    @NonNull
    private ValidationEnvelopeFactory validationEnvelopeFactory;

    /**
     * @param submittable
     * @param submissionId
     * @param dataTypeId
     * @return true if it could create a {@link ValidationMessageEnvelope} with the {@link Project} entity and
     * the UUID of the {@link ValidationResult}
     */
    protected boolean handleSubmittable(Submittable submittable, String submissionId, String dataTypeId) {
        logger.trace("submittable {}; submissionId {}; dataTypeId {}",submittable,submissionId,dataTypeId);
        DataType dataType = dataTypeRepository.findOne(dataTypeId);
        Set<ValidationAuthor> validationAuthors = validationAuthorsForDataType(dataType);

        Optional<ValidationResult> validationResult = coordinatorValidationResultService.fetchValidationResultDocument(submittable, validationAuthors);

        if (validationResult.isPresent()) {
            ValidationMessageEnvelope<?> messageEnvelope = validationEnvelopeFactory.buildValidationMessageEnvelope(submittable, validationResult.get());
            triggerValidationEvents(submittable, validationAuthors, messageEnvelope);
        }
        return validationResult.isPresent() && validationResult.get().getEntityUuid() != null;
    }

    private void triggerValidationEvents(Submittable submittable, Set<ValidationAuthor> authors, ValidationMessageEnvelope envelope) {
        String className = submittable.getClass().getSimpleName();

        for (ValidationAuthor author : authors) {
            String routingKey = String.join(".", author.name(), className, "validation").toLowerCase();
            logger.trace("Sending {} for validation with routing key {}", submittable, routingKey);
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, routingKey, envelope);
        }
    }

    private Set<ValidationAuthor> validationAuthorsForDataType(DataType dataType) {
        Set<ValidationAuthor> authors = new HashSet<>();
        authors.addAll(standardAuthors);
        if (dataType.getRequiredValidationAuthors() != null) {
            authors.addAll(dataType.getRequiredValidationAuthors().stream().map(name -> ValidationAuthor.valueOf(name)).collect(Collectors.toList()));
        }
        if (dataType.getOptionalValidationAuthors() != null) {
            authors.addAll(dataType.getOptionalValidationAuthors().stream().map(name -> ValidationAuthor.valueOf(name)).collect(Collectors.toList()));
        }

        return authors;
    }


}