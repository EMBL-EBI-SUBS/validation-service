package uk.ac.ebi.subs.validator.coordinator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.*;
import uk.ac.ebi.subs.repository.model.*;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.validator.config.MongoDBDependentTest;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = {SampleRepository.class, SubmissionRepository.class, SubmissionStatusRepository.class})
@Category(MongoDBDependentTest.class)
@EnableAutoConfiguration
@SpringBootTest(classes = SampleValidationMessageEnvelopeExpander.class)
public class SampleValidationMessageEnvelopeExpanderTest {

    @Autowired
    SampleRepository sampleRepository;

    @Autowired
    SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    SampleValidationMessageEnvelopeExpander sampleValidatorMessageEnvelopeExpander;

    Team team;
    Submission submission;
    List<Sample> savedSampleList;

    @Before
    public void setup() {
        team = MesssageEnvelopeTestHelper.createTeam();
        submission = MesssageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository, submissionRepository, team);
        savedSampleList = MesssageEnvelopeTestHelper.createAndSaveSamples(sampleRepository, submission, team, 1);
    }

    @After
    public void finish() {
        sampleRepository.delete(savedSampleList);
        submissionRepository.delete(submission);
        submissionStatusRepository.delete(submission.getSubmissionStatus());
    }


    @Test
    public void testExpandEnvelopeSameSubmissionByAccession() throws Exception {
        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope();

        for (Sample sample : savedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAccession(sample.getAccession());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        assertThat(savedSampleList, is(sampleList));
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAlias() throws Exception {

        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope();

        for (Sample sample : savedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAlias(sample.getAlias());
            sampleRelationship.setTeam(team.getName());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        assertThat(savedSampleList, is(sampleList));
    }

    private SampleValidationMessageEnvelope createSampleValidationMessageEnvelope() {
        SampleValidationMessageEnvelope sampleValidationMessageEnvelope = new SampleValidationMessageEnvelope();
        uk.ac.ebi.subs.data.submittable.Sample submittableSample = new uk.ac.ebi.subs.data.submittable.Sample();
        submittableSample.setTeam(team);
        submittableSample.setAccession(UUID.randomUUID().toString());
        submittableSample.setAlias(UUID.randomUUID().toString());
        sampleValidationMessageEnvelope.setEntityToValidate(submittableSample);
        return sampleValidationMessageEnvelope;
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAccessionNotInRepo() throws Exception {
        final SampleValidationMessageEnvelope sampleValidationMessageEnvelope = createSampleValidationMessageEnvelope();
        List<Sample>notSavedSampleList = MesssageEnvelopeTestHelper.createSamples(submission, team, 1);

        for (Sample sample : notSavedSampleList) {
            SampleRelationship sampleRelationship = new SampleRelationship();
            sampleRelationship.setAccession(sample.getAccession());
            sampleValidationMessageEnvelope.getEntityToValidate().getSampleRelationships().add(sampleRelationship);
        }

        sampleValidatorMessageEnvelopeExpander.expandEnvelope(sampleValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = sampleValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        assertThat(sampleList, is(empty()));
    }


}