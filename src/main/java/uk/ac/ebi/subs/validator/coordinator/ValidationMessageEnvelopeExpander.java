package uk.ac.ebi.subs.validator.coordinator;

import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;


public abstract class ValidationMessageEnvelopeExpander<T extends ValidationMessageEnvelope> {
    abstract void expandEnvelope(T validationMessageEnvelope);

    boolean canAddSubmittable(T validationMessageEnvelope, StoredSubmittable storedSubmittable) {
        if (storedSubmittable != null) {
            if (validationMessageEnvelope.getSubmissionId().equals(storedSubmittable.getSubmission().getId()) ||
                    storedSubmittable.getAccession() != null && !storedSubmittable.getAccession().isEmpty()) {
                return true;
            }
        }

        return false;
    }

}
