package uk.ac.ebi.subs.validator.coordinator;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.submittable.Assay;

import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.List;

@Service
public class AssayValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<AssayValidationMessageEnvelope> {

    SampleRepository sampleRepository;
    StudyRepository studyRepository;

    public AssayValidationMessageEnvelopeExpander(SampleRepository sampleRepository, StudyRepository studyRepository) {
        this.sampleRepository = sampleRepository;
        this.studyRepository = studyRepository;
    }

    @Override
    public void expandEnvelope(AssayValidationMessageEnvelope validationMessageEnvelope) {
        final Assay entityToValidate = validationMessageEnvelope.getEntityToValidate();

        final List<SampleUse> sampleUses = entityToValidate.getSampleUses();

        for (SampleUse sampleUse : sampleUses) {

            Sample sample;

            if (sampleUse.getSampleRef().getAccession() != null && !sampleUse.getSampleRef().getAccession().isEmpty()) {
                sample = sampleRepository.findByAccession(sampleUse.getSampleRef().getAccession());
            } else {
                sample = sampleRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(sampleUse.getSampleRef().getTeam(), sampleUse.getSampleRef().getAlias());
            }

            if (canAddSubmittable(validationMessageEnvelope,sample)) {
                Submittable<uk.ac.ebi.subs.data.submittable.Sample> sampleSubmittable = new Submittable<>(sample, sample.getSubmission().getId());
                validationMessageEnvelope.getSampleList().add(sampleSubmittable);
            }

        }

        final StudyRef studyRef = entityToValidate.getStudyRef();

        Study study;

        if (studyRef.getAccession() != null && !studyRef.getAccession().isEmpty()) {
            study = studyRepository.findFirstByAccessionOrderByCreatedDateDesc(studyRef.getAccession());
        } else {
            study = studyRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(studyRef.getTeam(), studyRef.getAlias());
        }

        if (canAddSubmittable(validationMessageEnvelope, study)) {
            Submittable<uk.ac.ebi.subs.data.submittable.Study> studySubmittable = new Submittable<>(study, study.getSubmission().getId());
            validationMessageEnvelope.setStudy(studySubmittable);
        }
    }
}
