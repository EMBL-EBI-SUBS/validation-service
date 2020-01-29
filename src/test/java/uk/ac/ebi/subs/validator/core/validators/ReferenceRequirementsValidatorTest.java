package uk.ac.ebi.subs.validator.core.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.Study;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.repository.model.StoredSubmittable;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SubmittableRepository;
import uk.ac.ebi.subs.validator.core.handlers.ValidationTestHelper;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
public class ReferenceRequirementsValidatorTest {


    Assay entityUnderValidation;

    ReferenceRequirementsValidator validator;

    Study referencedEntity;
    StudyRef reference;

    DataType dataTypeOfEntityUnderValidation;
    DataType.RefRequirement refRequirement;

    StudyRepository studyRepository;
    ValidationResultRepository validationResultRepository;

    String expectedDataTypeOfReferencedEntity = "barId";


    @Before
    public void buildUp() {
        //build up data under validation for test
        this.entityUnderValidation = new Assay();
        this.entityUnderValidation.setId("iAmBeingValidated");

        this.referencedEntity = new Study();
        this.referencedEntity.setId("fooId");

        this.reference = new StudyRef();


        //describe reference requirements
        this.refRequirement = new DataType.RefRequirement();
        refRequirement.setRefClassName(
                reference.getClass().getName()
        );
        this.refRequirement.setDataTypeIdForReferencedDocument(expectedDataTypeOfReferencedEntity);

        Set<String> requiredValidationAuthors = new HashSet<>();
        requiredValidationAuthors.add(ValidationAuthor.Ena.name());
        this.refRequirement.setAdditionalRequiredValidationAuthors(requiredValidationAuthors);


        this.dataTypeOfEntityUnderValidation = new DataType();
        Set<DataType.RefRequirement> refRequirements = new HashSet<>();
        refRequirements.add(refRequirement);
        this.dataTypeOfEntityUnderValidation.setRefRequirements(refRequirements);


        //provide mocks for repository methods
        this.studyRepository = Mockito.mock(StudyRepository.class);
        this.validationResultRepository = Mockito.mock(ValidationResultRepository.class);

        Map<Class<? extends StoredSubmittable>, SubmittableRepository<? extends StoredSubmittable>> repositoryMap = new HashMap<>();
        repositoryMap.put(uk.ac.ebi.subs.repository.model.Study.class,studyRepository);

        //finally, an actual validator
        this.validator = new ReferenceRequirementsValidator(repositoryMap,validationResultRepository);
        this.validator.setMaximumTimeToWaitInMillis(3000); // make it give up quickly, or the tests will take too long

    }

    @Test
    /**
     * if the data type of the referenced entity matches that specified in the data type of the entity under validation,
     * pass
     */
    public void matching_dataType_and_validation_passes() {
        String dataTypeId = expectedDataTypeOfReferencedEntity;
        Collection<ValidationAuthor> passingAuthors = Collections.singletonList(ValidationAuthor.Ena);

        Mockito.when(studyRepository.findOne(referencedEntity.getId()))
                .thenReturn(
                        buildStoredStudy(referencedEntity.getId(), dataTypeId, passingAuthors, Collections.emptyList(), Collections.emptyList())
                )
        ;

        List<SingleValidationResult> results = this.validator.validate(entityUnderValidation, dataTypeOfEntityUnderValidation, reference, referencedEntity);
        Assert.assertTrue(results.isEmpty());

        Mockito.verify(studyRepository).findOne(referencedEntity.getId());
    }

    @Test
    /**
     * if the data type of the referenced entity does not match that specified in the data type of the entity under
     * validation, produce an error message
     */
    public void wrong_dataType_fails() {
        String dataTypeId = expectedDataTypeOfReferencedEntity + "wrongen";
        Collection<ValidationAuthor> passingAuthors = Collections.singletonList(ValidationAuthor.Ena);

        Mockito.when(studyRepository.findOne(referencedEntity.getId()))
                .thenReturn(
                        buildStoredStudy(referencedEntity.getId(), dataTypeId, passingAuthors, Collections.emptyList(), Collections.emptyList())
                )
        ;

        List<SingleValidationResult> results = this.validator.validate(entityUnderValidation, dataTypeOfEntityUnderValidation, reference, referencedEntity);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(SingleValidationResultStatus.Error, results.get(0).getValidationStatus());

        Mockito.verify(studyRepository).findOne(referencedEntity.getId());
    }

