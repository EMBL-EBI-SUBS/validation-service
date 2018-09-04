package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.schema.custom.LocalDateCustomSerializer;
import uk.ac.ebi.subs.validator.schema.model.JsonSchemaValidationError;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.ebi.subs.validator.util.ValidationHelper.generatePassingSingleValidationResult;
import static uk.ac.ebi.subs.validator.util.ValidationHelper.generateSingleValidationResultsEnvelope;

@Service
@Data
@RequiredArgsConstructor
public class JsonSchemaValidationHandler {

    private JsonSchemaValidationService validationService;
    private ObjectMapper mapper = new ObjectMapper();
    private SimpleModule module = new SimpleModule();
    private DataTypeRepository dataTypeRepository;
    private ChecklistRepository checklistRepository;

    public JsonSchemaValidationHandler(
            DataTypeRepository dataTypeRepository,
            ChecklistRepository checklistRepository,
            JsonSchemaValidationService validationService) {
        this.dataTypeRepository = dataTypeRepository;
        this.checklistRepository = checklistRepository;
        this.validationService = validationService;
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY); // Null fields and empty collections are not included in the serialization.
        this.module.addSerializer(LocalDate.class, new LocalDateCustomSerializer());
        this.mapper.registerModule(module);
    }

    private Map<String, SubmittableRepository<? extends StoredSubmittable>> repositoryByClassSimpleName(Map<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> submittableRepositoryMap) {
        Map<String, SubmittableRepository<? extends StoredSubmittable>> map = new HashMap<>();

        for (Map.Entry<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> entry : submittableRepositoryMap.entrySet()) {
            String className = entry.getKey().getSimpleName();
            map.put(className, entry.getValue());
        }
        return map;
    }

    public SingleValidationResultsEnvelope handleSubmittableValidation(ValidationMessageEnvelope envelope) {
        Submittable submittable = envelope.getEntityToValidate();

        DataType dataType = null;
        Checklist checklist = null;

        if (envelope.getDataTypeId() != null){
            dataType = dataTypeRepository.findOne(envelope.getDataTypeId());
        }
        if (envelope.getChecklistId() != null){
            checklist = checklistRepository.findOne(envelope.getChecklistId());
        }

        List<JsonSchemaValidationError> errors = new ArrayList<>();

        JsonNode documentToValidate = mapper.valueToTree(submittable);

        if (dataType != null && dataType.getValidationSchema() != null) {
            JsonNode schema = fixSchemaKey(dataType.getValidationSchema());


            List<JsonSchemaValidationError> errors1 = validationService.validate(schema, documentToValidate);
            errors.addAll(errors1);
        }

        if (checklist != null && checklist.getValidationSchema() != null) {
            JsonNode schema = fixSchemaKey(checklist.getValidationSchema());

            List<JsonSchemaValidationError> errors1 = validationService.validate(schema, documentToValidate);
            errors.addAll(errors1);
        }


        List<SingleValidationResult> singleValidationResultList = getSingleValidationResults(envelope, errors);
        return generateSingleValidationResultsEnvelope(envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID(), singleValidationResultList, ValidationAuthor.JsonSchema);
    }

    /**
     * Our current version of Mongo cannot store documents with keys that start with '$'
     * JSON schema must contain a '$schema' key.
     * <p>
     * We re-write the schema, to convert 'schema' to '$schema'
     *
     * @param storedSchemaJson
     * @return the fixed schema, converted to a JsonNode
     */
    private JsonNode fixSchemaKey(String storedSchemaJson) {
        try {
            ObjectNode schemaAsNode = mapper.readValue(storedSchemaJson, ObjectNode.class);
            JsonNode value = schemaAsNode.remove("schema");
            schemaAsNode.set("$schema", value);
            return schemaAsNode;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    // -- Helper methods -- //
    private List<SingleValidationResult> getSingleValidationResults(ValidationMessageEnvelope envelope, List<JsonSchemaValidationError> jsonSchemaValidationErrors) {
        List<SingleValidationResult> singleValidationResultList;
        if (jsonSchemaValidationErrors.isEmpty()) {
            singleValidationResultList = Arrays.asList(generatePassingSingleValidationResult(envelope.getEntityToValidate().getId(), ValidationAuthor.JsonSchema));
        } else {
            singleValidationResultList = convertToSingleValidationResultList(jsonSchemaValidationErrors, envelope.getEntityToValidate().getId());
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
