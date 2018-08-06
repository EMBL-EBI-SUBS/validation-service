package uk.ac.ebi.subs.validator.schema;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.data.component.StudyDataType;
import uk.ac.ebi.subs.validator.coordinator.MesssageEnvelopeTestHelper;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
public class JsonSchemaValidationHandlerTest {

    JsonSchemaValidationHandler jsonSchemaValidationHandler;
    JsonSchemaValidationService jsonSchemaValidationService;

    private RestTemplate restTemplate = new RestTemplate();
    private SchemaService schemaService;

    @Before
    public void setUp() {
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            protected boolean hasError(HttpStatus statusCode) {
                return false;
            }
        });

        schemaService = new SchemaService(restTemplate);

        jsonSchemaValidationService = new JsonSchemaValidationService(restTemplate);
        jsonSchemaValidationService.setJsonSchemaValidator("https://subs-json-schema-validator.herokuapp.com/validate");

        jsonSchemaValidationHandler = new JsonSchemaValidationHandler(jsonSchemaValidationService, schemaService);
        
        jsonSchemaValidationHandler.setAssaySchemaUrl("https://raw.githubusercontent.com/EMBL-EBI-SUBS/validation-schemas/master/assay/assay-schema.json");
        jsonSchemaValidationHandler.setMlAssaySchemaUrl("https://raw.githubusercontent.com/EMBL-EBI-SUBS/validation-schemas/master/assay/ml-assay-schema.json");

        jsonSchemaValidationHandler.setStudySchemaUrl("https://raw.githubusercontent.com/EMBL-EBI-SUBS/validation-schemas/master/study/study-schema.json");
        jsonSchemaValidationHandler.setMlStudySchemaUrl("https://raw.githubusercontent.com/EMBL-EBI-SUBS/validation-schemas/master/study/ml-study-schema.json");
    }

    @Test
    public void handleStudyValidation() {
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = MesssageEnvelopeTestHelper.getStudyValidationMessageEnvelope();
        SingleValidationResultsEnvelope singleValidationResultsEnvelope = jsonSchemaValidationHandler.handleStudyValidation(studyValidationMessageEnvelope);

        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().size(), 7);
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().get(1).getMessage(), ".attributes.factors error(s): should have required property 'factors'.");
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().get(2).getMessage(), ".attributes.studyDesignDescriptors error(s): should have required property 'studyDesignDescriptors'.");

        studyValidationMessageEnvelope.getEntityToValidate().setStudyType(StudyDataType.Proteomics);
        singleValidationResultsEnvelope = jsonSchemaValidationHandler.handleStudyValidation(studyValidationMessageEnvelope);
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().size(), 4);

    }


    @Test
    public void handleAssayValidation() {
        AssayValidationMessageEnvelope assayValidationMessageEnvelope = MesssageEnvelopeTestHelper.getAssayValidationMessageEnvelope();
        SingleValidationResultsEnvelope singleValidationResultsEnvelope = jsonSchemaValidationHandler.handleAssayValidation(assayValidationMessageEnvelope);

        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().size(), 7);
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().get(0).getMessage(), ".attributes.technologyType error(s): should have required property 'technologyType'.");

        assayValidationMessageEnvelope.getStudy().getBaseSubmittable().setStudyType(StudyDataType.Proteomics);
        singleValidationResultsEnvelope = jsonSchemaValidationHandler.handleAssayValidation(assayValidationMessageEnvelope);
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().size(), 4);

    }
}