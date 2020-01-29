package uk.ac.ebi.subs.validator.core.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.commonTestMethodForEntities;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.fail;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.getValidationResultFromSubmittables;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.pass;

@RunWith(SpringRunner.class)
public class AssayHandlerTest {

    private AssayHandler assayHandler;

    @MockBean
    private ReferenceValidator referenceValidator;

    @MockBean
    private AttributeValidator attributeValidator;

    private final String assayId = "assayId";
    private final String validationResultId = "vrID";
    private final int validationVersion = 42;
    private static final ValidationAuthor VALIDATION_AUTHOR_CORE = ValidationAuthor.Core;

    @MockBean
    private DataTypeRepository dataTypeRepository;

    private final String dataTypeId = "dataTypeId";
    private DataType dataType;

    private AssayValidationMessageEnvelope envelope;

    private StudyRef studyRef;
    private SampleRef sampleRef;
    private Assay assay; //entity under validation

    private Submittable<Sample> wrappedSample;
    private Submittable<Study> wrappedStudy;

    @Before
    public void buildUp() {

        //setup the handler
        assayHandler = new AssayHandler(referenceValidator, attributeValidator,dataTypeRepository);

        //refs
        studyRef = new StudyRef();
        sampleRef = new SampleRef();

        SampleUse sampleUse = new SampleUse();
        sampleUse.setSampleRef(sampleRef);

        //entity to be validated
        assay = new Assay();
        assay.setId(assayId);
        assay.setStudyRef(studyRef);
        assay.setSampleUses(Collections.singletonList(
                sampleUse
        ));

        //reference data for the envelope
        Study study = new Study();
        String submissionId = "subID";
        wrappedStudy = new Submittable<>(study, submissionId);
        Sample sample = new Sample();
        wrappedSample = new Submittable<>(sample, submissionId);

        //dataType
        dataType = new DataType();
        dataType.setId(dataTypeId);

        mockRepoCalls();

        //envelope
        envelope = new AssayValidationMessageEnvelope();
        envelope.setValidationResultUUID(validationResultId);
        envelope.setValidationResultVersion(validationVersion);
        envelope.setEntityToValidate(assay);
        envelope.setStudy(wrappedStudy);
        envelope.setSampleList(Collections.singletonList(wrappedSample));
        envelope.setDataTypeId(dataTypeId);
    }

    @Test
    public void testHandler_bothRefCallsPass() {
        mockRefValidatorCalls(pass(assayId, VALIDATION_AUTHOR_CORE), pass(assayId, VALIDATION_AUTHOR_CORE));

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(getValidationResultFromSubmittables(assayHandler, envelope),
                        envelope, validationResultId, validationVersion, assayId, VALIDATION_AUTHOR_CORE);

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Pass, actualResults.get(0).getValidationStatus());
    }

    @Test
    public void testHandler_sampleFails() {
        mockRefValidatorCalls(fail(assayId, VALIDATION_AUTHOR_CORE), pass(assayId, VALIDATION_AUTHOR_CORE));

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(getValidationResultFromSubmittables(assayHandler, envelope),
                        envelope, validationResultId, validationVersion, assayId, VALIDATION_AUTHOR_CORE);

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
    }

    @Test
    public void testHandler_assayFails() {
        mockRefValidatorCalls(pass(assayId, VALIDATION_AUTHOR_CORE), fail(assayId, VALIDATION_AUTHOR_CORE));

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(getValidationResultFromSubmittables(assayHandler, envelope),
                        envelope, validationResultId, validationVersion, assayId, VALIDATION_AUTHOR_CORE);

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
    }

    @Test
    public void testHandler_bothFail() {
        mockRefValidatorCalls(fail(assayId, VALIDATION_AUTHOR_CORE), fail(assayId, VALIDATION_AUTHOR_CORE));

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(getValidationResultFromSubmittables(assayHandler, envelope),
                        envelope, validationResultId, validationVersion, assayId, VALIDATION_AUTHOR_CORE);

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(2, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(1).getValidationStatus());
    }


    private void mockRefValidatorCalls(SingleValidationResult studyResult, SingleValidationResult sampleresult) {
        when(
                referenceValidator.validate(assay, dataType, studyRef, wrappedStudy)
        ).thenReturn(
                Collections.singletonList(studyResult)
        );

        when(
                referenceValidator.validate(assay, dataType, Collections.singletonList(sampleRef),
                        Collections.singletonList(wrappedSample))
        ).thenReturn(
                Collections.singletonList(sampleresult)
        );
    }

    private void mockRepoCalls() {
        when(dataTypeRepository.findOne(dataTypeId))
                .thenReturn(dataType);
    }
}
