package uk.ac.ebi.subs.validator.core.handlers;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.repos.DataTypeRepository;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class responsible for handle {@link Assay} validation.
 * <p>
 * An assay refers to a study via {@link uk.ac.ebi.subs.data.component.StudyRef StudyRef} and to
 * one or multiple samples via {@link uk.ac.ebi.subs.data.component.SampleUse SampleUse}.
 */
@Service
public class AssayHandler extends AbstractHandler<AssayValidationMessageEnvelope> {

    @NonNull
    private ReferenceValidator refValidator;

    @NonNull @Getter
    private AttributeValidator attributeValidator;

    public AssayHandler(@NonNull ReferenceValidator refValidator, @NonNull AttributeValidator attributeValidator,
                        DataTypeRepository dataTypeRepository) {
        super(dataTypeRepository);
        this.refValidator = refValidator;
        this.attributeValidator = attributeValidator;
    }

    @Override
    List<SingleValidationResult> validateSubmittable(AssayValidationMessageEnvelope envelope) {
        Assay assay = envelope.getEntityToValidate();

        final String dataTypeId = envelope.getDataTypeId();
        DataType dataType = getDataTypeFromRepository(dataTypeId);

        List<SingleValidationResult> results = new ArrayList<>(refValidator.validate(
                assay,
                dataType,
                assay.getStudyRef(),
                envelope.getStudy()));

        Collection<SampleRef> sampleRefs = assay.getSampleUses().stream()
                .map(SampleUse::getSampleRef)
                .collect(Collectors.toList());

        results.addAll(
                refValidator.validate(assay,dataType, sampleRefs, envelope.getSampleList())
        );

        return results;
    }
}
