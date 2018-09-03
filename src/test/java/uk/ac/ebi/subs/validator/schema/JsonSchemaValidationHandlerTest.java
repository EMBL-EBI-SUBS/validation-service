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
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;
import uk.ac.ebi.subs.validator.coordinator.MesssageEnvelopeTestHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.schema.model.JsonSchemaValidationError;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
public class JsonSchemaValidationHandlerTest {

    JsonSchemaValidationHandler jsonSchemaValidationHandler;
    JsonSchemaValidationService jsonSchemaValidationService;

    StudyRepository studyRepository;

    StudyValidationMessageEnvelope studyValidationMessageEnvelope;

    JsonSchemaValidationError error = new JsonSchemaValidationError(Arrays.asList("fake error message"), "/a/fake/path");

    @Before
    public void setUp() {
        studyValidationMessageEnvelope = MesssageEnvelopeTestHelper.getStudyValidationMessageEnvelope();


        studyRepository = Mockito.mock(StudyRepository.class);

        Map<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> submittableRepositoryMap = new HashMap<>();
        submittableRepositoryMap.put(Study.class, studyRepository);

        DataType dataType = new DataType();
        Checklist checklist = new Checklist();

        dataType.setValidationSchema(jsonStringToNode("{\"schema\": \"foo\"}"));
        checklist.setValidationSchema(jsonStringToNode("{\"schema\": \"bar\"}"));


        Study study = new Study();
        study.setDataType(dataType);
        study.setChecklist(checklist);


        Mockito.when(studyRepository.findOne(studyValidationMessageEnvelope.getEntityToValidate().getId())).thenReturn(
                study
        );


        jsonSchemaValidationService = Mockito.mock(JsonSchemaValidationService.class);
        jsonSchemaValidationHandler = new JsonSchemaValidationHandler(submittableRepositoryMap, jsonSchemaValidationService);

    }


    @Test
    public void handleStudyValidation() {

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

        Mockito.verify(jsonSchemaValidationService,Mockito.times(1)).validate(Mockito.eq(expectedDtSchema),Mockito.any());

        Mockito.verify(jsonSchemaValidationService,Mockito.times(1)).validate(Mockito.eq(expectedClSchema),Mockito.any());


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