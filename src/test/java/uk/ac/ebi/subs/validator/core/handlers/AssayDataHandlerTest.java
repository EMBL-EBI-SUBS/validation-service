package uk.ac.ebi.subs.validator.core.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AssayDataHandlerTest {

    private AssayDataHandler assayDataHandler;

    @MockBean
    private ReferenceValidator referenceValidator;

    @MockBean
    private AttributeValidator attributeValidator;

    private final String assayDataId = "assayDataID";
    private final String validationResultId = "vrID";
    private final int validationVersion = 42;

    private AssayDataValidationMessageEnvelope envelope;

    private AssayRef assayRef;
    private SampleRef sampleRef;

    private Submittable<Assay> wrappedAssay;
    private Submittable<Sample> wrappedSample;

    @Before
    public void buildUp() {

        //setup the handler
        assayDataHandler = new AssayDataHandler(referenceValidator, attributeValidator);

        //refs
        assayRef = new AssayRef();
        sampleRef = new SampleRef();

        //entity to be validated
        AssayData assayData = new AssayData();
        assayData.setId(assayDataId);
        assayData.setAssayRef(assayRef);
        assayData.setSampleRef(sampleRef);

        //reference data for the envelope
        Assay assay = new Assay();
        String submissionId = "subID";
        wrappedAssay = new Submittable<>(assay, submissionId);
        Sample sample = new Sample();
        wrappedSample = new Submittable<>(sample, submissionId);

        //envelope
        envelope = new AssayDataValidationMessageEnvelope();
        envelope.setValidationResultUUID(validationResultId);
        envelope.setValidationResultVersion(validationVersion);
        envelope.setEntityToValidate(assayData);
        envelope.setAssay(wrappedAssay);
        envelope.setSample(wrappedSample);
    }

    private SingleValidationResult pass() {
        return createResult(SingleValidationResultStatus.Pass);
    }

    private SingleValidationResult fail() {
        return createResult(SingleValidationResultStatus.Error);
    }

    private SingleValidationResult createResult(SingleValidationResultStatus status) {
        SingleValidationResult result = new SingleValidationResult();
        result.setEntityUuid(assayDataId);
        result.setValidationStatus(status);
        result.setValidationAuthor(ValidationAuthor.Core);
        return result;
    }

    @Test
    public void testHandler_bothPass() {
        when(
                referenceValidator.validate(assayDataId, sampleRef, wrappedSample)
        ).thenReturn(
                pass()
        );

        when(
                referenceValidator.validate(assayDataId, assayRef, wrappedAssay)
        ).thenReturn(
                pass()
        );

        SingleValidationResultsEnvelope resultsEnvelope = assayDataHandler.handleValidationRequest(envelope);


        commonEnvelopeAsserts(resultsEnvelope);

        List<SingleValidationResult> actualResults = resultsEnvelope.getSingleValidationResults();

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Pass, actualResults.get(0).getValidationStatus());
    }

    @Test
    public void testHandler_sampleFails() {
        when(
                referenceValidator.validate(assayDataId, sampleRef, wrappedSample)
        ).thenReturn(
                fail()
        );

        when(
                referenceValidator.validate(assayDataId, assayRef, wrappedAssay)
        ).thenReturn(
                pass()
        );

        SingleValidationResultsEnvelope resultsEnvelope = assayDataHandler.handleValidationRequest(envelope);


        commonEnvelopeAsserts(resultsEnvelope);

        List<SingleValidationResult> actualResults = resultsEnvelope.getSingleValidationResults();

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
    }

    @Test
    public void testHandler_assayFails() {
        when(
                referenceValidator.validate(assayDataId, sampleRef, wrappedSample)
        ).thenReturn(
                pass()
        );

        when(
                referenceValidator.validate(assayDataId, assayRef, wrappedAssay)
        ).thenReturn(
                fail()
        );

        SingleValidationResultsEnvelope resultsEnvelope = assayDataHandler.handleValidationRequest(envelope);


        commonEnvelopeAsserts(resultsEnvelope);

        List<SingleValidationResult> actualResults = resultsEnvelope.getSingleValidationResults();

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
    }

    @Test
    public void testHandler_bothFail() {
        when(
                referenceValidator.validate(assayDataId, sampleRef, wrappedSample)
        ).thenReturn(
                fail()
        );

        when(
                referenceValidator.validate(assayDataId, assayRef, wrappedAssay)
        ).thenReturn(
                fail()
        );

        SingleValidationResultsEnvelope resultsEnvelope = assayDataHandler.handleValidationRequest(envelope);


        commonEnvelopeAsserts(resultsEnvelope);

        List<SingleValidationResult> actualResults = resultsEnvelope.getSingleValidationResults();

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(2, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(1).getValidationStatus());
    }

    private void commonEnvelopeAsserts(SingleValidationResultsEnvelope resultsEnvelope) {
        Assert.assertNotNull(resultsEnvelope);
        Assert.assertNotNull(resultsEnvelope.getSingleValidationResults());
        Assert.assertEquals(ValidationAuthor.Core, resultsEnvelope.getValidationAuthor());
        Assert.assertEquals(validationResultId, envelope.getValidationResultUUID());
        Assert.assertEquals(validationVersion, envelope.getValidationResultVersion());

        for (SingleValidationResult result : resultsEnvelope.getSingleValidationResults()) {
            Assert.assertEquals(assayDataId, result.getEntityUuid());
        }

    }
}