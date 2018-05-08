package uk.ac.ebi.subs.validator.filereference;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.fileupload.File;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;
import uk.ac.ebi.subs.repository.repos.submittables.AssayDataRepository;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileReferenceValidator {

    @NonNull
    private FileRepository fileRepository;

    @NonNull
    private AssayDataRepository assayDataRepository;

    final static String STORED_FILE_NOT_REFERENCED = "The [%s] uploaded file is not referenced in any of the run.";
    final static String FILE_METADATA_NOT_EXISTS_AS_UPLOADED_FILE = "The file [%s] referenced in the metadata is not exists on the file storage area.";
    static final String SUCCESS_FILE_VALIDATION_MESSAGE_ASSAY_DATA = "All referenced files exists on the file storage.";
    static final String SUCCESS_FILE_VALIDATION_MESSAGE_UPLOADED_FILE = "All file uploaded file(s) referenced in file metadata";

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public List<SingleValidationResult> validate(File fileToValidate) {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();
        final List<uk.ac.ebi.subs.repository.model.AssayData> assayDataList =
                assayDataRepository.findBySubmissionId(fileToValidate.getSubmissionId());

        List<String> filePathsFromMetadata = assayDataList
                .stream().map( AssayData::getFiles).collect(Collectors.toList())
                .stream().flatMap(List::stream).map( file ->
                    String.join(FILE_SEPARATOR,
                            buildBaseFilePathBySubmissionID(fileToValidate.getSubmissionId()), file.getName())
                )
                .collect(Collectors.toList());

        singleValidationResults.add(
                validateIfStoredFilesReferencedInSubmittables(fileToValidate, filePathsFromMetadata)
        );

        return singleValidationResults;
    }

    public List<SingleValidationResult> validate(AssayData entityToValidate, String submissionID) {
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();
        List<uk.ac.ebi.subs.repository.model.fileupload.File> uploadedFiles = fileRepository.findBySubmissionId(submissionID);
        List<String> filePathsFromUploadedFile =
                uploadedFiles.stream().map(uploadedFile ->
                        subtractServerDependantPath(
                                uploadedFile.getTargetPath(), uploadedFile.getSubmissionId())
                )
                .collect(Collectors.toList());

        List<String> filePathsFromEntity = entityToValidate.getFiles().stream()
                .map( file ->
                    String.join(FILE_SEPARATOR,
                            buildBaseFilePathBySubmissionID(submissionID), file.getName())
                )
                .collect(Collectors.toList());

        if (!filePathsFromEntity.isEmpty()) {
            for (String filepath : filePathsFromEntity) {
                singleValidationResults.add(validateIfReferencedFileExistsOnStorage(entityToValidate.getId(), filepath,
                        filePathsFromUploadedFile));
            }
        } else {
            singleValidationResults.add(generateDefaultSingleValidationResult(
                    entityToValidate.getId(), SUCCESS_FILE_VALIDATION_MESSAGE_ASSAY_DATA));
        }

        return singleValidationResults;
    }

    static String buildBaseFilePathBySubmissionID(String submissionID) {
        StringBuilder baseFilePath = new StringBuilder();
        baseFilePath.append(submissionID.substring(0, 1));
        baseFilePath.append(FILE_SEPARATOR);
        baseFilePath.append(submissionID.substring(1, 2));
        baseFilePath.append(FILE_SEPARATOR);
        baseFilePath.append(submissionID);

        return baseFilePath.toString();
    }

    private String subtractServerDependantPath(String fullFilePath, String submissionID) {
        int indexOfRelativePath = fullFilePath.indexOf(buildBaseFilePathBySubmissionID(submissionID));
        return fullFilePath.substring(indexOfRelativePath);
    }

    private SingleValidationResult validateIfStoredFilesReferencedInSubmittables(
            File uploadedFile, List<String> filePathsFromMetadata ) {
        SingleValidationResult singleValidationResult = generateDefaultSingleValidationResult(
                uploadedFile.getId(), SUCCESS_FILE_VALIDATION_MESSAGE_UPLOADED_FILE);

        if (!filePathsFromMetadata.contains(
                subtractServerDependantPath(
                        uploadedFile.getTargetPath(), uploadedFile.getSubmissionId()))) {
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
            singleValidationResult.setMessage(String.format(STORED_FILE_NOT_REFERENCED, uploadedFile.getFilename()));
        }

        return singleValidationResult;
    }

    private SingleValidationResult validateIfReferencedFileExistsOnStorage(String entityToValidateId, String metadataFilePath,
                                                                           List<String> storedFilesPathList) {
        SingleValidationResult singleValidationResult = generateDefaultSingleValidationResult(
                entityToValidateId, SUCCESS_FILE_VALIDATION_MESSAGE_ASSAY_DATA);

        if (!storedFilesPathList.contains(metadataFilePath)) {
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
            singleValidationResult.setMessage(String.format(FILE_METADATA_NOT_EXISTS_AS_UPLOADED_FILE,
                    metadataFilePath.substring(metadataFilePath.lastIndexOf(FILE_SEPARATOR) + 1)));
        }

        return singleValidationResult;
    }

    private SingleValidationResult generateDefaultSingleValidationResult(String entityId, String successMessage) {
        SingleValidationResult result = new SingleValidationResult(ValidationAuthor.FileReference, entityId);
        result.setValidationStatus(SingleValidationResultStatus.Pass);
        result.setMessage(successMessage);
        return result;
    }
}
