package uk.ac.ebi.subs.validator.coordinator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.fileupload.File;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.repository.model.Analysis;
import uk.ac.ebi.subs.repository.model.AssayData;
import uk.ac.ebi.subs.repository.repos.fileupload.FileRepository;
import uk.ac.ebi.subs.repository.repos.submittables.AnalysisRepository;
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
    private AnalysisRepository analysisRepository;
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

    void handleSubmittableForFileReferenceValidation(String submissionId) {
        List<AssayData> assayDataList = assayDataRepository.findBySubmissionId(submissionId);
        assayDataList.forEach(assayData -> {

                    // TODO: karoly add later a check if that entity has been archived previously (proposed: ArchivedSubmittable)
                    // if yes, then make sure that the list of file references has not been changed

                    submittableHandler.handleSubmittable(
                            assayData,
                            submissionId,
                            (assayData.getDataType() == null) ? null : assayData.getDataType().getId(),
                            (assayData.getChecklist() == null) ? null : assayData.getChecklist().getId()
                    );
                }
        );

        List<Analysis> analysisList = analysisRepository.findBySubmissionId(submissionId);
        analysisList.forEach(analysis -> {

            // TODO: karoly add later a check if that entity has been archived previously (proposed: ArchivedSubmittable)
            // if yes, then make sure that the list of file references has not been changed

            submittableHandler.handleSubmittable(
                    analysis,
                    submissionId,
                    (analysis.getDataType() == null) ? null : analysis.getDataType().getId(),
                    (analysis.getChecklist() == null) ? null : analysis.getChecklist().getId()
            );
        });
    }

    boolean handleFilesWhenSubmittableChanged(String submissionId) {
        boolean hasPassed = true;
        List<uk.ac.ebi.subs.repository.model.fileupload.File> uploadedFiles = fileRepository.findBySubmissionId(submissionId);

        for (File uploadedFile : uploadedFiles) {
            if (!handleFile(uploadedFile, submissionId)) {
                logger.error("Error handling file to validate with id {}", uploadedFile.getId());
            }

            hasPassed = false;
        }

        return hasPassed;
    }
}
