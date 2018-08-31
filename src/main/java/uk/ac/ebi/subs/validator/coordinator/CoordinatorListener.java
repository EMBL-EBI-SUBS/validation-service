package uk.ac.ebi.subs.validator.coordinator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.fileupload.File;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.validator.coordinator.messages.FileDeletedMessage;
import uk.ac.ebi.subs.validator.coordinator.messages.StoredSubmittableDeleteMessage;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.AssayDataValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.AssayValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.FileUploadValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.ProjectValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.SampleValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.StudyValidationEnvelopeToCoordinator;

import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.FILE_DELETION_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.FILE_REF_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_ANALYSIS_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_ASSAY_DATA_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_ASSAY_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_PROJECT_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_SAMPLE_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_STUDY_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_SUBMITTABLE_DELETED;

@Component
@RequiredArgsConstructor
public class CoordinatorListener {
    private static final Logger logger = LoggerFactory.getLogger(CoordinatorListener.class);

    @NonNull
    private SubmittableHandler submittableHandler;
    @NonNull
    private FileValidationRequestHandler fileValidationRequestHandler;
    @NonNull
    private ChainedValidationService chainedValidationService;



    /**
     * Project validator data entry point.
     * @param envelope contains the {@link Project} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_PROJECT_VALIDATOR)
    public void processProjectSubmission(ProjectValidationEnvelopeToCoordinator envelope) {
        Project project = envelope.getEntityToValidate();


        if (project == null) {
            throw new IllegalArgumentException("The envelope should contain a project.");
        }

        logger.info("Received validation request on project {}", project.getId());

        if (!submittableHandler.handleSubmittable(project,envelope.getDataTypeId())) {
            logger.error("Error handling project with id {}", project.getId());
        } else {
            logger.trace("Triggering chained validation from project {}", project.getId());
            chainedValidationService.triggerChainedValidation(project, envelope.getSubmissionId());
        }
    }

    /**
     * Sample validator data entry point.
     * @param envelope contains the {@link Sample} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_SAMPLE_VALIDATOR)
    public void processSampleSubmission(SampleValidationEnvelopeToCoordinator envelope) {
        Sample sample = envelope.getEntityToValidate();

        if (sample == null) {
            throw new IllegalArgumentException("The envelope should contain a sample.");
        }

        logger.info("Received validation request on sample with id {}", sample.getId());

        if (!submittableHandler.handleSubmittable(sample, envelope.getSubmissionId(),envelope.getDataTypeId())) {
            logger.error("Error handling sample with id {}", sample.getId());
        } else {
            logger.trace("Triggering chained validation from sample {}", sample.getId());
            chainedValidationService.triggerChainedValidation(sample, envelope.getSubmissionId());
        }
    }

    /**
     * Study validator data entry point.
     * @param envelope contains the {@link Study} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_STUDY_VALIDATOR)
    public void processStudySubmission(StudyValidationEnvelopeToCoordinator envelope){
        Study study = envelope.getEntityToValidate();

        if (study == null) {
            throw new IllegalArgumentException("The envelope should contain a study.");
        }

        logger.info("Received validation request on study with id {}", study.getId());

        if (!submittableHandler.handleSubmittable(study,envelope.getSubmissionId(),envelope.getDataTypeId())) {
            logger.error("Error handling study with id {}", study.getId());
        } else {
            logger.trace("Triggering chained validation from study {}", study.getId());
            chainedValidationService.triggerChainedValidation(study, envelope.getSubmissionId());
        }
    }

    /**
     * Assay validator data entry point.
     * @param envelope contains the {@link Assay} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_ASSAY_VALIDATOR)
    public void processAssaySubmission(AssayValidationEnvelopeToCoordinator envelope) {
        Assay assay = envelope.getEntityToValidate();

        if (assay == null) {
            throw new IllegalArgumentException("The envelope should contain an assay.");
        }

        logger.info("Received validation request on assay {}", assay.getId());

        if (!submittableHandler.handleSubmittable(assay,envelope.getSubmissionId(),envelope.getDataTypeId())) {
            logger.error("Error handling assay with id {}", assay.getId());
        } else {
            logger.trace("Triggering chained validation from assay {}", assay.getId());
            chainedValidationService.triggerChainedValidation(assay, envelope.getSubmissionId());
        }
    }

    /**
     * AssayData validator data entry point.
     * @param envelope contains the {@link AssayData} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_ASSAY_DATA_VALIDATOR)
    public void processAssayDataSubmission(AssayDataValidationEnvelopeToCoordinator envelope) {
        AssayData assayData = envelope.getEntityToValidate();

        if (assayData == null) {
            throw new IllegalArgumentException("The envelope should contain an assay data.");
        }

        logger.info("Received validation request on assay data {}", assayData.getId());

        if (!submittableHandler.handleSubmittable(assayData,envelope.getSubmissionId(),envelope.getDataTypeId())) {
            logger.error("Error handling assayData with id {}", assayData.getId());
        } else {
            fileValidationRequestHandler.handleFilesWhenSubmittableChanged(envelope.getSubmissionId());

            logger.trace("Triggering chained validation from assayData {}", assayData.getId());
            chainedValidationService.triggerChainedValidation(assayData, envelope.getSubmissionId());
        }
    }

    /**
     * Analysis validator data entry point.
     * @param envelope contains the {@link Analysis} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_ANALYSIS_VALIDATOR)
    public void processAnalysisSubmission(AnalysisValidationEnvelopeToCoordinator envelope) {
        Analysis analysis = envelope.getEntityToValidate();

        if (analysis == null) {
            throw new IllegalArgumentException("The envelope should contain an analysis.");
        }

        logger.info("Received validation request on analysis {}", analysis.getId());

        if (!submittableHandler.handleSubmittable(analysis,envelope.getSubmissionId(),envelope.getDataTypeId())) {
            logger.error("Error handling analysis with id {}", analysis.getId());
        } else {
            fileValidationRequestHandler.handleFilesWhenSubmittableChanged(envelope.getSubmissionId());

            logger.trace("Triggering chained validation from analysis {}", analysis.getId());
            chainedValidationService.triggerChainedValidation(analysis, envelope.getSubmissionId());
        }
    }

    /**
     * File reference existence validator data entry point.
     * @param envelope contains the {@link File} entity to validate
     */
    @RabbitListener(queues = FILE_REF_VALIDATOR)
    public void processFileReferenceValidationRequest(FileUploadValidationEnvelopeToCoordinator envelope) {
        File fileToValidate = envelope.getFileToValidate();

        if (fileToValidate == null) {
            throw new IllegalArgumentException("The envelope should contain a file to validate.");
        }

        logger.info("Received validation request on file [id: {}]", fileToValidate.getId());

        if (!fileValidationRequestHandler.handleFile(fileToValidate, envelope.getSubmissionId())) {
            logger.error("Error handling file to validate with id {}", fileToValidate.getId());
        }
        if (!fileValidationRequestHandler.handleSubmittableForFileReferenceValidation(envelope.getSubmissionId())) {
            logger.error("Error handling submittables to validate their file references for submission (id: {})", envelope.getSubmissionId());
        }
    }

