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
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
public class JsonSchemaValidationHandlerTest {

    JsonSchemaValidationHandler jsonSchemaValidationHandler;
    JsonSchemaValidationService jsonSchemaValidationService;

    private RestTemplate restTemplate = new RestTemplate();
    private SchemaService schemaService;

    @Before
    public void setUp() {
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            protected boolean hasError(HttpStatus statusCode) {
                return false;
            }});

        schemaService = new SchemaService(restTemplate);

        jsonSchemaValidationService = new JsonSchemaValidationService(restTemplate);
        jsonSchemaValidationService.setJsonSchemaValidator("https://subs-json-schema-validator.herokuapp.com/validate");

        jsonSchemaValidationHandler = new JsonSchemaValidationHandler(jsonSchemaValidationService, schemaService);
        jsonSchemaValidationHandler.setMlSampleSchemaUrl("https://raw.githubusercontent.com/EMBL-EBI-SUBS/validation-schemas/master/sample/ml-sample-schema.json");
        jsonSchemaValidationHandler.setSampleSchemaUrl("https://raw.githubusercontent.com/EMBL-EBI-SUBS/validation-schemas/master/sample/sample-schema.json");
    }

    @Test
    public void handleSampleValidation() {
        SampleValidationMessageEnvelope sampleValidationEnvelope = MesssageEnvelopeTestHelper.getSampleValidationEnvelope();
        sampleValidationEnvelope.setStudyDataType(StudyDataType.Metabolomics_LCMS);
        SingleValidationResultsEnvelope singleValidationResultsEnvelope = jsonSchemaValidationHandler.handleSampleValidation(sampleValidationEnvelope);

        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().size(), 2);
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().get(0).getMessage(),".attributes.Organism error(s): should have required property 'Organism'.");
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().get(1).getMessage(),".attributes.Organism part error(s): should have required property 'Organism part'.");

        sampleValidationEnvelope.setStudyDataType(StudyDataType.Sequencing);
        singleValidationResultsEnvelope = jsonSchemaValidationHandler.handleSampleValidation(sampleValidationEnvelope);
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().size(), 1);
        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus(), SingleValidationResultStatus.Pass);
    }
}