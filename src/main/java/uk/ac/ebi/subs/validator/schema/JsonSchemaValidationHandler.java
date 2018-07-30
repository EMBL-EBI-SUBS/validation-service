package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.schema.custom.LocalDateCustomSerializer;
import uk.ac.ebi.subs.validator.schema.model.JsonSchemaValidationError;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.ac.ebi.subs.validator.util.ValidationHelper.generatePassingSingleValidationResult;
import static uk.ac.ebi.subs.validator.util.ValidationHelper.generateSingleValidationResultsEnvelope;

@Service
public class JsonSchemaValidationHandler {

    // Temporary solution - schema url should be provided not hardcoded
    @Value("${sample.schema.url}")
    private String sampleSchemaUrl;
    @Value("${mlsample.schema.url}")
    private String mlSampleSchemaUrl;
    @Value("${study.schema.url}")
    private String studySchemaUrl;
    @Value("${assay.schema.url}")
    private String assaySchemaUrl;
    @Value("${assaydata.schema.url}")
    private String assayDataSchemaUrl;
    @Value("${eva.seqvar.analysis.schema.url}")
    private String evaSeqVarAnalysisSchemaUrl;

    private JsonSchemaValidationService validationService;
    private SchemaService schemaService;
    private ObjectMapper mapper = new ObjectMapper();
    private SimpleModule module = new SimpleModule();

    public JsonSchemaValidationHandler(JsonSchemaValidationService validationService, SchemaService schemaService) {
        this.validationService = validationService;
        this.schemaService = schemaService;
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY); // Null fields and empty collections are not included in the serialization.
        this.module.addSerializer(LocalDate.class, new LocalDateCustomSerializer());
        this.mapper.registerModule(module);
    }

    public SingleValidationResultsEnvelope handleSampleValidation(SampleValidationMessageEnvelope envelope) {
        JsonNode sampleSchema = schemaService.getSchemaFor(envelope.getEntityToValidate().getClass().getTypeName(), sampleSchemaUrl); // TODO - handle logic on which schema to use for validation

        List<JsonSchemaValidationError> jsonSchemaValidationErrors = validationService.validate(sampleSchema, mapper.valueToTree(envelope.getEntityToValidate()));
        List<SingleValidationResult> singleValidationResultList = getSingleValidationResults(envelope, jsonSchemaValidationErrors);

        return generateSingleValidationResultsEnvelope(envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID(), singleValidationResultList, ValidationAuthor.JsonSchema);
    }

    public SingleValidationResultsEnvelope handleStudyValidation(StudyValidationMessageEnvelope envelope)  {
        JsonNode studySchema = schemaService.getSchemaFor(envelope.getEntityToValidate().getClass().getTypeName(), studySchemaUrl); // TODO - handle logic on which schema to use for validation

        List<JsonSchemaValidationError> jsonSchemaValidationErrors = validationService.validate(studySchema, mapper.valueToTree(envelope.getEntityToValidate()));
        List<SingleValidationResult> singleValidationResultList = getSingleValidationResults(envelope, jsonSchemaValidationErrors);

        return generateSingleValidationResultsEnvelope(envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID(), singleValidationResultList, ValidationAuthor.JsonSchema);
    }

    public SingleValidationResultsEnvelope handleAssayValidation(AssayValidationMessageEnvelope envelope)  {
        JsonNode assaySchema = schemaService.getSchemaFor(envelope.getEntityToValidate().getClass().getTypeName(), assaySchemaUrl); // TODO - handle logic on which schema to use for validation

        List<JsonSchemaValidationError> jsonSchemaValidationErrors = validationService.validate(assaySchema, mapper.valueToTree(envelope.getEntityToValidate()));
        List<SingleValidationResult> singleValidationResultList = getSingleValidationResults(envelope, jsonSchemaValidationErrors);

        return generateSingleValidationResultsEnvelope(envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID(), singleValidationResultList, ValidationAuthor.JsonSchema);
    }

    public SingleValidationResultsEnvelope handleAssayDataValidation(AssayDataValidationMessageEnvelope envelope)  {
        JsonNode assayDataSchema = schemaService.getSchemaFor(envelope.getEntityToValidate().getClass().getTypeName(), assayDataSchemaUrl); // TODO - handle logic on which schema to use for validation

        List<JsonSchemaValidationError> jsonSchemaValidationErrors = validationService.validate(assayDataSchema, mapper.valueToTree(envelope.getEntityToValidate()));
        List<SingleValidationResult> singleValidationResultList = getSingleValidationResults(envelope, jsonSchemaValidationErrors);

        return generateSingleValidationResultsEnvelope(envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID(), singleValidationResultList, ValidationAuthor.JsonSchema);
    }

    public SingleValidationResultsEnvelope handleAnalysisValidation(AnalysisValidationEnvelope envelope)  {
        JsonNode analysisDataSchema = schemaService.getSchemaFor(envelope.getEntityToValidate().getClass().getTypeName(), evaSeqVarAnalysisSchemaUrl); // TODO - handle logic on which schema to use for validation

        List<JsonSchemaValidationError> jsonSchemaValidationErrors = validationService.validate(analysisDataSchema, mapper.valueToTree(envelope.getEntityToValidate()));
        List<SingleValidationResult> singleValidationResultList = getSingleValidationResults(envelope, jsonSchemaValidationErrors);

        return generateSingleValidationResultsEnvelope(envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID(), singleValidationResultList, ValidationAuthor.JsonSchema);
    }

    // -- Helper methods -- //
    private List<SingleValidationResult> getSingleValidationResults(ValidationMessageEnvelope envelope, List<JsonSchemaValidationError> jsonSchemaValidationErrors) {
        List<SingleValidationResult> singleValidationResultList;
        if(jsonSchemaValidationErrors.isEmpty()) {
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
