package uk.ac.ebi.subs.validator.core.validators;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.component.AbstractSubsRef;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.data.submittable.Submittable;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReferenceRequirementsValidator {

    @NonNull
    private StudyRepository studyRepository;

    @NonNull
    private ValidationResultRepository validationResultRepository;

    public List<SingleValidationResult> validate(Submittable entityUnderValidation, DataType dataType, AbstractSubsRef ref, Submittable referencedEntity) {

        //bail out early if there are no ref requirements defined
        if (dataType.getRefRequirements() == null || dataType.getRefRequirements().isEmpty()) {
            return Collections.emptyList();
        }

        Set<DataType.RefRequirement> refRequirementSet = dataType.getRefRequirements();

        //is there a ref requirement for this reference?
        Optional<DataType.RefRequirement> optionalRefRequirement = refRequirementSet
                .stream()
                .filter(r -> r.getRefClassName().equals(ref.getClass().getName()))
                .findAny();


        //bail out if there are no ref requirements for this ref type
        if (!optionalRefRequirement.isPresent()) {
            return Collections.emptyList();
        }

        DataType.RefRequirement refRequirement = optionalRefRequirement.get();

        List<SingleValidationResult> results = new ArrayList<>();


        Pair<DataType, ValidationResult> pair = fetchDataTypeAndValidationResult(referencedEntity);
        DataType dataTypeOfReferencedEntity = pair.getFirst();

        if (!refRequirement.getDataTypeIdForReferencedDocument().equals(dataTypeOfReferencedEntity.getId())) {
            //e.g. referenced a metabolights study from an ENA assay

            String refType = simpleClassNameLowerCase(referencedEntity);
            String errorMessage = String.join(" ",
                    "The",
                    refType,
                    referencedEntity.getAlias(),
                    "is a",
                    dataTypeOfReferencedEntity.getDisplayNameSingular(),
                    "but a",
                    dataType.getDisplayNameSingular(),
                    "is required instead"
            );

            SingleValidationResult result = errorResult(entityUnderValidation, errorMessage);

            results.add(result);

        }

        Collection<ValidationAuthor> requiredValidationAuthors = refRequirement.getAdditionalRequiredValidationAuthors()
                .stream()
                .map(ValidationAuthor::valueOf)
                .collect(Collectors.toSet());

        if (requiredValidationAuthors != null && !requiredValidationAuthors.isEmpty()) {
            ValidationResult validationResult = pair.getSecond();

            while (waitingForResults(validationResult, requiredValidationAuthors)) {
                sleep();
                validationResult = validationResultRepository.findOne(validationResult.getUuid());
            }

            for (ValidationAuthor author : requiredValidationAuthors) {

                if (validationResultContainsErrorsForAuthor(validationResult, author)) {
                    String refType = simpleClassNameLowerCase(referencedEntity);

                    String errorMessage = String.join(" ",
                            "The",
                            refType,
                            referencedEntity.getAlias(),
                            "must pass the ",
                            author.name(),
                            "validation standard before it can be referenced here"
                    );

                    SingleValidationResult result = errorResult(entityUnderValidation, errorMessage);

                    results.add(result);
                }
            }
        }


        return results;
    }

    private String simpleClassNameLowerCase(Submittable referencedEntity) {
        return referencedEntity.getClass().getSimpleName().toLowerCase();
    }

    private SingleValidationResult errorResult(Submittable entityUnderValidation, String errorMessage) {
        SingleValidationResult result = ValidatorHelper.singleValidationResult(
                entityUnderValidation.getId(),
                ValidationAuthor.Core,
                SingleValidationResultStatus.Error
        );

        result.setMessage(errorMessage);
        return result;
    }

    private boolean validationResultContainsErrorsForAuthor(ValidationResult validationResult, ValidationAuthor author) {
        return validationResult.getExpectedResults()
                .get(author).stream()
                .anyMatch(r -> SingleValidationResultStatus.Error.equals(r.getValidationStatus()));

    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean waitingForResults(ValidationResult validationResult, Collection<ValidationAuthor> requiredValidationAuthors) {
        Map<ValidationAuthor, List<SingleValidationResult>> results = validationResult.getExpectedResults();

        for (ValidationAuthor author : requiredValidationAuthors) {
            List<SingleValidationResult> authorResults = results.get(author);
            if (authorResults == null || authorResults.isEmpty()) {
                return true; //no results = still waiting
            }
        }

        return false;

    }

    private Pair<DataType, ValidationResult> fetchDataTypeAndValidationResult(Submittable submittable) {
        SubmittableRepository repo = null;

        if (submittable instanceof Study) {
            repo = studyRepository;
        }

        StoredSubmittable storedSubmittable = null;

        if (repo != null) {
            storedSubmittable = repo.findOne(submittable.getId());
        }

        if (storedSubmittable != null) {
            return Pair.of(storedSubmittable.getDataType(), storedSubmittable.getValidationResult());
        }

        return null;
    }

}
