package uk.ac.ebi.subs.validator.core.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.subs.data.component.AbstractSubsRef;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.repository.model.DataType;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReferenceValidator.class)
public class ReferenceValidatorTest {

    private static final String SAME_AS_RELATIONSHIP_NATURE = "same as";
    @Autowired
    ReferenceValidator referenceValidator;

    @MockBean
    ReferenceRequirementsValidator referenceRequirementsValidator;

    Team team;
    static final String TEAM_NAME = "Test-Team";

    static final String EXPECTED_ID = "foo";

    Assay entityUnderValidation = new Assay();
    DataType dataType = new DataType();

    @Before
    public void setup () {

        team = createTeam(TEAM_NAME);
        entityUnderValidation.setId(EXPECTED_ID);
    }

    @Test
    public void whenReferencedSubmittableHasAccessionIDAndExistsInRepository_ThenValidationShouldPass() {

        Submittable<Sample> sample = createSample(team);
        SampleRef sampleRef = new SampleRef();
        sampleRef.setAccession(sample.getAccession());

        List<SingleValidationResult> results = referenceValidator.validate(entityUnderValidation,dataType,sampleRef, sample);

        SingleValidationResult result = results.get(0);

        Assert.assertEquals(SingleValidationResultStatus.Pass, result.getValidationStatus());
        Assert.assertEquals(EXPECTED_ID, result.getEntityUuid());
        Assert.assertNull(result.getMessage());
    }

    @Test
    public void whenReferencedSubmittableHasAliasAndTeamAndExistsInRepository_ThenValidationShouldPass() {
        Submittable<Sample> sample = createSample(team);
        SampleRef sampleRef = new SampleRef();
        sampleRef.setAlias(sample.getAlias());
        sampleRef.setTeam(team.getName());

        List<SingleValidationResult> results = referenceValidator.validate(entityUnderValidation,dataType, sampleRef, sample);

        SingleValidationResult result = results.get(0);

        Assert.assertEquals(SingleValidationResultStatus.Pass, result.getValidationStatus());
        Assert.assertEquals(EXPECTED_ID, result.getEntityUuid());
        Assert.assertNull(result.getMessage());
    }

    @Test
    public void when2ReferencedSubmittableHasSampleAliasButNoAccession_ThenValidationShouldPass() {

        final List<Submittable> sampleList = createSamplesWithoutAccession(team, 2);

        final List<AbstractSubsRef> sampleRefList = sampleList.stream().map(sample -> {
            SampleRef sampleRef = new SampleRef();
            sampleRef.setAlias(sample.getAlias());
            sampleRef.setTeam(sample.getTeam().getName());
            return sampleRef;
        }).collect(Collectors.toList());


        List<SingleValidationResult> validationResults =
                referenceValidator.validate(entityUnderValidation,dataType, sampleRefList, sampleList);

        validationResults.forEach( validationResult -> {
            Assert.assertEquals(SingleValidationResultStatus.Pass, validationResult.getValidationStatus());
            Assert.assertEquals(EXPECTED_ID, validationResult.getEntityUuid());
            Assert.assertNull(validationResult.getMessage());

        });
    }

    @Test
    public void whenReferenced2SubmittableHasSameAccession_ThenValidationStillPass() {
        String sameAccession = UUID.randomUUID().toString();
        List<Submittable> sampleList = createSamples(team, 2);
        sampleList = sampleList.stream().map(sample -> {
            sample.setAccession(sameAccession);
            return sample;
        }).collect(Collectors.toList());

        final List<AbstractSubsRef> sampleRefList = sampleList.stream().map(sample -> {
            SampleRelationship sampleRef = new SampleRelationship();
            sampleRef.setAccession(sample.getAccession());
            sampleRef.setRelationshipNature(SAME_AS_RELATIONSHIP_NATURE);
            return sampleRef;
        }).collect(Collectors.toList());


        List<SingleValidationResult> validationResults =
                referenceValidator.validate(entityUnderValidation,dataType, sampleRefList, sampleList);

        validationResults.forEach( validationResult -> {
            Assert.assertEquals(SingleValidationResultStatus.Pass, validationResult.getValidationStatus());
            Assert.assertEquals(EXPECTED_ID, validationResult.getEntityUuid());
            Assert.assertNull(validationResult.getMessage());

        });
    }

