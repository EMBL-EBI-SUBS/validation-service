package uk.ac.ebi.subs.validator.coordinator;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;

import java.util.Map;
import java.util.stream.Stream;

@Service
public class ChainedValidationService {

    private Map<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> submittableRepositoryMap;
    private SubmittableHandler submittableHandler;

    public ChainedValidationService(
            Map<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> submittableRepositoryMap,
            SubmittableHandler submittableHandler
    ) {
        this.submittableRepositoryMap = submittableRepositoryMap;
        this.submittableHandler = submittableHandler;
    }

    public void triggerChainedValidation(Submittable triggerSubmittable, String submissionId) {
        streamSubmittablesInSubmissionExceptTriggerSubmittable(triggerSubmittable, submissionId)
                .forEach(storedSubmittable -> submittableHandler.handleSubmittable(
                        storedSubmittable,
                        submissionId,
                        (storedSubmittable.getDataType() == null) ? null : storedSubmittable.getDataType().getId(),
                        (storedSubmittable.getChecklist() == null) ? null : storedSubmittable.getChecklist().getId()
                        )
                );
    }

    protected Stream<? extends StoredSubmittable> streamSubmittablesInSubmissionExceptTriggerSubmittable(Submittable triggerSubmittable, String submissionId) {
        Stream<? extends StoredSubmittable> streamSubmittablesInSubmission = streamSubmittablesInSubmission(submissionId);

        if (triggerSubmittable != null) {
            streamSubmittablesInSubmission = streamSubmittablesInSubmission
                    .filter(submittable -> !submittable.getId().equals(triggerSubmittable.getId()));
        }

        return streamSubmittablesInSubmission;
    }

    protected Stream<? extends StoredSubmittable> streamSubmittablesInSubmission(String submissionId) {
        return submittableRepositoryMap.entrySet().stream()
                .map(entry -> entry.getValue())
                .flatMap(submittableRepository -> submittableRepository.streamBySubmissionId(submissionId));
    }

}
