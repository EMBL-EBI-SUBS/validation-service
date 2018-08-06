package uk.ac.ebi.subs.validator.core.handlers;

import org.junit.Assert;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.FileUploadValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.filereference.FileReferenceHandler;
import uk.ac.ebi.subs.validator.filereference.FileReferenceValidationDTO;

import java.util.List;

public final class ValidationTestHelper {

    public static SingleValidationResult pass(String entityUuid, ValidationAuthor validationAuthor) {
        return createResult(SingleValidationResultStatus.Pass, entityUuid, validationAuthor);
    }

    public static SingleValidationResult fail(String entityUuid, ValidationAuthor validationAuthor) {
        return createResult(SingleValidationResultStatus.Error, entityUuid, validationAuthor);
    }

    public static SingleValidationResultsEnvelope getValidationResultFromSubmittables(
            AbstractHandler handler, ValidationMessageEnvelope envelope) {

        return handler.handleValidationRequest(envelope);
    }

    public static SingleValidationResultsEnvelope getValidationResultForAssayDataFileReference(
            FileReferenceHandler handler, AssayDataValidationMessageEnvelope envelope) {

        FileReferenceValidationDTO validationDTO = new FileReferenceValidationDTO(
                envelope.getEntityToValidate(), envelope.getSubmissionId(), envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID());


        return handler.handleValidationRequestForAssayData(validationDTO);
    }

    public static SingleValidationResultsEnvelope getValidationResultForUploadedFileFileReference(
            FileReferenceHandler handler, FileUploadValidationMessageEnvelope envelope) {

        FileReferenceValidationDTO validationDTO = new FileReferenceValidationDTO(
                envelope.getfileToValidate(), envelope.getSubmissionId(), envelope.getValidationResultVersion(),
                envelope.getValidationResultUUID());


        return handler.handleValidationRequestForUploadedFile(validationDTO);
    }


    public static List<SingleValidationResult> commonTestMethodForEntities(SingleValidationResultsEnvelope resultsEnvelope,
                                                                           ValidationMessageEnvelope envelope,
                                                                           String validationResultId, int validationVersion,
                                                                           String entityUuid, ValidationAuthor validationAuthor) {

        Assert.assertNotNull(resultsEnvelope);
        Assert.assertNotNull(resultsEnvelope.getSingleValidationResults());
        Assert.assertEquals(validationAuthor, resultsEnvelope.getValidationAuthor());
        Assert.assertEquals(validationResultId, envelope.getValidationResultUUID());
        Assert.assertEquals(validationVersion, envelope.getValidationResultVersion());

        for (SingleValidationResult result : resultsEnvelope.getSingleValidationResults()) {
            Assert.assertEquals(entityUuid, result.getEntityUuid());
        }

        return resultsEnvelope.getSingleValidationResults();
    }

    public static List<SingleValidationResult> commonTestMethodForFiles(SingleValidationResultsEnvelope resultsEnvelope,
                                                                           FileUploadValidationMessageEnvelope envelope,
                                                                           String validationResultId, int validationVersion,
                                                                           String fileID, ValidationAuthor validationAuthor) {

        Assert.assertEquals(validationResultId, envelope.getValidationResultUUID());
        Assert.assertEquals(validationVersion, envelope.getValidationResultVersion());
        Assert.assertNotNull(resultsEnvelope);
        Assert.assertNotNull(resultsEnvelope.getSingleValidationResults());
        Assert.assertEquals(validationAuthor, resultsEnvelope.getValidationAuthor());

        for (SingleValidationResult result : resultsEnvelope.getSingleValidationResults()) {
            Assert.assertEquals(fileID, result.getEntityUuid());
        }

        return resultsEnvelope.getSingleValidationResults();
    }

    private static SingleValidationResult createResult(SingleValidationResultStatus status, String entityUuid,
                                                       ValidationAuthor validationAuthor) {
        SingleValidationResult result = new SingleValidationResult();
        result.setEntityUuid(entityUuid);
        result.setValidationStatus(status);
        result.setValidationAuthor(validationAuthor);
        return result;
    }
}
