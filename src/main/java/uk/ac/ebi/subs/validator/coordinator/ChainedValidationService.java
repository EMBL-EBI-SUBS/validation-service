package uk.ac.ebi.subs.validator.coordinator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.AbstractSubsRef;
import uk.ac.ebi.subs.data.component.AnalysisRef;
import uk.ac.ebi.subs.data.component.AssayDataRef;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.EgaDacPolicyRef;
import uk.ac.ebi.subs.data.component.EgaDacRef;
import uk.ac.ebi.subs.data.component.EgaDatasetRef;
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.data.component.ProtocolRef;
import uk.ac.ebi.subs.data.component.SampleGroupRef;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
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
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepositoryCustom;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChainedValidationService {

    @NonNull
    private List<SubmittableRepository<?>> submissionContentsRepositories;
    @NonNull
    private SubmittableHandler submittableHandler;

    public void triggerChainedValidation(String submissionId){
        submissionContentsRepositories.stream()
                .flatMap(repo -> repo.streamBySubmissionId(submissionId))
                .forEach(storedSubmittable -> revalidate(storedSubmittable,submissionId));
    }


    private void revalidate(StoredSubmittable storedSubmittable, String submissionId){
        submittableHandler.handleSubmittable(
                storedSubmittable,
                submissionId,
                (storedSubmittable.getDataType() == null) ? null : storedSubmittable.getDataType().getId(),
                (storedSubmittable.getChecklist() == null) ? null : storedSubmittable.getChecklist().getId()
        );
    }

    public void triggerChainedValidation(Submittable triggerSubmittable, String submissionId) {
        AbstractSubsRef ref = submittableToRef(triggerSubmittable);

        submissionContentsRepositories.stream()
                .flatMap(repo -> {
                    List<StoredSubmittable> itemsReferencingTriggerSubmittable = ((SubmittableRepositoryCustom) repo)
                            .findBySubmissionIdAndReference(submissionId, ref);
                    return itemsReferencingTriggerSubmittable.stream();
                })
                .forEach(storedSubmittable -> revalidate(storedSubmittable,submissionId));
    }

    protected AbstractSubsRef submittableToRef(Submittable submittable) {
        AbstractSubsRef ref = new SampleGroupRef();

        if (submittable instanceof Analysis) {
            ref = new AnalysisRef();
        }
        if (submittable instanceof Assay) {
            ref = new AssayRef();
        }
        if (submittable instanceof AssayData) {
            ref = new AssayDataRef();
        }
        if (submittable instanceof EgaDac) {
            ref = new EgaDacRef();
        }
        if (submittable instanceof EgaDacPolicy) {
            ref = new EgaDacPolicyRef();
        }
        if (submittable instanceof EgaDataset) {
            ref = new EgaDatasetRef();
        }
        if (submittable instanceof Project) {
            ref = new ProjectRef();
        }
        if (submittable instanceof Protocol) {
            ref = new ProtocolRef();
        }
        if (submittable instanceof Sample) {
            ref = new SampleRef();
        }
        if (submittable instanceof SampleGroup) {
            ref = new SampleGroupRef();
        }
        if (submittable instanceof Study) {
            ref = new StudyRef();
        }

        ref.setAccession(submittable.getAccession());
        ref.setAlias(submittable.getAlias());

        if (submittable.getTeam() != null) {
            ref.setTeam(submittable.getTeam().getName());
        }
        return ref;

    }


}
