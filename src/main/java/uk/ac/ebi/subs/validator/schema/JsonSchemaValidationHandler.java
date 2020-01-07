package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.schema.custom.SchemaObjectMapperProvider;
import uk.ac.ebi.subs.validator.schema.model.JsonSchemaValidationError;
import uk.ac.ebi.subs.validator.schema.model.SchemaValidationMessageEnvelope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.ac.ebi.subs.repository.util.SchemaConverterFromMongo.fixStoredJson;
import static uk.ac.ebi.subs.validator.util.ValidationHelper.generatePassingSingleValidationResult;
import static uk.ac.ebi.subs.validator.util.ValidationHelper.generateSingleValidationResultsEnvelope;

@Service
@Data
@RequiredArgsConstructor
public class JsonSchemaValidationHandler {


    @NonNull
    private DataTypeRepository dataTypeRepository;
    @NonNull
    private ChecklistRepository checklistRepository;
    @NonNull
    private JsonSchemaValidationService validationService;
    @NonNull
    private ObjectMapper objectMapper;
    @NonNull
    private List<Class<? extends StoredSubmittable>> submittablesClassList;

    private ObjectMapper customObjectMapper = SchemaObjectMapperProvider.createCustomObjectMapper();


    private Map<String, SubmittableRepository<? extends StoredSubmittable>> repositoryByClassSimpleName(Map<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> submittableRepositoryMap) {
        Map<String, SubmittableRepository<? extends StoredSubmittable>> map = new HashMap<>();

        for (Map.Entry<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> entry : submittableRepositoryMap.entrySet()) {
            String className = entry.getKey().getSimpleName();
            map.put(className, entry.getValue());
        }
        return map;
    }

    public SingleValidationResultsEnvelope handleSubmittableValidation(SchemaValidationMessageEnvelope envelope) {

        DataType dataType = null;
        Checklist checklist = null;

        if (envelope.getDataTypeId() != null) {
            dataType = dataTypeRepository.findById(envelope.getDataTypeId()).orElse(null);
        }
        if (envelope.getChecklistId() != null) {
            checklist = checklistRepository.findById(envelope.getChecklistId()).orElse(null);
        }

        resolveMapperDifferences(envelope, dataType);

        List<JsonSchemaValidationError> errors = new ArrayList<>();

        JsonNode documentToValidate = envelope.getEntityToValidate();

        if (dataType != null && dataType.getValidationSchema() != null) {
            JsonNode schema = fixStoredJson(dataType.getValidationSchema());


            List<JsonSchemaValidationError> errors1 = validationService.validate(schema, documentToValidate);
            errors.addAll(errors1);
        }

        if (checklist != null && checklist.getValidationSchema() != null) {
            JsonNode schema = fixStoredJson(checklist.getValidationSchema());

            List<JsonSchemaValidationError> errors1 = validationService.validate(schema, documentToValidate);
            errors.addAll(errors1);
        }


        List<SingleValidationResult> singleValidationResultList = getSingleValidationResults(envelope, errors);
        return generateSingleValidationResultsEnvelope(envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID(), singleValidationResultList, ValidationAuthor.JsonSchema);
    }

    /* usi general object mapping config can be different to that required by the schema validator
            e.g. it writes local dates as a 3 value array, instead of a string
       rewrite using custom object mapper to fix
    */
    private void resolveMapperDifferences(SchemaValidationMessageEnvelope envelope, DataType dataType) {
        if (dataType != null && dataType.getSubmittableClassName() != null) {
            String submittableClassName = dataType.getSubmittableClassName();

            Optional<Class<? extends StoredSubmittable>> optionalClass = submittablesClassList.stream()
                    .filter(clazz -> clazz.getName().equals(submittableClassName))
                    .findAny();

            if (optionalClass.isPresent()) {

                StoredSubmittable submittable = null;
                try {
                    submittable = objectMapper.treeToValue(envelope.getEntityToValidate(), optionalClass.get());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                envelope.setEntityToValidate(customObjectMapper.valueToTree(submittable));
            }
        }
    }

    // -- Helper methods -- //
    private List<SingleValidationResult> getSingleValidationResults(SchemaValidationMessageEnvelope envelope, List<JsonSchemaValidationError> jsonSchemaValidationErrors) {
        List<SingleValidationResult> singleValidationResultList;
        if (jsonSchemaValidationErrors.isEmpty()) {
            singleValidationResultList = Arrays.asList(generatePassingSingleValidationResult(envelope.entityId(), ValidationAuthor.JsonSchema));
        } else {
            singleValidationResultList = convertToSingleValidationResultList(jsonSchemaValidationErrors, envelope.entityId());
        }
        return singleValidationResultList;
    }

    private List<SingleValidationResult> convertToSingleValidationResultList(List<JsonSchemaValidationError> errorList, String entityUuid) {
        List<SingleValidationResult> validationResults = new ArrayList<>();
        for (JsonSchemaValidationError error : errorList) {
            validationResults.add(generateSchemaSingleValidationResult(error, entityUuid));
        }
        return validationResults;
    }

    private SingleValidationResult generateSchemaSingleValidationResult(JsonSchemaValidationError error, String entityUuid) {
        SingleValidationResult validationResult = new SingleValidationResult();
        validationResult.setValidationAuthor(ValidationAuthor.JsonSchema);
        validationResult.setValidationStatus(SingleValidationResultStatus.Error);
        validationResult.setEntityUuid(entityUuid);
        validationResult.setMessage(error.getDataPath() + " error(s): " + error.getErrorsAsString());
        return validationResult;
    }


}
