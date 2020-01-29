package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.TestUtils;
import uk.ac.ebi.subs.validator.coordinator.MessageEnvelopeTestHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.schema.model.JsonSchemaValidationError;
import uk.ac.ebi.subs.validator.schema.model.SchemaValidationMessageEnvelope;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
//@SpringBootTest()
public class JsonSchemaValidationHandlerTest {

    JsonSchemaValidationHandler jsonSchemaValidationHandler;
    JsonSchemaValidationService jsonSchemaValidationService;

    DataTypeRepository dataTypeRepository;
    ChecklistRepository checklistRepository;

    SchemaValidationMessageEnvelope schemaValidationMessageEnvelope;

    JsonSchemaValidationError error = new JsonSchemaValidationError(Collections.singletonList("fake error message"), "/a/fake/path");
    ObjectMapper objectMapper;

    DataType dataType;
    Checklist checklist;

    @Before
    public void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // emulate general usi mapping of local date as a 3 element array

        dataTypeRepository = Mockito.mock(DataTypeRepository.class);
        checklistRepository = Mockito.mock(ChecklistRepository.class);

        dataType = new DataType();
        dataType.setValidationSchema(jsonStringToNode("{\"#dollar#schema\": \"foo\"}"));
        dataType.setId("dt1");
        dataType.setSubmittableClassName(Sample.class.getName());

        checklist = new Checklist();
        checklist.setValidationSchema(jsonStringToNode("{\"#dollar#schema\": \"bar\"}"));
        checklist.setId("cl1");

        schemaValidationMessageEnvelope = MessageEnvelopeTestHelper.getSchemaValidationMessageEnveloper();
        schemaValidationMessageEnvelope.setDataTypeId(dataType.getId());
        schemaValidationMessageEnvelope.setChecklistId(checklist.getId());
        schemaValidationMessageEnvelope.setEntityToValidate( objectMapper.valueToTree(TestUtils.createStaticSampleWithReleaseDate(LocalDate.now())) );

        jsonSchemaValidationService = Mockito.mock(JsonSchemaValidationService.class);
        jsonSchemaValidationHandler = new JsonSchemaValidationHandler(
                dataTypeRepository,
                checklistRepository,
                jsonSchemaValidationService,
                objectMapper,
                Collections.singletonList(Sample.class)
        );
    }

    @Test
    public void handleSampleValidation() {
        Mockito.when(dataTypeRepository.findOne(dataType.getId())).thenReturn(dataType);
        Mockito.when(checklistRepository.findOne(checklist.getId())).thenReturn(checklist);

        JsonNode expectedDtSchema = jsonStringToNode("{\"$schema\": \"foo\"}");
        JsonNode expectedClSchema = jsonStringToNode("{\"$schema\": \"bar\"}");

        Mockito.when(jsonSchemaValidationService.validate(Mockito.eq(expectedDtSchema), Mockito.any())).thenReturn(
                Collections.singletonList(error)
        );
        Mockito.when(jsonSchemaValidationService.validate(Mockito.eq(expectedClSchema), Mockito.any())).thenReturn(
                Collections.singletonList(error)
        );

        SingleValidationResultsEnvelope singleValidationResultsEnvelope = jsonSchemaValidationHandler.handleSubmittableValidation(schemaValidationMessageEnvelope);

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