    @Test
    /**
     * referenced entity does not immediatly have validation results for the relevant author, but it should pass eventually
     */
    public void validator_copes_with_pending_validation() {
        String dataTypeId = expectedDataTypeOfReferencedEntity;
        Collection<ValidationAuthor> authors = Collections.singletonList(ValidationAuthor.Ena);

        uk.ac.ebi.subs.repository.model.Study storedStudyWithPendingResults = buildStoredStudy(
                referencedEntity.getId(),
                dataTypeId,
                Collections.emptyList(),
                Collections.emptyList(),
                authors //pending
        );

        uk.ac.ebi.subs.repository.model.Study storedStudyWithPassingResults = buildStoredStudy(
                referencedEntity.getId(),
                dataTypeId,
                authors, //passing
                Collections.emptyList(),
                Collections.emptyList()
        );


        Mockito.when(studyRepository.findOne(referencedEntity.getId()))
                .thenReturn(storedStudyWithPendingResults)
        ;

        Mockito.when(validationResultRepository.findOne(storedStudyWithPendingResults.getValidationResult().getUuid()))
                .thenReturn(
                        storedStudyWithPendingResults.getValidationResult(), //pending results on first call
                        storedStudyWithPassingResults.getValidationResult()  //passing results on second call
                );


        List<SingleValidationResult> results = this.validator.validate(entityUnderValidation, dataTypeOfEntityUnderValidation, reference, referencedEntity);
        Assert.assertTrue(results.isEmpty());

        Mockito.verify(studyRepository).findOne(referencedEntity.getId());

        Mockito.verify(validationResultRepository, Mockito.times(2))
                .findOne(storedStudyWithPendingResults.getValidationResult().getUuid());
    }

    @Test
    /**
     * referenced entity has validation errors from a validation author that the data type for the entity under
     * validation requires to pass, produce an error message
     */
    public void error_for_ValidationAuthor_fails() {
        String dataTypeId = expectedDataTypeOfReferencedEntity;
        Collection<ValidationAuthor> failingAuthors = Collections.singletonList(ValidationAuthor.Ena);

        Mockito.when(studyRepository.findOne(referencedEntity.getId()))
                .thenReturn(
                        buildStoredStudy(referencedEntity.getId(), dataTypeId, Collections.emptyList(), failingAuthors, Collections.emptyList())
                )
        ;

        List<SingleValidationResult> results = this.validator.validate(entityUnderValidation, dataTypeOfEntityUnderValidation, reference, referencedEntity);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(SingleValidationResultStatus.Error, results.get(0).getValidationStatus());

        Mockito.verify(studyRepository).findOne(referencedEntity.getId());
    }

    @Test(expected = RuntimeException.class)
    /**
     * referenced entity does not immediatly have validation results for the relevant author, but it should pass eventually
     */
    public void validator_errors_if_pending_for_too_long() {
        String dataTypeId = expectedDataTypeOfReferencedEntity;
        Collection<ValidationAuthor> authors = Collections.singletonList(ValidationAuthor.Ena);

        uk.ac.ebi.subs.repository.model.Study storedStudyWithPendingResults = buildStoredStudy(
                referencedEntity.getId(),
                dataTypeId,
                Collections.emptyList(),
                Collections.emptyList(),
                authors //pending
        );

        Mockito.when(studyRepository.findOne(referencedEntity.getId()))
                .thenReturn(
                        storedStudyWithPendingResults
                );

        Mockito.when(validationResultRepository.findOne(storedStudyWithPendingResults.getValidationResult().getUuid()))
                .thenReturn(
                        storedStudyWithPendingResults.getValidationResult()
                );

        this.validator.validate(entityUnderValidation, dataTypeOfEntityUnderValidation, reference, referencedEntity);
    }


    private uk.ac.ebi.subs.repository.model.Study buildStoredStudy(String id, String dataTypeId, Collection<ValidationAuthor> passingAuthors, Collection<ValidationAuthor> failingAuthors, Collection<ValidationAuthor> pendingAuthors) {
        uk.ac.ebi.subs.repository.model.Study storedStudy = new uk.ac.ebi.subs.repository.model.Study();
        DataType dt = new DataType();
        dt.setId(dataTypeId);
        storedStudy.setDataType(dt);

        ValidationResult vr = new ValidationResult();
        vr.setUuid("iamatestvr");
        Map<ValidationAuthor, List<SingleValidationResult>> results = new HashMap<>();

        for (ValidationAuthor a : passingAuthors) {
            results.put(a, Collections.singletonList(ValidationTestHelper.pass(id, a)));
        }
        for (ValidationAuthor a : failingAuthors) {
            results.put(a, Collections.singletonList(ValidationTestHelper.fail(id, a)));
        }
        for (ValidationAuthor a : pendingAuthors) {
            results.put(a, Collections.emptyList());
        }
        vr.setExpectedResults(results);
        storedStudy.setValidationResult(vr);

        return storedStudy;
    }

}