    @Test
    public void whenReferenced2SubmittableHasSameAliasAndTeamName_ThenValidationStillPass() {
        final String sameAlias = "same alias";
        final String sameTeamName = "same team name";

        List<Submittable> sampleList = createSamples(team, 2);
        sampleList = sampleList.stream().map(sample -> {
            sample.setAlias(sameAlias);
            sample.getTeam().setName(sameTeamName);
            return sample;
        }).collect(Collectors.toList());

        final List<AbstractSubsRef> sampleRefList = sampleList.stream().map(sample -> {
            SampleRef sampleRef = new SampleRef();
            sampleRef.setAlias(sample.getAlias());
            sampleRef.setTeam(sample.getTeam().getName());
            return sampleRef;
        }).collect(Collectors.toList());


        List<SingleValidationResult> validationResults =
                referenceValidator.validate(entityUnderValidation,dataType, sampleRefList, sampleList);

        validationResults.forEach( validationResult -> {
            Assert.assertEquals(SingleValidationResultStatus.Pass, validationResult.getValidationStatus());
            Assert.assertEquals(EXPECTED_ID, validationResult.getEntityUuid());
            Assert.assertNull(validationResult.getMessage());

        });
    }

    @Test
    public void whenReferencedSubmittableDefinedByAccessionButNotExists_ThenValidationShouldFail() {
        SampleRef sampleRef = new SampleRef();
        sampleRef.setAccession(UUID.randomUUID().toString());

        Submittable<Sample> nullSample = null;

        List<SingleValidationResult> results = referenceValidator.validate(entityUnderValidation,dataType, sampleRef, nullSample);

        SingleValidationResult result = results.get(0);


        Assert.assertEquals(SingleValidationResultStatus.Error, result.getValidationStatus());
    }

    @Test
    public void whenReferencedSubmittableDefinedByAliasAndTeamButNotExists_ThenValidationShouldFail() {
        SampleRef sampleRef = new SampleRef();
        sampleRef.setAlias(UUID.randomUUID().toString());
        sampleRef.setTeam(team.getName());

        Submittable<Sample> nullSample = null;

        List<SingleValidationResult> results = referenceValidator.validate(entityUnderValidation,dataType, sampleRef, nullSample);

        Assert.assertEquals(SingleValidationResultStatus.Error, results.get(0).getValidationStatus());
    }

    @Test
    public void whenReferencedSamplesContainsOneNonExistingSample_ThenShouldBeExactlyOneError() {
        final List<Submittable> sampleList = createSamples(team, 10);

        final List<AbstractSubsRef> sampleRefList = sampleList.stream().map(sample -> {
            SampleRef sampleRef = new SampleRef();
            sampleRef.setAccession(sample.getAccession());
            return sampleRef;
        }).collect(Collectors.toList());

        SampleRef sampleRefNotFound = new SampleRef();
        sampleRefNotFound.setAccession(UUID.randomUUID().toString());
        sampleRefList.add(sampleRefNotFound);


        List<SingleValidationResult> results = referenceValidator.validate(
                entityUnderValidation,dataType,
                sampleRefList,
                sampleList
        );

        long errorCount = results.stream().filter(r -> r.getValidationStatus().equals(SingleValidationResultStatus.Error)).count();


        Assert.assertEquals(1L, errorCount);

    }

    static List<Submittable> createSamples (Team team, int sampleNumber) {
        List<Submittable> sampleList = new ArrayList<>(sampleNumber);
        for (int i = 0; i < sampleNumber; i++ ) {
            sampleList.add(createSample(team));
        }
        return sampleList;
    }

    static List<Submittable> createSamplesWithoutAccession (Team team, int sampleNumber) {
        List<Submittable> sampleList = new ArrayList<>(sampleNumber);
        for (int i = 0; i < sampleNumber; i++ ) {
            sampleList.add(createSampleWithouAccession(team));
        }
        return sampleList;
    }

    static Submittable<Sample> createSampleWithouAccession(Team team) {
        Submittable<Sample> sample = createSample(team);
        sample.setAccession(null);

        return sample;
    }

    static Submittable<Sample> createSample (Team team) {
        Sample sample = new Sample();
        sample.setTeam(team);
        String alias = UUID.randomUUID().toString();
        String accession = UUID.randomUUID().toString();
        sample.setAlias(alias);
        sample.setAccession(accession);

        return new Submittable<>(sample,"testSubId");

    }

    static Team createTeam (String teamName) {
        Team team = new Team();
        team.setName(teamName);
        return team;
    }

}