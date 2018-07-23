package uk.ac.ebi.subs.validator.core.handlers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.AnalysisRef;
import uk.ac.ebi.subs.data.component.AssayDataRef;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AnalysisHandler extends AbstractHandler<AnalysisValidationEnvelope> {

    @NonNull
    private ReferenceValidator refValidator;
    @NonNull
    private AttributeValidator attributeValidator;

    @Override
    List<SingleValidationResult> validateSubmittable(AnalysisValidationEnvelope envelope) {
        Analysis analysis = envelope.getEntityToValidate();

        List<SingleValidationResult> results = Stream.of(
                studyRefValidation(envelope),
                sampleRefValidation(envelope),
                assayRefValidation(envelope),
                assayDataRefValidation(envelope),
                analysisRefValidation(envelope)

        )
                .flatMap(l -> l.stream())
                .collect(Collectors.toList());

        return results;
    }

    @Override
    List<SingleValidationResult> validateAttributes(AnalysisValidationEnvelope envelope) {
        Analysis analysis = envelope.getEntityToValidate();
        return ValidatorHelper.validateAttribute(analysis.getAttributes(), analysis.getId(), attributeValidator);
    }

    List<SingleValidationResult> studyRefValidation(AnalysisValidationEnvelope envelope){
        List<StudyRef> refs = envelope.getEntityToValidate().getStudyRefs();
        List<Submittable<Study>> studies = envelope.getStudies();

        return refValidator.validate(
                envelope.getEntityToValidate().getId(),
                refs,
                studies
        );
    }

    List<SingleValidationResult> sampleRefValidation(AnalysisValidationEnvelope envelope){
        List<StudyRef> refs = envelope.getEntityToValidate().getStudyRefs();
        List<Submittable<Study>> studies = envelope.getStudies();

        return refValidator.validate(
                envelope.getEntityToValidate().getId(),
                refs,
                studies
        );
    }

    List<SingleValidationResult> assayRefValidation(AnalysisValidationEnvelope envelope){
        List<AssayRef> refs = envelope.getEntityToValidate().getAssayRefs();
        List<Submittable<Assay>> assays = envelope.getAssays();

        return refValidator.validate(
                envelope.getEntityToValidate().getId(),
                refs,
                assays
        );
    }

    List<SingleValidationResult> assayDataRefValidation(AnalysisValidationEnvelope envelope){
        List<AssayDataRef> refs = envelope.getEntityToValidate().getAssayDataRefs();
        List<Submittable<AssayData>> assayData = envelope.getAssayData();

        return refValidator.validate(
                envelope.getEntityToValidate().getId(),
                refs,
                assayData
        );
    }

    List<SingleValidationResult> analysisRefValidation(AnalysisValidationEnvelope envelope){
        List<AnalysisRef> refs = envelope.getEntityToValidate().getAnalysisRefs();
        List<Submittable<Analysis>> analyses = envelope.getAnalyses();

        return refValidator.validate(
                envelope.getEntityToValidate().getId(),
                refs,
                analyses
        );
    }

}
