package uk.ac.ebi.subs.validator.core.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.filereference.FileReferenceValidator;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AssayDataHandlerTest {

    private AssayDataHandler assayDataHandler;

    @MockBean
    private ReferenceValidator referenceValidator;

    @MockBean
    private AttributeValidator attributeValidator;

    @MockBean
    private FileReferenceValidator fileReferenceValidator;

    @MockBean
    private DataTypeRepository dataTypeRepository;

    private final String dataTypeId = "dataTypeId";
    private DataType dataType;


    private final String assayDataId = "assayDataID";
    private final String validationResultId = "vrID";
    private final int validationVersion = 42;

    private AssayDataValidationMessageEnvelope envelope;

    private AssayRef assayRef;

    private Submittable<Assay> wrappedAssay;

    private AssayData assayData; //entity under validation

    @Before
    public void buildUp() {

        //setup the handler
        assayDataHandler = new AssayDataHandler(referenceValidator, attributeValidator, dataTypeRepository);

        //refs
        assayRef = new AssayRef();

        //entity to be validated
        assayData = new AssayData();
        assayData.setId(assayDataId);
        assayData.setAssayRefs(Arrays.asList(assayRef));

        //reference data for the envelope
        Assay assay = new Assay();
        String submissionId = "subID";
        wrappedAssay = new Submittable<>(assay, submissionId);

        //dataType
        dataType = new DataType();
        dataType.setId(dataTypeId);

        mockRepoCalls();

        //envelope
        envelope = new AssayDataValidationMessageEnvelope();
        envelope.setValidationResultUUID(validationResultId);
        envelope.setValidationResultVersion(validationVersion);
        envelope.setEntityToValidate(assayData);
        envelope.getAssays().add(wrappedAssay);
        envelope.setDataTypeId(dataTypeId);
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
    public void testHandler_assayRefCallsPasses() {
        mockRefValidatorCalls(pass());

        SingleValidationResultsEnvelope resultsEnvelope = assayDataHandler.handleValidationRequest(envelope);


        commonEnvelopeAsserts(resultsEnvelope);

        List<SingleValidationResult> actualResults = resultsEnvelope.getSingleValidationResults();

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Pass, actualResults.get(0).getValidationStatus());
    }


    @Test
    public void testHandler_assayRefCallFails() {
        mockRefValidatorCalls(fail());

        SingleValidationResultsEnvelope resultsEnvelope = assayDataHandler.handleValidationRequest(envelope);


        commonEnvelopeAsserts(resultsEnvelope);

        List<SingleValidationResult> actualResults = resultsEnvelope.getSingleValidationResults();

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
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

    private void mockRefValidatorCalls(SingleValidationResult assayResult) {


        when(
                referenceValidator.validate(assayData, dataType, envelope.getEntityToValidate().getAssayRefs(), envelope.getAssays())
        ).thenReturn(
                Arrays.asList(assayResult)
        );
    }

    private void mockRepoCalls() {
        when(dataTypeRepository.findOne(dataTypeId))
                .thenReturn(dataType);
    }
}
