package uk.ac.ebi.subs.validator.coordinator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.fileupload.File;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.repository.model.AssayData;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;
import uk.ac.ebi.subs.repository.repos.submittables.AssayDataRepository;
import uk.ac.ebi.subs.validator.data.FileUploadValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationResult;

import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.subs.validator.messaging.CoordinatorRoutingKeys.EVENT_FILE_REF_VALIDATION;

@Component
@RequiredArgsConstructor
public class FileValidationRequestHandler {

    @NonNull
    private RabbitMessagingTemplate rabbitMessagingTemplate;
    @NonNull
    private CoordinatorValidationResultService coordinatorValidationResultService;
    @NonNull
    private SubmittableHandler submittableHandler;
    @NonNull
    private AssayDataRepository assayDataRepository;
    @NonNull
    private FileRepository fileRepository;

    private static final Logger logger = LoggerFactory.getLogger(FileValidationRequestHandler.class);

    /**
     * @param file
     * @param submissionId
     * @return true if it could create a {@link FileUploadValidationMessageEnvelope} with the {@link File} entity and
     * the UUID of the {@link ValidationResult}
     */
    boolean handleFile(File file, String submissionId) {
        Optional<ValidationResult> optionalValidationResult = coordinatorValidationResultService.fetchValidationResultDocument(file);
        if (optionalValidationResult.isPresent()) {
            ValidationResult validationResult = optionalValidationResult.get();
            logger.debug("Validation result document has been persisted into MongoDB with ID: {}", validationResult.getUuid());

            FileUploadValidationMessageEnvelope fileUploadValidationMessageEnvelope =
                    new FileUploadValidationMessageEnvelope(validationResult.getUuid(), validationResult.getVersion(),
                            file, submissionId);

            logger.debug("Sending file to validation queues");
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_FILE_REF_VALIDATION, fileUploadValidationMessageEnvelope);

            return validationResult.getEntityUuid() != null;
        }
        return false;
    }

    boolean handleSubmittableForFileReferenceValidation(String submissionId) {
        List<AssayData> assayDataList = assayDataRepository.findBySubmissionId(submissionId);
        assayDataList.forEach( assayData -> {

            // TODO: karoly add later a check if that entity has been archived previously (proposed: ArchivedSubmittable)
            // if yes, then make sure that the list of file references has not been changed

            submittableHandler.handleSubmittable(assayData, submissionId, true);
        });

        return false;
    }

    void handleFilesWhenSubmittableChanged(String submissionId) {
        List<uk.ac.ebi.subs.repository.model.fileupload.File> uploadedFiles = fileRepository.findBySubmissionId(submissionId);

        uploadedFiles.forEach( uploadedFile -> {
            if (!handleFile(uploadedFile, submissionId)) {
                logger.error("Error handling file to validate with id {}", uploadedFile.getId());
            }
        });
    }
}
