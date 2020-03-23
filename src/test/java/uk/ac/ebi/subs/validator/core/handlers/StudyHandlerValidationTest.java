package uk.ac.ebi.subs.validator.core.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StudyHandlerValidationTest {

    private StudyHandler studyHandler;

    @SpyBean
    private ReferenceValidator spyReferenceValidator;

    @MockBean
    private AttributeValidator attributeValidator;

    private final String studyId = "studyId";
    private final String validationResultId = "vrID";
    private final int validationVersion = 42;

    private StudyValidationMessageEnvelope envelope;

    private ProjectRef projectRef;

    private Study study;

    @MockBean
    private DataTypeRepository dataTypeRepository;

    private final String dataTypeId = "dataTypeId";
    private DataType dataType;


    @Before
    public void buildUp() {
        //setup the handler
        studyHandler = new StudyHandler(attributeValidator, spyReferenceValidator, dataTypeRepository);

        //refs
        projectRef = new ProjectRef();

        //entity to be validated
        study = new Study();
        study.setId(studyId);
        study.setProjectRef(projectRef);

        //dataType
        dataType = new DataType();
        dataType.setId(dataTypeId);

        mockRepoCalls();

        //envelope
        envelope = new StudyValidationMessageEnvelope();
        envelope.setValidationResultUUID(validationResultId);
        envelope.setValidationResultVersion(validationVersion);
        envelope.setEntityToValidate(study);
        envelope.setDataTypeId(dataTypeId);
    }

    @Test
    public void whenSubmittingStudyWithoutProjectReference_ThenValidationPass() {

        List<SingleValidationResult> validationResults = studyHandler.validateSubmittable(envelope);

        Assert.assertEquals(0, validationResults.size());
    }

    private void mockRepoCalls() {
        when(dataTypeRepository.findOne(dataTypeId))
                .thenReturn(dataType);
    }


}
