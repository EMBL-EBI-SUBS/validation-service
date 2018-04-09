package uk.ac.ebi.subs.validator.coordinator;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.repository.repos.submittables.AssayRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssayDataValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<AssayDataValidationMessageEnvelope> {

    AssayRepository assayRepository;
    SampleRepository sampleRepository;

    public AssayDataValidationMessageEnvelopeExpander(AssayRepository assayRepository, SampleRepository sampleRepository) {
        this.assayRepository = assayRepository;
        this.sampleRepository = sampleRepository;
    }

    @Override
    public void expandEnvelope(AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope) {
        final List<AssayRef> assayRefs = assayDataValidationMessageEnvelope.getEntityToValidate().getAssayRefs();
        final List<Submittable<uk.ac.ebi.subs.data.submittable.Assay>> assays = new ArrayList<>();

        for (AssayRef assayRef : assayRefs) {
            uk.ac.ebi.subs.repository.model.Assay assayStoredSubmittable;

            if (assayRef.getAccession() != null && !assayRef.getAccession().isEmpty()) {
                assayStoredSubmittable = assayRepository.findFirstByAccessionOrderByCreatedDateDesc(assayRef.getAccession());
            } else {
                assayStoredSubmittable = assayRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(assayRef.getTeam(), assayRef.getAlias());
            }

            if (assayStoredSubmittable != null) {
                Submittable<uk.ac.ebi.subs.data.submittable.Assay> assaySubmittable = new Submittable<>(assayStoredSubmittable, assayStoredSubmittable.getSubmission().getId());
                assays.add(assaySubmittable);
            }
        }

        assayDataValidationMessageEnvelope.setAssays(assays);
    }


}