    /**
     * File deletion entry point to trigger a file reference validation to the given submission.
     * @param fileDeletedMessage contains the ID of the submission to validate
     */
    @RabbitListener(queues = FILE_DELETION_VALIDATOR)
    public void processFileDeletionRequest(FileDeletedMessage fileDeletedMessage) {
        String submissionID = fileDeletedMessage.getSubmissionId();

        if (!fileValidationRequestHandler.handleSubmittableForFileReferenceValidation(submissionID)) {
            logger.error("Error handling file deletion for submission (id: {})", submissionID);
        }
    }

    /**
     * Submittable deletion entry point for triggering a file reference and chained validation
     * based on the given submission ID..
     * @param storedSubmittableDeleteMessage contains the ID of the submission to validate
     */
    @RabbitListener(queues = SUBMISSION_SUBMITTABLE_DELETED)
    public void processSubmittableDeletion(StoredSubmittableDeleteMessage storedSubmittableDeleteMessage) {
        String submissionID = storedSubmittableDeleteMessage.getSubmissionId();

        if (!fileValidationRequestHandler.handleFilesWhenSubmittableChanged(submissionID)) {
            logger.error("Error handling submittable deleted from submission (id: {})", submissionID);
        } else {
            logger.trace("Triggering chained validation from submission (id: {})", submissionID);
            chainedValidationService.triggerChainedValidation(null, submissionID);
        }
    }
}
