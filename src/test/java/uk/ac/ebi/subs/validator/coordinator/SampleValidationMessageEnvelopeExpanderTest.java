package uk.ac.ebi.subs.validator.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = {SampleRepository.class, SubmissionRepository.class, SubmissionStatusRepository.class, ValidationResultRepository.class})
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
    private RestTemplate restTemplate;

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
        sampleRepository.delete(savedSampleList);
        submissionRepository.delete(submission);
        submissionStatusRepository.delete(submission.getSubmissionStatus());
    }


    @Test
    public void testExpandEnvelopeSameSubmissionByAccession() {
        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope(submission.getId());

        for (Sample sample : savedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAccession(sample.getAccession());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        SampleAssertionHelper.assertSampleList(savedSampleList, sampleList);
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAlias() {

        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope(submission.getId());

        for (Sample sample : savedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAlias(sample.getAlias());
            sampleRelationship.setTeam(team.getName());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());

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
    public void testExpandEnvelopeByAccessionNotInRepoAndNotInArchive() {
        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope(submission.getId());
        List<Sample> notSavedSampleList = MessageEnvelopeTestHelper.createSamples(submission, team, 1);

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

        ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(restTemplate.getForEntity(anyString(), anyObject()))
                .thenReturn(new ResponseEntity<>(objectMapper.writeValueAsString(notSavedSampleList.get(0)), HttpStatus.OK));


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

        Mockito.when(restTemplate.getForEntity(anyString(), anyObject()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        SampleRelationship sampleRelationship = new SampleRelationship();
        sampleRelationship.setAccession(sampleInArchive.getAccession());
        sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        assertThat(sampleList, is(empty()));
    }
}