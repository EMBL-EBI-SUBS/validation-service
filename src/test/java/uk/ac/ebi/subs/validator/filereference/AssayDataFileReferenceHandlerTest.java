package uk.ac.ebi.subs.validator.filereference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.commonTestMethodForEntities;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.fail;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.getValidationResultForAssayDataFileReference;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.pass;

@RunWith(SpringRunner.class)
public class AssayDataFileReferenceHandlerTest {

    private FileReferenceHandler fileReferenceHandler;

    @MockBean
    private FileReferenceValidator fileReferenceValidator;

    private final String submissionId = "subID";
    private final String assayDataId = "assayDataID";
    private final String validationResultId = "vrID";
    private final int validationVersion = 42;

    private AssayDataValidationMessageEnvelope envelope;

    private AssayData assayData;
    private AssayRef assayRef;

    private Submittable<Assay> wrappedAssay;
    private Submittable<Sample> wrappedSample;


    @Before
    public void buildUp() {
        //setup the handler
        fileReferenceHandler = new FileReferenceHandler(fileReferenceValidator);

        //refs
        assayRef = new AssayRef();

        //entity to be validated
        assayData = new AssayData();
        assayData.setId(assayDataId);
        assayData.setAssayRefs(Collections.singletonList(assayRef));

        //reference data for the envelope
        Assay assay = new Assay();
        wrappedAssay = new Submittable<>(assay, submissionId);
        Sample sample = new Sample();
        wrappedSample = new Submittable<>(sample, submissionId);

        //envelope
        envelope = new AssayDataValidationMessageEnvelope();
        envelope.setValidationResultUUID(validationResultId);
        envelope.setValidationResultVersion(validationVersion);
        envelope.setEntityToValidate(assayData);
        envelope.setAssays(Collections.singletonList(wrappedAssay));
        envelope.setSubmissionId(submissionId);
    }

    @Test
    public void whenValidatorFails_ThenValidationResultStatusShouldBeError() {
        mockRefValidatorCalls(fail(assayDataId, ValidationAuthor.FileReference));

        SingleValidationResultsEnvelope resultsEnvelope = getValidationResultForAssayDataFileReference(fileReferenceHandler, envelope);

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(resultsEnvelope, envelope, validationResultId, validationVersion, assayDataId,
                        ValidationAuthor.FileReference);

        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());

    }

    @Test
    public void whenValidatorPasses_ThenValidationResultStatusShouldBePass() {
        mockRefValidatorCalls(pass(assayDataId, ValidationAuthor.FileReference));

        SingleValidationResultsEnvelope resultsEnvelope = getValidationResultForAssayDataFileReference(fileReferenceHandler, envelope);

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(resultsEnvelope, envelope, validationResultId, validationVersion, assayDataId,
                        ValidationAuthor.FileReference);

        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Pass, actualResults.get(0).getValidationStatus());
    }

    private void mockRefValidatorCalls(SingleValidationResult assayDataResult) {
        when(
                fileReferenceValidator.validate(assayData, submissionId, assayData.getId())
        ).thenReturn(
                Collections.singletonList(assayDataResult)
        );

    }
}