package uk.ac.ebi.subs.validator.core.handlers;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.validator.core.validators.AttributeValidator;
import uk.ac.ebi.subs.validator.core.validators.ReferenceValidator;
import uk.ac.ebi.subs.validator.core.validators.ValidatorHelper;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;

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

    private ReferenceValidator refValidator;


    private AttributeValidator attributeValidator;

    public AssayHandler(ReferenceValidator refValidator,
                        AttributeValidator attributeValidator) {
        this.refValidator = refValidator;
        this.attributeValidator = attributeValidator;
    }

    @Override
    List<SingleValidationResult> validateSubmittable(AssayValidationMessageEnvelope envelope) {
        Assay assay = envelope.getEntityToValidate();

        List<SingleValidationResult> results = new ArrayList<>();

        results.add(
                refValidator.validate(
                        assay.getId(),
                        assay.getStudyRef(),
                        envelope.getStudy())
        );

        Collection<SampleRef> sampleRefs = assay.getSampleUses().stream()
                .map(SampleUse::getSampleRef)
                .collect(Collectors.toList());


        results.addAll(
                refValidator.validate(assay.getId(), sampleRefs, envelope.getSampleList())
        );

        return results;
    }

    @Override
    List<SingleValidationResult> validateAttributes(AssayValidationMessageEnvelope envelope) {
        Assay assay = envelope.getEntityToValidate();

        return ValidatorHelper.validateAttribute(assay.getAttributes(), assay.getId(), attributeValidator);
    }
}
