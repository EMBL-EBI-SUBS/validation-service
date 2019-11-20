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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferenceValidator {
    static final String FAIL_MESSAGE = "Could not find reference target: %s ";
    static final String FAIL_TEAM_AND_ALIAS_MESSAGE = "Could not find reference for ALIAS: %s in TEAM: %s ";
    static final String DUPLICATED_ACCESSION_MESSAGE = "The sample with accession: %s were duplicated in the sample relationship";
    static final String DUPLICATED_ALIAS_PLUS_TEAM_MESSAGE = "The sample with alias: %s and team: %S were duplicated in the sample relationship";

    @NonNull
    private ReferenceRequirementsValidator referenceRequirementsValidator;


    public List<SingleValidationResult> validate(
            uk.ac.ebi.subs.data.submittable.Submittable entityUnderValidation,
            DataType dataTypeOfEntityUnderValidation,
            Collection<? extends AbstractSubsRef> referencesToSubmittables,
            Collection<? extends Submittable> referencedSubmittables) {

        List<SingleValidationResult> results = new ArrayList<>();

        results.addAll(validateDuplicatedAccession(entityUnderValidation, referencedSubmittables));

        results.addAll(validateDuplicatedAliasPlusTeamName(entityUnderValidation, referencedSubmittables));

        final Map<String, Submittable> sampleAccessionMap = referencedSubmittables.stream()
                .filter(referencedSubmittable -> referencedSubmittable.getAccession() != null)
                .collect(
                        Collectors.toMap(Submittable::getAccession, sample -> sample,
                                (sample1, sample2) -> sample1 )
                );
        final Map<String, Submittable> sampleAliasMap = referencedSubmittables.stream()
                .filter(referencedSubmittable -> referencedSubmittable.getAlias() != null && referencedSubmittable.getTeam().getName() != null)
                .collect(
                        Collectors.toMap(sample -> sample.getAlias() + sample.getTeam().getName(), sample -> sample,
                                (sample1, sample2) -> sample1 )
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

    private List<SingleValidationResult> validateDuplicatedAliasPlusTeamName(
            uk.ac.ebi.subs.data.submittable.Submittable entityUnderValidation,
            Collection<? extends Submittable> referencedSubmittables) {
        List<SingleValidationResult> results = new ArrayList<>();
        Set<? extends Submittable> submittablesWithDuplicateAliasPlusTeamName = checkDuplicateAliasPlusTeamName(referencedSubmittables);

        if (!submittablesWithDuplicateAliasPlusTeamName.isEmpty()) {
            results.addAll(
                    submittablesWithDuplicateAliasPlusTeamName.stream()
                            .map(submittable ->
                                    generateSingleValidationResultForDuplication(
                                            entityUnderValidation, DUPLICATED_ALIAS_PLUS_TEAM_MESSAGE,
                                            Arrays.asList(submittable.getAlias(), submittable.getTeam().getName())))
                            .collect(Collectors.toList())
            );
        }

        return results;
    }

    private List<SingleValidationResult> validateDuplicatedAccession(uk.ac.ebi.subs.data.submittable.Submittable entityUnderValidation,
                                             Collection<? extends Submittable> referencedSubmittables) {
        List<SingleValidationResult> results = new ArrayList<>();
        Set<? extends Submittable> submittablesWithDuplicatedAccession = checkDuplicateAccession(referencedSubmittables);

        if (!submittablesWithDuplicatedAccession.isEmpty()) {
            results.addAll(
                    submittablesWithDuplicatedAccession.stream()
                            .map(submittable ->
                                    generateSingleValidationResultForDuplication(
                                            entityUnderValidation, DUPLICATED_ACCESSION_MESSAGE,
                                            Collections.singletonList(submittable.getAccession())))
                            .collect(Collectors.toList())
            );
        }

        return results;
    }

    private Set<? extends Submittable> checkDuplicateAccession(Collection<? extends Submittable> referencedSubmittables) {
        Set<String> allItems = new HashSet<>();
        return referencedSubmittables.stream()
                .filter(referencedSubmittable -> referencedSubmittable.getAccession() != null)
                .filter(submittable -> !allItems.add(submittable.getAccession()))
                .collect(Collectors.toSet());
    }

    private Set<? extends Submittable> checkDuplicateAliasPlusTeamName(Collection<? extends Submittable> referencedSubmittables) {

        Set<String> allItems = new HashSet<>();
        return referencedSubmittables.stream()
                .filter(submittable -> !allItems.add(submittable.getAlias() + submittable.getTeam().getName()))
                .collect(Collectors.toSet());
    }

    private SingleValidationResult generateSingleValidationResultForDuplication(
            uk.ac.ebi.subs.data.submittable.Submittable entityUnderValidation, String validationMessage, List<String> messageParams) {

        SingleValidationResult singleValidationResult = getDefaultSingleValidationResult(entityUnderValidation);
        singleValidationResult.setMessage(String.format(validationMessage, messageParams.toArray()));
        singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);

        return singleValidationResult;
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

        SingleValidationResult singleValidationResult = getDefaultSingleValidationResult(entityUnderValidation);

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

    private SingleValidationResult getDefaultSingleValidationResult(uk.ac.ebi.subs.data.submittable.Submittable entityUnderValidation) {
        return ValidatorHelper.getDefaultSingleValidationResult(entityUnderValidation.getId(), ValidationAuthor.Core);
    }
}
