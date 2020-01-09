package uk.ac.ebi.subs.validator.coordinator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.ProcessingStatusRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.ProjectRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = {"uk.ac.ebi.subs.repository.repos", "uk.ac.ebi.subs.validator.repository"})
@EnableAutoConfiguration
@SpringBootTest(classes = SampleValidationMessageEnvelopeExpander.class)
@MockBeans({
        @MockBean(CoordinatorListener.class)
})
public class SampleValidationMessageEnvelopeExpanderTest {

    @Autowired
    SampleRepository sampleRepository;

    @Autowired
    SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    SampleValidationMessageEnvelopeExpander sampleValidatorMessageEnvelopeExpander;

    @MockBean
    private SubmittableFinderService submittableFinderService;

    Team team;
    Submission submission;
    List<Sample> savedSampleList;

    @Before
    public void setup() {
        team = MessageEnvelopeTestHelper.createTeam();
        submission = MessageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository, submissionRepository, team);
        savedSampleList = MessageEnvelopeTestHelper.createAndSaveSamples(sampleRepository, submission, team, 1);
    }

    @After
    public void finish() {
        sampleRepository.deleteAll(savedSampleList);
        submissionRepository.delete(submission);
        submissionStatusRepository.delete(submission.getSubmissionStatus());
    }


    @Test
    public void testExpandEnvelopeSameSubmissionByAccession() throws Exception {
        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope =
                createSampleValidationMessageEnvelope(submission.getId());

        Mockito.when(submittableFinderService.findSampleByAccession(anyString()))
                .thenReturn(savedSampleList.get(0));

        for (Sample sample : savedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAccession(sample.getAccession());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList =
                sampleValidationMessageEnvelope.getSampleList().stream()
                        .map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        SampleAssertionHelper.assertSampleList(savedSampleList, sampleList);
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAlias() throws Exception {

        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope =
                createSampleValidationMessageEnvelope(submission.getId());

        Mockito.when(submittableFinderService.findSampleByTeamNameAndAlias(any(SampleRelationship.class)))
                .thenReturn(savedSampleList.get(0));

        for (Sample sample : savedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAlias(sample.getAlias());
            sampleRelationship.setTeam(team.getName());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList =
                sampleValidationMessageEnvelope.getSampleList().stream()
                        .map(Submittable::getBaseSubmittable).collect(Collectors.toList());

        SampleAssertionHelper.assertSampleList(savedSampleList, sampleList);
    }

    private SampleValidationMessageEnvelope createSampleValidationMessageEnvelope(String submissionId) {
        SampleValidationMessageEnvelope sampleValidationMessageEnvelope = new SampleValidationMessageEnvelope();
        uk.ac.ebi.subs.data.submittable.Sample submittableSample = new uk.ac.ebi.subs.data.submittable.Sample();
        submittableSample.setTeam(team);
        submittableSample.setAccession(UUID.randomUUID().toString());
        submittableSample.setAlias(UUID.randomUUID().toString());
        sampleValidationMessageEnvelope.setEntityToValidate(submittableSample);
        sampleValidationMessageEnvelope.setSubmissionId(submissionId);
        return sampleValidationMessageEnvelope;
    }

    @Test
    public void testExpandEnvelopeByAccessionNotInRepoAndNotInArchive() throws Exception {
        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope(submission.getId());
        List<Sample> notSavedSampleList = MessageEnvelopeTestHelper.createSamples(submission, team, 1);

        Mockito.when(submittableFinderService.findSampleByAccession(anyString()))
                .thenReturn(null);

        for (Sample sample : notSavedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAccession(sample.getAccession());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        assertThat(sampleList, is(empty()));
    }

    @Test
    public void testExpandEnvelopeByAliasNotExistsInRepoAndNotInArchive() throws Exception {
        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope(submission.getId());
        List<Sample> notSavedSampleList = MessageEnvelopeTestHelper.createSamples(submission, team, 1);

        Mockito.when(submittableFinderService.findSampleByTeamNameAndAlias(any(SampleRelationship.class)))
                .thenReturn(null);

        for (Sample sample : notSavedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAlias(sample.getAlias());
            sampleRelationship.setTeam(team.getName());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        assertThat(sampleList, is(empty()));
    }

    @Test
    public void testExpandEnvelopeByAccessionNotExistsInRepoButExistsInArchive() throws Exception {
        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope(submission.getId());
        Sample sampleInArchive = MessageEnvelopeTestHelper.createSamples(submission, team, 1).get(0);
        sampleInArchive.setAccession(UUID.randomUUID().toString());

        Mockito.when(submittableFinderService.findSampleByAccession(anyString()))
                .thenReturn(null);

        SampleRelationship sampleRelationship = new SampleRelationship();
        sampleRelationship.setAccession(sampleInArchive.getAccession());
        sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        assertThat(sampleList, is(empty()));
    }
}