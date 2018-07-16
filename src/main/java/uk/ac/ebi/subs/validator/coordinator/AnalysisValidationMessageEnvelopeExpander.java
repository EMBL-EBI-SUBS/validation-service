package uk.ac.ebi.subs.validator.coordinator;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class AnalysisValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<AnalysisValidationEnvelope> {

    @NonNull
    private SampleRepository sampleRepository;

    @NonNull
    private StudyRepository studyRepository;

    @Override
    void expandEnvelope(AnalysisValidationEnvelope validationMessageEnvelope) {
        Collection<SampleRef> sampleRefs = validationMessageEnvelope.getEntityToValidate().getSampleRefs();
        List<Submittable<Sample>> wrappedSamples = wrappedSamples(sampleRefs);
        validationMessageEnvelope.setSamples(wrappedSamples);

        Collection<StudyRef> studyRefs = validationMessageEnvelope.getEntityToValidate().getStudyRefs();
        List<Submittable<Study>> wrappedStudies = wrappedStudies(studyRefs);
        validationMessageEnvelope.setStudies(wrappedStudies);
    }

    private List<Submittable<Sample>> wrappedSamples(Collection<SampleRef> sampleRefs) {
        List<Submittable<Sample>> samples = new ArrayList<>();

        for (SampleRef sampleRef : sampleRefs) {
            uk.ac.ebi.subs.repository.model.Sample sampleStoredSubmittable;

            if (sampleRef.getAccession() != null && !sampleRef.getAccession().isEmpty()) {
                sampleStoredSubmittable = sampleRepository.findFirstByAccessionOrderByCreatedDateDesc(sampleRef.getAccession());
            } else {
                sampleStoredSubmittable = sampleRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(sampleRef.getTeam(), sampleRef.getAlias());
            }

            if (sampleStoredSubmittable != null) {
                Submittable<uk.ac.ebi.subs.data.submittable.Sample> sampleSubmittable = new Submittable<>(sampleStoredSubmittable, sampleStoredSubmittable.getSubmission().getId());
                samples.add(sampleSubmittable);
            }
        }

        return samples;
    }

    private List<Submittable<Study>> wrappedStudies(Collection<StudyRef> studyRefs) {
        List<Submittable<Study>> studies = new ArrayList<>();

        for (StudyRef studyRef : studyRefs) {
            uk.ac.ebi.subs.repository.model.Study studyStoredSubmittable;

            if (studyRef.getAccession() != null && !studyRef.getAccession().isEmpty()) {
                studyStoredSubmittable = studyRepository.findFirstByAccessionOrderByCreatedDateDesc(studyRef.getAccession());
            } else {
                studyStoredSubmittable = studyRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(studyRef.getTeam(), studyRef.getAlias());
            }

            if (studyStoredSubmittable != null) {
                Submittable<uk.ac.ebi.subs.data.submittable.Study> sampleSubmittable = new Submittable<>(studyStoredSubmittable, studyStoredSubmittable.getSubmission().getId());
                studies.add(sampleSubmittable);
            }
        }

        return studies;
    }


}
