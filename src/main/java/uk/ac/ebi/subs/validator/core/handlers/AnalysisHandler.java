package uk.ac.ebi.subs.validator.core.handlers;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.AnalysisRef;
import uk.ac.ebi.subs.data.component.AssayDataRef;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.submittable.Analysis;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.AssayData;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.data.AnalysisValidationEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AnalysisHandler extends AbstractHandler<AnalysisValidationEnvelope> {

    @NonNull
    private ReferenceValidator refValidator;
    @NonNull
    @Getter
    private AttributeValidator attributeValidator;

    public AnalysisHandler(@NonNull ReferenceValidator refValidator, @NonNull AttributeValidator attributeValidator,
                           DataTypeRepository dataTypeRepository) {
        super(dataTypeRepository);
        this.refValidator = refValidator;
        this.attributeValidator = attributeValidator;
    }

    @Override
    List<SingleValidationResult> validateSubmittable(AnalysisValidationEnvelope envelope) {
        DataType dataType = getDataTypeFromRepository(envelope.getDataTypeId());

        return Stream.of(
                studyRefValidation(envelope,dataType),
                sampleRefValidation(envelope,dataType),
                assayRefValidation(envelope,dataType),
                assayDataRefValidation(envelope,dataType),
                analysisRefValidation(envelope,dataType)

        )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    List<SingleValidationResult> studyRefValidation(AnalysisValidationEnvelope envelope,DataType dataType){
        List<StudyRef> refs = envelope.getEntityToValidate().getStudyRefs();
        List<Submittable<Study>> studies = envelope.getStudies();

        return refValidator.validate(
                envelope.getEntityToValidate(),
                dataType,
                refs,
                studies
        );
    }

    List<SingleValidationResult> sampleRefValidation(AnalysisValidationEnvelope envelope,DataType dataType){
        List<StudyRef> refs = envelope.getEntityToValidate().getStudyRefs();
        List<Submittable<Study>> studies = envelope.getStudies();

        return refValidator.validate(
                envelope.getEntityToValidate(),
                dataType,
                refs,
                studies
        );
    }

    List<SingleValidationResult> assayRefValidation(AnalysisValidationEnvelope envelope,DataType dataType){
        List<AssayRef> refs = envelope.getEntityToValidate().getAssayRefs();
        List<Submittable<Assay>> assays = envelope.getAssays();

        return refValidator.validate(
                envelope.getEntityToValidate(),
                dataType,
                refs,
                assays
        );
    }

    List<SingleValidationResult> assayDataRefValidation(AnalysisValidationEnvelope envelope,DataType dataType){
        List<AssayDataRef> refs = envelope.getEntityToValidate().getAssayDataRefs();
        List<Submittable<AssayData>> assayData = envelope.getAssayData();

        return refValidator.validate(
                envelope.getEntityToValidate(),
                dataType,
                refs,
                assayData
        );
    }

    List<SingleValidationResult> analysisRefValidation(AnalysisValidationEnvelope envelope,DataType dataType){
        List<AnalysisRef> refs = envelope.getEntityToValidate().getAnalysisRefs();
        List<Submittable<Analysis>> analyses = envelope.getAnalyses();

        return refValidator.validate(
                envelope.getEntityToValidate(),
                dataType,
                refs,
                analyses
        );
    }
}
