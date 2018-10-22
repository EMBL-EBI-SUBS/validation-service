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
import uk.ac.ebi.subs.data.submittable.BaseSubmittable;
import uk.ac.ebi.subs.data.submittable.EgaDac;
import uk.ac.ebi.subs.data.submittable.EgaDacPolicy;
import uk.ac.ebi.subs.data.submittable.EgaDataset;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.data.submittable.Protocol;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.SampleGroup;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.validator.coordinator.messages.FileDeletedMessage;
import uk.ac.ebi.subs.validator.coordinator.messages.StoredSubmittableDeleteMessage;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.AssayDataValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.AssayValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.EgaDacPolicyValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.EgaDacValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.EgaDatasetValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.FileUploadValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.ProjectValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.ProtocolValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.SampleGroupValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.SampleValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.StudyValidationEnvelopeToCoordinator;
import uk.ac.ebi.subs.validator.data.ValidationEnvelopeToCoordinator;

import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.FILE_DELETION_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.FILE_REF_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_ANALYSIS_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_ASSAY_DATA_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_ASSAY_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_EGA_DAC_POLICY_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_EGA_DAC_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_EGA_DATASET_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_PROJECT_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_PROTOCOL_VALIDATOR;
import static uk.ac.ebi.subs.validator.messaging.CoordinatorQueues.SUBMISSION_SAMPLE_GROUP_VALIDATOR;
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
     *
     * @param envelope contains the {@link Project} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_PROJECT_VALIDATOR)
    public void processProjectSubmission(ProjectValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, Project.class);
    }

    /**
     * Sample validator data entry point.
     *
     * @param envelope contains the {@link Sample} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_SAMPLE_VALIDATOR)
    public void processSampleSubmission(SampleValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, Sample.class);
    }

    /**
     * Study validator data entry point.
     *
     * @param envelope contains the {@link Study} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_STUDY_VALIDATOR)
    public void processStudySubmission(StudyValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, Study.class);
    }

    /**
     * Assay validator data entry point.
     *
     * @param envelope contains the {@link Assay} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_ASSAY_VALIDATOR)
    public void processAssaySubmission(AssayValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, Assay.class);
    }

    @RabbitListener(queues = SUBMISSION_SAMPLE_GROUP_VALIDATOR)
    public void processSampleGroupSubmission(SampleGroupValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, SampleGroup.class);
    }

    @RabbitListener(queues = SUBMISSION_PROTOCOL_VALIDATOR)
    public void processProtocolSubmission(ProtocolValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, Protocol.class);
    }

    @RabbitListener(queues = SUBMISSION_EGA_DAC_VALIDATOR)
    public void processEgaDacSubmission(EgaDacValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, EgaDac.class);
    }

    @RabbitListener(queues = SUBMISSION_EGA_DAC_POLICY_VALIDATOR)
    public void processEgaDacPolicySubmission(EgaDacPolicyValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, EgaDacPolicy.class);
    }

    @RabbitListener(queues = SUBMISSION_EGA_DATASET_VALIDATOR)
    public void processEgaDatasetSubmission(EgaDatasetValidationEnvelopeToCoordinator envelope) {
        basicSubmittableValidationProcessing(envelope, EgaDataset.class);
    }


    private void basicSubmittableValidationProcessing(ValidationEnvelopeToCoordinator envelope, Class<? extends BaseSubmittable> clazz) {
        BaseSubmittable submittable = envelope.getEntityToValidate();

        if (submittable == null) {
            throw new IllegalArgumentException("The envelope should contain a " + clazz.getSimpleName().toLowerCase()+".");
        }

        logger.info("Received validation request on {} {}", clazz.getSimpleName(), submittable.getId());

        if (!submittableHandler.handleSubmittable(submittable, envelope.getSubmissionId(),
                envelope.getDataTypeId(), envelope.getChecklistId())) {
            logger.error("Error handling {} with id {}", clazz.getSimpleName().toLowerCase(), submittable.getId());
        } else {
            logger.trace("Triggering chained validation from {} {}", clazz.getSimpleName().toLowerCase(), submittable.getId());
            chainedValidationService.triggerChainedValidation(submittable, envelope.getSubmissionId());
        }
    }

    /**
     * AssayData validator data entry point.
     *
     * @param envelope contains the {@link AssayData} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_ASSAY_DATA_VALIDATOR)
    public void processAssayDataSubmission(AssayDataValidationEnvelopeToCoordinator envelope) {
        AssayData assayData = envelope.getEntityToValidate();

        if (assayData == null) {
            throw new IllegalArgumentException("The envelope should contain an assay data.");
        }

        logger.info("Received validation request on assay data {}", assayData.getId());

        if (!submittableHandler.handleSubmittable(assayData, envelope.getSubmissionId(),
                envelope.getDataTypeId(), envelope.getChecklistId())) {
            logger.error("Error handling assayData with id {}", assayData.getId());
        } else {
            fileValidationRequestHandler.handleFilesWhenSubmittableChanged(envelope.getSubmissionId());

            logger.trace("Triggering chained validation from assayData {}", assayData.getId());
            chainedValidationService.triggerChainedValidation(assayData, envelope.getSubmissionId());
        }
    }

    /**
     * Analysis validator data entry point.
     *
     * @param envelope contains the {@link Analysis} entity to validate
     */
    @RabbitListener(queues = SUBMISSION_ANALYSIS_VALIDATOR)
    public void processAnalysisSubmission(AnalysisValidationEnvelopeToCoordinator envelope) {
        Analysis analysis = envelope.getEntityToValidate();

        if (analysis == null) {
            throw new IllegalArgumentException("The envelope should contain an analysis.");
        }

        logger.info("Received validation request on analysis {}", analysis.getId());

        if (!submittableHandler.handleSubmittable(analysis, envelope.getSubmissionId(),
                envelope.getDataTypeId(), envelope.getChecklistId())) {
            logger.error("Error handling analysis with id {}", analysis.getId());
        } else {
            fileValidationRequestHandler.handleFilesWhenSubmittableChanged(envelope.getSubmissionId());

            logger.trace("Triggering chained validation from analysis {}", analysis.getId());
            chainedValidationService.triggerChainedValidation(analysis, envelope.getSubmissionId());
        }
    }


    /**
     * File reference existence validator data entry point.
     *
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
        fileValidationRequestHandler.handleSubmittableForFileReferenceValidation(envelope.getSubmissionId());
        logger.trace("Handled submittables for file reference validation - a new file has been added.");
    }

    /**
     * File deletion entry point to trigger a file reference validation to the given submission.
     *
     * @param fileDeletedMessage contains the ID of the submission to validate
     */
    @RabbitListener(queues = FILE_DELETION_VALIDATOR)
    public void processFileDeletionRequest(FileDeletedMessage fileDeletedMessage) {
        String submissionID = fileDeletedMessage.getSubmissionId();

        fileValidationRequestHandler.handleSubmittableForFileReferenceValidation(submissionID);
        logger.trace("Handled submittables for file reference validation - a file has been deleted.");
    }

    /**
     * Submittable deletion entry point for triggering a file reference and chained validation
     * based on the given submission ID..
     *
     * @param storedSubmittableDeleteMessage contains the ID of the submission to validate
     */
    @RabbitListener(queues = SUBMISSION_SUBMITTABLE_DELETED)
    public void processSubmittableDeletion(StoredSubmittableDeleteMessage storedSubmittableDeleteMessage) {
        String submissionID = storedSubmittableDeleteMessage.getSubmissionId();

        fileValidationRequestHandler.handleFilesWhenSubmittableChanged(submissionID);
        chainedValidationService.triggerChainedValidation(submissionID);
    }
}
