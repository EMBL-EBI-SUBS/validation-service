package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.coordinator.MessageEnvelopeTestHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.schema.model.JsonSchemaValidationError;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
public class JsonSchemaValidationHandlerTest {

    JsonSchemaValidationHandler jsonSchemaValidationHandler;
    JsonSchemaValidationService jsonSchemaValidationService;

    DataTypeRepository dataTypeRepository;
    ChecklistRepository checklistRepository;

    StudyValidationMessageEnvelope studyValidationMessageEnvelope;

    JsonSchemaValidationError error = new JsonSchemaValidationError(Arrays.asList("fake error message"), "/a/fake/path");

    DataType dataType;
    Checklist checklist;

    @Before
    public void setUp() {
        dataTypeRepository = Mockito.mock(DataTypeRepository.class);
        checklistRepository = Mockito.mock(ChecklistRepository.class);

        dataType = new DataType();
        dataType.setValidationSchema(jsonStringToNode("{\"schema\": \"foo\"}"));
        dataType.setId("dt1");

        checklist = new Checklist();
        checklist.setValidationSchema(jsonStringToNode("{\"schema\": \"bar\"}"));
        checklist.setId("cl1");

        studyValidationMessageEnvelope = MessageEnvelopeTestHelper.getStudyValidationMessageEnvelope();
        studyValidationMessageEnvelope.setDataTypeId(dataType.getId());
        studyValidationMessageEnvelope.setChecklistId(checklist.getId());

        jsonSchemaValidationService = Mockito.mock(JsonSchemaValidationService.class);
        jsonSchemaValidationHandler = new JsonSchemaValidationHandler(dataTypeRepository, checklistRepository, jsonSchemaValidationService);

    }


    @Test
    public void handleStudyValidation() {
        Mockito.when(dataTypeRepository.findOne(dataType.getId())).thenReturn(dataType);
        Mockito.when(checklistRepository.findOne(checklist.getId())).thenReturn(checklist);



        JsonNode expectedDtSchema = jsonStringToNode("{\"$schema\": \"foo\"}");
        JsonNode expectedClSchema = jsonStringToNode("{\"$schema\": \"bar\"}");


        Mockito.when(jsonSchemaValidationService.validate(Mockito.eq(expectedDtSchema), Mockito.any())).thenReturn(
                Arrays.asList(
                        error
                )
        );
        Mockito.when(jsonSchemaValidationService.validate(Mockito.eq(expectedClSchema), Mockito.any())).thenReturn(
                Arrays.asList(
                        error
                )
        );


        SingleValidationResultsEnvelope singleValidationResultsEnvelope = jsonSchemaValidationHandler.handleSubmittableValidation(studyValidationMessageEnvelope);


        Mockito.verify(jsonSchemaValidationService, Mockito.times(1)).validate(Mockito.eq(expectedDtSchema), Mockito.any());

        Mockito.verify(jsonSchemaValidationService, Mockito.times(1)).validate(Mockito.eq(expectedClSchema), Mockito.any());


        singleValidationResultsEnvelope.getSingleValidationResults();


        assertEquals(singleValidationResultsEnvelope.getSingleValidationResults().size(), 2);

    }

    private ObjectNode jsonStringToNode(String str) {
        try {
            return new ObjectMapper().readValue(str, ObjectNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}