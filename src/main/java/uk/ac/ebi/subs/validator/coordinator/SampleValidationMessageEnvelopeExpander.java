package uk.ac.ebi.subs.validator.coordinator;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class SampleValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<SampleValidationMessageEnvelope> {

    @NonNull
    private SubmittableFinderService submittableFinderService;

    @Override
    void expandEnvelope(SampleValidationMessageEnvelope validationMessageEnvelope) {
        final List<SampleRelationship> sampleRelationships = validationMessageEnvelope.getEntityToValidate().getSampleRelationships();

        for (SampleRelationship sampleRelationship : sampleRelationships) {

            Sample sample;

            if (sampleRelationship.getAccession() != null && !sampleRelationship.getAccession().isEmpty()) {
                sample = submittableFinderService.findSampleByAccession(sampleRelationship.getAccession());
            } else {
                sample = submittableFinderService.findSampleByTeamNameAndAlias(sampleRelationship);
            }

            if (canAddSubmittable(validationMessageEnvelope, sample)) {
                Submittable<uk.ac.ebi.subs.data.submittable.Sample> sampleSubmittable = new Submittable<>(sample, sample.getSubmission().getId());
                validationMessageEnvelope.getSampleList().add(sampleSubmittable);
            }
        }
    }
}