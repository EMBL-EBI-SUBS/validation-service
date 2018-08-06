package uk.ac.ebi.subs.validator.filereference;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.fileupload.File;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.util.ValidationHelper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ebi.subs.validator.util.ValidationHelper.generateSingleValidationResultsEnvelope;

@Service
@RequiredArgsConstructor
public class FileReferenceHandler {

    @NonNull
    private FileReferenceValidator fileReferenceValidator;

    public SingleValidationResultsEnvelope handleValidationRequestForUploadedFile(FileReferenceValidationDTO validationDTO) {

        File fileToValidate = (File)validationDTO.getEntityToValidate();
        List<SingleValidationResult> validationResult = fileReferenceValidator.validate(fileToValidate);

        return processValidationResult(validationResult, validationDTO, fileToValidate.getId());
    }

    public SingleValidationResultsEnvelope handleValidationRequestForAssayData(FileReferenceValidationDTO validationDTO) {

        AssayData entityToValidate = (AssayData)validationDTO.getEntityToValidate();
        String submissionID = validationDTO.getSubmissionId();
        List<SingleValidationResult> validationResult = fileReferenceValidator.validate(entityToValidate, submissionID, entityToValidate.getId());

        return processValidationResult(validationResult, validationDTO, entityToValidate.getId());
    }

    public SingleValidationResultsEnvelope handleValidationRequestForAnalysis(FileReferenceValidationDTO validationDTO) {

        Analysis entityToValidate = (Analysis) validationDTO.getEntityToValidate();
        String submissionID = validationDTO.getSubmissionId();
        List<SingleValidationResult> validationResult = fileReferenceValidator.validate(entityToValidate, submissionID, entityToValidate.getId());

        return processValidationResult(validationResult, validationDTO, entityToValidate.getId());
    }

    private SingleValidationResultsEnvelope processValidationResult(List<SingleValidationResult> validationResult,
                                                                    FileReferenceValidationDTO validationDTO,
                                                                    String objectToValidateID) {
        List<SingleValidationResult> interestingResults = validationResult.stream()
                .filter(ValidationHelper::statusIsNotPassOrPending)
                .collect(Collectors.toList());

        if (interestingResults.isEmpty()) {
            SingleValidationResult r = ValidatorHelper.getDefaultSingleValidationResult(
                    objectToValidateID, ValidationAuthor.FileReference);
            interestingResults = Collections.singletonList(r);
        }

        return generateSingleValidationResultsEnvelope(validationDTO.getValidationResultVersion(),
                validationDTO.getValidationResultUUID(), interestingResults, ValidationAuthor.FileReference);
    }
}
