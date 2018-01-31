package uk.ac.ebi.subs.validator.coordinator;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Project;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.ProjectRepository;
import uk.ac.ebi.subs.validator.config.MongoDBDependentTest;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = {ProjectRepository.class, SubmissionRepository.class, SubmissionStatusRepository.class})
@Category(MongoDBDependentTest.class)
@EnableAutoConfiguration
@SpringBootTest(classes = StudyValidationMessageEnvelopeExpander.class)
public class StudyValidationMessageEnvelopeExpanderTest {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    SubmissionStatusRepository submissionStatusRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    StudyValidationMessageEnvelopeExpander studyValidationMessageEnvelopeExpander;

    Team team;



    @Test
    public void testExpandEnvelopeSameSubmissionByAccession() throws Exception {
        Team team = MesssageEnvelopeTestHelper.createTeam();
        final Submission submission= MesssageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository,submissionRepository,team);
        final Project savedProject = createAndSaveProject(submission,team);
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = createStudyValidationMessageEnvelope();
        ProjectRef projectRef = new ProjectRef();
        projectRef.setAccession(savedProject.getAccession());
        studyValidationMessageEnvelope.getEntityToValidate().setProjectRef(projectRef);
        studyValidationMessageEnvelopeExpander.expandEnvelope(studyValidationMessageEnvelope,submission.getId());
        assertThat(savedProject,is(studyValidationMessageEnvelope.getProject()));

    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAlias() throws Exception {
        Team team = MesssageEnvelopeTestHelper.createTeam();
        final Submission submission= MesssageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository,submissionRepository,team);
        final Project savedProject = createAndSaveProject(submission,team);
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = createStudyValidationMessageEnvelope();
        ProjectRef projectRef = new ProjectRef();
        projectRef.setAlias(savedProject.getAlias());
        projectRef.setTeam(team.getName());
        studyValidationMessageEnvelope.getEntityToValidate().setProjectRef(projectRef);
        studyValidationMessageEnvelopeExpander.expandEnvelope(studyValidationMessageEnvelope,submission.getId());
        assertThat(savedProject,is(studyValidationMessageEnvelope.getProject()));

    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAccessionDifferentSubmission() throws Exception {
        Team team = MesssageEnvelopeTestHelper.createTeam();
        final Submission submission= MesssageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository,submissionRepository,team);
        final Project savedProject = createAndSaveProject(submission,team);
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = createStudyValidationMessageEnvelope();
        ProjectRef projectRef = new ProjectRef();
        projectRef.setAccession(savedProject.getAccession());
        studyValidationMessageEnvelope.getEntityToValidate().setProjectRef(projectRef);
        studyValidationMessageEnvelopeExpander.expandEnvelope(studyValidationMessageEnvelope,"SUB001");
        assertThat(studyValidationMessageEnvelope.getProject(),is(nullValue()));

    }

    private StudyValidationMessageEnvelope createStudyValidationMessageEnvelope() {
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = new StudyValidationMessageEnvelope();
        uk.ac.ebi.subs.data.submittable.Study submittableStudy = new uk.ac.ebi.subs.data.submittable.Study();
        submittableStudy.setTeam(team);
        submittableStudy.setAccession(UUID.randomUUID().toString());
        submittableStudy.setAlias(UUID.randomUUID().toString());
        studyValidationMessageEnvelope.setEntityToValidate(submittableStudy);
        return studyValidationMessageEnvelope;
    }

    private Project createAndSaveProject (Submission submission, Team team) {
        Project project = new Project();
        project.setTeam(team);
        String projectAccession = UUID.randomUUID().toString();
        String projectAlias = UUID.randomUUID().toString();
        project.setAlias(projectAlias);
        project.setAccession(projectAccession);
        project.setSubmission(submission);
        return projectRepository.save(project);
    }

}