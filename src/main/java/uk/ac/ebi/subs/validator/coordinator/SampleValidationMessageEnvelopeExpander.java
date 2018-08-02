package uk.ac.ebi.subs.validator.coordinator;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.component.StudyDataType;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.List;

@Service
public class SampleValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<SampleValidationMessageEnvelope> {
    SampleRepository sampleRepository;
    StudyRepository studyRepository;

    public SampleValidationMessageEnvelopeExpander(SampleRepository sampleRepository,
                                                   StudyRepository studyRepository) {
        this.sampleRepository = sampleRepository;
        this.studyRepository = studyRepository;
    }

    @Override
    void expandEnvelope(SampleValidationMessageEnvelope validationMessageEnvelope) {
        final List<SampleRelationship> sampleRelationships = validationMessageEnvelope.getEntityToValidate().getSampleRelationships();

        for (SampleRelationship sampleRelationship : sampleRelationships) {

            Sample sample;

            if (sampleRelationship.getAccession() != null && !sampleRelationship.getAccession().isEmpty()) {
                sample = sampleRepository.findByAccession(sampleRelationship.getAccession());
            } else {
                sample = sampleRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(sampleRelationship.getTeam(), sampleRelationship.getAlias());
            }

            if (sample != null) {
                Submittable<uk.ac.ebi.subs.data.submittable.Sample> sampleSubmittable = new Submittable<>(sample, sample.getSubmission().getId());
                validationMessageEnvelope.getSampleList().add(sampleSubmittable);
            }

        }

        StudyDataType studyDataType;
        List<Study> studies = studyRepository.findBySubmissionId(validationMessageEnvelope.getSubmissionId());
        if(studies !=null && !studies.isEmpty()){
            studyDataType = studies.get(0).getStudyType();
            //todo set studydatatype in sample validation envelope
        }

    }
}