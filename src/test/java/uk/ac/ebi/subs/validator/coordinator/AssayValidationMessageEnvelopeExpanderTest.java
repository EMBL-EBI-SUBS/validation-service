package uk.ac.ebi.subs.validator.coordinator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.SampleRef;
import uk.ac.ebi.subs.data.component.SampleUse;
import uk.ac.ebi.subs.data.component.StudyRef;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.model.Study;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.repository.repos.submittables.StudyRepository;
import uk.ac.ebi.subs.validator.data.AssayValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = {"uk.ac.ebi.subs.repository.repos", "uk.ac.ebi.subs.validator.repository"})
@EnableAutoConfiguration
@SpringBootTest(classes = AssayValidationMessageEnvelopeExpander.class)
public class AssayValidationMessageEnvelopeExpanderTest {

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    SampleRepository sampleRepository;

    @Autowired
    SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    AssayValidationMessageEnvelopeExpander assayValidatorMessageEnvelopeExpander;

    Team team;
    Submission submission;
    Submission submission2;
    List<Sample> savedSampleList;
    Study savedStudy;

    @Before
    public void setup() {
        team = MessageEnvelopeTestHelper.createTeam();
        submission = MessageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository, submissionRepository, team);
        submission2 = MessageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository, submissionRepository, team);
        savedStudy = MessageEnvelopeTestHelper.createAndSaveStudy(studyRepository, submission, team);
        savedSampleList = MessageEnvelopeTestHelper.createAndSaveSamples(sampleRepository, submission, team, 1);
    }

    @After
    public void finish() {
        studyRepository.delete(savedStudy);
        sampleRepository.deleteAll(savedSampleList);
        submissionRepository.deleteAll(Arrays.asList(submission, submission2));
        submissionStatusRepository.delete(submission.getSubmissionStatus());
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAccessionForStudy() throws Exception {
        AssayValidationMessageEnvelope assayValidationMessageEnvelope = createAssayValidationMessageEnvelope(submission.getId());
        StudyRef studyRef = new StudyRef();
        studyRef.setAccession(savedStudy.getAccession());
        assayValidationMessageEnvelope.getEntityToValidate().setStudyRef(studyRef);
        assayValidatorMessageEnvelopeExpander.expandEnvelope(assayValidationMessageEnvelope);
        assertThat(savedStudy, is(assayValidationMessageEnvelope.getStudy().getBaseSubmittable()));
    }

    @Test
    public void testExpandEnvelopeWithStudyWithoutAccessionInSameSubmissionForAssay() {
        AssayValidationMessageEnvelope assayValidationMessageEnvelope = createAssayValidationMessageEnvelope(submission.getId());

        Study studyWithoutAccessionID = MessageEnvelopeTestHelper.createStudy(submission, team);
        studyWithoutAccessionID.setAccession(null);
        studyRepository.save(studyWithoutAccessionID);

        StudyRef studyRef = new StudyRef();
        studyRef.setAlias(studyWithoutAccessionID.getAlias());
        studyRef.setTeam(studyWithoutAccessionID.getTeam().getName());
        assayValidationMessageEnvelope.getEntityToValidate().setStudyRef(studyRef);
        assayValidatorMessageEnvelopeExpander.expandEnvelope(assayValidationMessageEnvelope);
        assertThat(studyWithoutAccessionID, is(assayValidationMessageEnvelope.getStudy().getBaseSubmittable()));
    }

    @Test
    public void testExpandEnvelopeWithStudyWithoutAccessionInAnotherSubmissionForAssay() {
        AssayValidationMessageEnvelope assayValidationMessageEnvelope = createAssayValidationMessageEnvelope(submission.getId());

        Study studyWithoutAccessionID = MessageEnvelopeTestHelper.createStudy(submission2, team);
        studyWithoutAccessionID.setAccession(null);
        studyRepository.save(studyWithoutAccessionID);

        StudyRef studyRef = new StudyRef();
        studyRef.setAlias(studyWithoutAccessionID.getAlias());
        studyRef.setTeam(studyWithoutAccessionID.getTeam().getName());
        assayValidationMessageEnvelope.getEntityToValidate().setStudyRef(studyRef);
        assayValidatorMessageEnvelopeExpander.expandEnvelope(assayValidationMessageEnvelope);
        assertNull(assayValidationMessageEnvelope.getStudy());
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAccessionForSampleList() throws Exception {
        AssayValidationMessageEnvelope assayValidationMessageEnvelope = createAssayValidationMessageEnvelope(submission.getId());

        for (Sample sample : savedSampleList) {
            SampleRef sampleRef = new SampleRef();
            sampleRef.setAccession(sample.getAccession());
            assayValidationMessageEnvelope.getEntityToValidate().getSampleUses().add(new SampleUse(sampleRef));
        }

        assayValidatorMessageEnvelopeExpander.expandEnvelope(assayValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = assayValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        SampleAssertionHelper.assertSampleList(savedSampleList, sampleList);
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAliasForStudy() throws Exception {
        AssayValidationMessageEnvelope assayValidationMessageEnvelope = createAssayValidationMessageEnvelope(submission.getId());
        StudyRef studyRef = new StudyRef();
        studyRef.setAlias(savedStudy.getAlias());
        studyRef.setTeam(savedStudy.getTeam().getName());
        assayValidationMessageEnvelope.getEntityToValidate().setStudyRef(studyRef);
        assayValidatorMessageEnvelopeExpander.expandEnvelope(assayValidationMessageEnvelope);
        assertThat(savedStudy, is(assayValidationMessageEnvelope.getStudy().getBaseSubmittable()));
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAliasForSampleList() throws Exception {
        AssayValidationMessageEnvelope assayValidationMessageEnvelope = createAssayValidationMessageEnvelope(submission.getId());

        for (Sample sample : savedSampleList) {
            SampleRef sampleRef = new SampleRef();
            sampleRef.setAlias(sample.getAlias());
            sampleRef.setTeam(team.getName());
            assayValidationMessageEnvelope.getEntityToValidate().getSampleUses().add(new SampleUse(sampleRef));
        }

        assayValidatorMessageEnvelopeExpander.expandEnvelope(assayValidationMessageEnvelope);
        final List<uk.ac.ebi.subs.data.submittable.Sample> sampleList = assayValidationMessageEnvelope.getSampleList().stream().map(Submittable::getBaseSubmittable).collect(Collectors.toList());
        SampleAssertionHelper.assertSampleList(savedSampleList, sampleList);
    }

    private AssayValidationMessageEnvelope createAssayValidationMessageEnvelope(String submissionId) {
        AssayValidationMessageEnvelope assayValidationMessageEnvelope = new AssayValidationMessageEnvelope();
        uk.ac.ebi.subs.data.submittable.Assay submittableAssay = new uk.ac.ebi.subs.data.submittable.Assay();
        submittableAssay.setTeam(team);
        submittableAssay.setAccession(UUID.randomUUID().toString());
        submittableAssay.setAlias(UUID.randomUUID().toString());
        assayValidationMessageEnvelope.setEntityToValidate(submittableAssay);
        assayValidationMessageEnvelope.setSubmissionId(submissionId);
        return assayValidationMessageEnvelope;
    }
}