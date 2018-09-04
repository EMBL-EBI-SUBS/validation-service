package uk.ac.ebi.subs.validator.coordinator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.BaseSubmittable;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationResult;

@Component
@RequiredArgsConstructor
public class ValidationEnvelopeFactory {

    @NonNull
    private SampleValidationMessageEnvelopeExpander sampleValidationMessageEnvelopeExpander;
    @NonNull
    private StudyValidationMessageEnvelopeExpander studyValidationMessageEnvelopeExpander;
    @NonNull
    private AssayValidationMessageEnvelopeExpander assayValidationMessageEnvelopeExpander;
    @NonNull
    private AssayDataValidationMessageEnvelopeExpander assayDataValidationMessageEnvelopeExpander;
    @NonNull
    private AnalysisValidationMessageEnvelopeExpander analysisValidationMessageEnvelopeExpander;

    public ValidationMessageEnvelope<?> buildValidationMessageEnvelope(Submittable submittable, ValidationResult validationResult){

        ValidationMessageEnvelope<?> envelope = null;


        if (submittable instanceof Sample) {
            SampleValidationMessageEnvelope sampleValidationMessageEnvelope = new SampleValidationMessageEnvelope(
                    validationResult.getUuid(),
                    validationResult.getVersion(),
                    (Sample)submittable,
                    validationResult.getSubmissionId()
            );

            sampleValidationMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);

            envelope = sampleValidationMessageEnvelope;
        }

        if (submittable instanceof Study) {
            StudyValidationMessageEnvelope studyValidationMessageEnvelope = new StudyValidationMessageEnvelope(
                    validationResult.getUuid(),
                    validationResult.getVersion(),
                    (Study)submittable,
                    validationResult.getSubmissionId()
            );

            studyValidationMessageEnvelopeExpander.expandEnvelope(studyValidationMessageEnvelope);

            envelope = studyValidationMessageEnvelope;
        }

        if (submittable instanceof Assay) {
            AssayValidationMessageEnvelope assayValidationMessageEnvelope = new AssayValidationMessageEnvelope(
                    validationResult.getUuid(),
                    validationResult.getVersion(),
                    (Assay)submittable,
                    validationResult.getSubmissionId()
            );

            assayValidationMessageEnvelopeExpander.expandEnvelope(assayValidationMessageEnvelope);

            envelope = assayValidationMessageEnvelope;
        }

        if (submittable instanceof AssayData) {
            AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope = new AssayDataValidationMessageEnvelope(
                    validationResult.getUuid(),
                    validationResult.getVersion(),
                    (AssayData) submittable,
                    validationResult.getSubmissionId()
            );

            assayDataValidationMessageEnvelopeExpander.expandEnvelope(assayDataValidationMessageEnvelope);

            envelope = assayDataValidationMessageEnvelope;
        }

        if (submittable instanceof Analysis) {
            AnalysisValidationEnvelope analysisValidationEnvelopeValidationMessageEnvelope = new AnalysisValidationEnvelope(
                    validationResult.getUuid(),
                    validationResult.getVersion(),
                    (Analysis)submittable,
                    validationResult.getSubmissionId()
            );

            analysisValidationMessageEnvelopeExpander.expandEnvelope(analysisValidationEnvelopeValidationMessageEnvelope);

            envelope = analysisValidationEnvelopeValidationMessageEnvelope;
        }

        if (envelope == null){
            envelope = new ValidationMessageEnvelope(validationResult.getUuid(), validationResult.getVersion(), (BaseSubmittable) submittable);
        }

        return envelope;
    }
}
