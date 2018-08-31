package uk.ac.ebi.subs.validator.coordinator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.BaseSubmittable;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_BIOSAMPLES_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_CORE_ANALYSIS_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_CORE_ASSAYDATA_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_CORE_ASSAY_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_CORE_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_CORE_STUDY_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_ENA_ANALYSIS_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_ENA_ASSAYDATA_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_ENA_ASSAY_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_ENA_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_ENA_STUDY_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_TAXON_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.FileReferenceRoutingKeys.EVENT_ANALYSIS_FILEREF_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.FileReferenceRoutingKeys.EVENT_ASSAYDATA_FILEREF_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_ANALYSIS_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_ASSAYDATA_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_ASSAY_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.messaging.SchemaRoutingKeys.EVENT_SCHEMA_STUDY_VALIDATION;

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
        DataType dataType = dataTypeRepository.findOne(dataTypeId);
        Set<ValidationAuthor> validationAuthors = validationAuthorsForDataType(dataType);

        ValidationResult validationResult = coordinatorValidationResultService.fetchValidationResultDocument(submittable, validationAuthors);

        ValidationMessageEnvelope<?> messageEnvelope = validationEnvelopeFactory.buildValidationMessageEnvelope(submittable,validationResult);
        triggerValidationEvents(submittable, validationAuthors, messageEnvelope);

        return validationResult.getEntityUuid() != null;
    }

    /**
     * @param project
     * @return true if it could create a {@link ValidationMessageEnvelope} with the {@link Project} entity and
     * the UUID of the {@link ValidationResult}
     */
    protected boolean handleSubmittable(Project project, String dataTypeId) {
        DataType dataType = dataTypeRepository.findOne(dataTypeId);
        Set<ValidationAuthor> validationAuthors = validationAuthorsForDataType(dataType);

        ValidationResult validationResult = coordinatorValidationResultService.fetchValidationResultDocument(project, validationAuthors);

        ValidationMessageEnvelope<Project> messageEnvelope = new ValidationMessageEnvelope<>(validationResult.getUuid(), validationResult.getVersion(), project);

        triggerValidationEvents(project, validationAuthors, messageEnvelope);

        return validationResult.getEntityUuid() != null;
    }

    private void triggerValidationEvents(Submittable submittable, Set<ValidationAuthor> authors, ValidationMessageEnvelope envelope) {
        String className = submittable.getClass().getSimpleName();

        for (ValidationAuthor author : authors) {
            String routingKey = String.join(".", author.name(), className, "validation").toLowerCase();
            logger.trace("Sending {} for validation with routing key {}",submittable,routingKey);
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