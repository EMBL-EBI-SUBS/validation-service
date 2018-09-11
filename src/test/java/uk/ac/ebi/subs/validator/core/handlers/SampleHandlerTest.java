package uk.ac.ebi.subs.validator.core.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.commonTestMethodForEntities;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.fail;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.getValidationResultFromSubmittables;
import static uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper.pass;

@RunWith(SpringRunner.class)
public class SampleHandlerTest {

    private SampleHandler sampleHandler;

    @MockBean
    private ReferenceValidator referenceValidator;

    @MockBean
    private AttributeValidator attributeValidator;

    @MockBean
    private DataTypeRepository dataTypeRepository;

    private final String sampleId = "sampleId";
    private final String validationResultId = "vrID";
    private final int validationVersion = 42;
    private static final ValidationAuthor VALIDATION_AUTHOR_CORE = ValidationAuthor.Core;

    private SampleValidationMessageEnvelope envelope;

    private Sample sample; //entity to be validated

    private SampleRelationship sampleRelationship;
    private Submittable<Sample> wrappedSample;

    private final String dataTypeId = "dataTypeId";
    private DataType dataType;

    @Before
    public void buildUp() {
        //setup the handler
        sampleHandler = new SampleHandler(referenceValidator, attributeValidator, dataTypeRepository);

        //refs
        sampleRelationship = new SampleRelationship();

        //entity to be validated
        sample = new Sample();
        sample.setId(sampleId);
        sample.setSampleRelationships(Arrays.asList(sampleRelationship));

        //reference data for the envelope
        Sample referencedSample = new Sample();
        String submissionId = "subID";
        wrappedSample = new Submittable<>(referencedSample, submissionId);

        //dataType
        dataType = new DataType();
        dataType.setId(dataTypeId);

        mockRepoCalls();

        //envelope
        envelope = new SampleValidationMessageEnvelope();
        envelope.setValidationResultUUID(validationResultId);
        envelope.setValidationResultVersion(validationVersion);
        envelope.setEntityToValidate(sample);
        envelope.setSampleList(Arrays.asList(wrappedSample));
        envelope.setDataTypeId(dataTypeId);

    }

    @Test
    public void testHandler_pass() {
        mockValidatorCalls(pass(sampleId, VALIDATION_AUTHOR_CORE));

        SingleValidationResultsEnvelope resultsEnvelope = getValidationResultFromSubmittables(sampleHandler, envelope);

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(resultsEnvelope, envelope, validationResultId, validationVersion, sampleId,
                        VALIDATION_AUTHOR_CORE);

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Pass, actualResults.get(0).getValidationStatus());
    }

    @Test
    public void testHandler_partialFailure() {
        mockValidatorCalls(fail(sampleId, VALIDATION_AUTHOR_CORE), pass(sampleId, VALIDATION_AUTHOR_CORE));

        SingleValidationResultsEnvelope resultsEnvelope = getValidationResultFromSubmittables(sampleHandler, envelope);

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(resultsEnvelope, envelope, validationResultId, validationVersion, sampleId,
                        VALIDATION_AUTHOR_CORE);

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(1, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
    }

    @Test
    public void testHandler_bothFail() {
        mockValidatorCalls(fail(sampleId, VALIDATION_AUTHOR_CORE), fail(sampleId, VALIDATION_AUTHOR_CORE));

        SingleValidationResultsEnvelope resultsEnvelope = getValidationResultFromSubmittables(sampleHandler, envelope);

        List<SingleValidationResult> actualResults =
                commonTestMethodForEntities(resultsEnvelope, envelope, validationResultId, validationVersion, sampleId,
                        VALIDATION_AUTHOR_CORE);

        //there should be one result (even though the handler received two passes) and it should be a pass
        Assert.assertEquals(2, actualResults.size());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(0).getValidationStatus());
        Assert.assertEquals(SingleValidationResultStatus.Error, actualResults.get(1).getValidationStatus());
    }

    private void mockRepoCalls() {
        when(dataTypeRepository.findOne(dataTypeId))
                .thenReturn(dataType);
    }

    private void mockValidatorCalls(SingleValidationResult... sampleResults) {
        when(
                referenceValidator.validate(
                        sample,
                        dataType,
                        Arrays.asList(sampleRelationship),
                        Arrays.asList(wrappedSample))
        ).thenReturn(
                Arrays.asList(sampleResults)
        );
    }
}
