package uk.ac.ebi.subs.validator.core.validators;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.AbstractSubsRef;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.ac.ebi.subs.validator.core.validators.ValidatorHelper.getDefaultSingleValidationResult;

@Service
@RequiredArgsConstructor
public class ReferenceValidator {
    String FAIL_MESSAGE = "Could not find reference target: %s ";
    String FAIL_TEAM_AND_ALIAS_MESSAGE = "Could not find reference for ALIAS: %s in TEAM: %s ";

    @NonNull
    private ReferenceRequirementsValidator referenceRequirementsValidator;


    public List<SingleValidationResult> validate(
            uk.ac.ebi.subs.data.submittable.Submittable entityUnderValidation,
            DataType dataTypeOfEntityUnderValidation,
            Collection<? extends AbstractSubsRef> referencesToSubmittables,
            Collection<? extends Submittable> referencedSubmittables) {

        List<SingleValidationResult> results = new ArrayList<>();

        final Map<String, Submittable> sampleAccessionMap = referencedSubmittables.stream()
                .collect(
                        Collectors.toMap(Submittable::getAccession, sample -> sample)
                );
        final Map<String, Submittable> sampleAliasMap = referencedSubmittables.stream()
                .collect(
                        Collectors.toMap(sample -> sample.getAlias() + sample.getTeam().getName(), sample -> sample)
                );


        for (AbstractSubsRef subsRef : referencesToSubmittables) {

            Submittable submittable;

            if (subsRef.getAccession() != null && !subsRef.getAccession().isEmpty()) {
                submittable = sampleAccessionMap.get(subsRef.getAccession());
            } else {
                submittable = sampleAliasMap.get(subsRef.getAlias() + subsRef.getTeam());
            }

            results.addAll(validate(entityUnderValidation, dataTypeOfEntityUnderValidation, subsRef, submittable));
        }

        return results;
    }

    public List<SingleValidationResult> validate(
            uk.ac.ebi.subs.data.submittable.Submittable entityUnderValidation,
            DataType dataTypeOfEntityUnderValidation,
            AbstractSubsRef referenceToSubmittables,
            Collection<Submittable> referencedSubmittables) {

        return this.validate(
                entityUnderValidation,
                dataTypeOfEntityUnderValidation,
                Arrays.asList(referenceToSubmittables),
                referencedSubmittables
        );
    }

    public List<SingleValidationResult> validate(
            uk.ac.ebi.subs.data.submittable.Submittable entityUnderValidation,
            DataType dataTypeOfEntityUnderValidation,
            AbstractSubsRef referenceToSubmittable,
            Submittable referencedSubmittable) {


        SingleValidationResult singleValidationResult =
                getDefaultSingleValidationResult(entityUnderValidation.getId(), ValidationAuthor.Core);

        if (referencedSubmittable == null) {
            if (referenceToSubmittable.getAccession() == null || referenceToSubmittable.getAccession().isEmpty()) {
                singleValidationResult.setMessage(String.format(FAIL_TEAM_AND_ALIAS_MESSAGE, referenceToSubmittable.getAlias(), referenceToSubmittable.getTeam()));
            } else {
                singleValidationResult.setMessage(String.format(FAIL_MESSAGE, referenceToSubmittable.getAccession()));
            }
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);

            return Arrays.asList(singleValidationResult);
        }

        List<SingleValidationResult> results = referenceRequirementsValidator.validate(
                entityUnderValidation,
                dataTypeOfEntityUnderValidation,
                referenceToSubmittable,
                referencedSubmittable
        );

        if (results.isEmpty()) {
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Pass);
            return Arrays.asList(singleValidationResult);
        } else {
            return results;
        }
    }
}
