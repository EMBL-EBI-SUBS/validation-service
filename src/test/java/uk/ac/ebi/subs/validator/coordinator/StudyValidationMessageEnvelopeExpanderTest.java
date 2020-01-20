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
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.repository.model.Project;
import uk.ac.ebi.subs.repository.model.Protocol;
import uk.ac.ebi.subs.repository.model.Submission;
import uk.ac.ebi.subs.repository.repos.SubmissionRepository;
import uk.ac.ebi.subs.repository.repos.status.SubmissionStatusRepository;
import uk.ac.ebi.subs.repository.repos.submittables.ProjectRepository;
import uk.ac.ebi.subs.repository.repos.submittables.ProtocolRepository;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = {"uk.ac.ebi.subs.repository.repos", "uk.ac.ebi.subs.validator.repository"})
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
    ProtocolRepository protocolRepository;

    @Autowired
    StudyValidationMessageEnvelopeExpander studyValidationMessageEnvelopeExpander;

    private Team team;
    private Submission submission;
    private Project savedProject;
    private List<Protocol> savedProtocols;

    @Before
    public void setup() {
        team = MessageEnvelopeTestHelper.createTeam();
        submission= MessageEnvelopeTestHelper.saveNewSubmission(submissionStatusRepository,submissionRepository,team);
        savedProject = createAndSaveProject(submission,team);
        savedProtocols = createAndSaveProtocols(team);
    }

    @After
    public void finish() {
        projectRepository.delete(savedProject);
        submissionRepository.delete(submission);
        submissionStatusRepository.delete(submission.getSubmissionStatus());
        protocolRepository.deleteAll(savedProtocols);
    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAccession() throws Exception {
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = createStudyValidationMessageEnvelope(submission.getId());
        ProjectRef projectRef = new ProjectRef();
        projectRef.setAccession(savedProject.getAccession());
        studyValidationMessageEnvelope.getEntityToValidate().setProjectRef(projectRef);
        studyValidationMessageEnvelopeExpander.expandEnvelope(studyValidationMessageEnvelope);
        assertThat(savedProject,is(studyValidationMessageEnvelope.getProject().getBaseSubmittable()));

    }

    @Test
    public void testExpandEnvelopeSameSubmissionByAlias() throws Exception {
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = createStudyValidationMessageEnvelope(submission.getId());
        ProjectRef projectRef = new ProjectRef();
        projectRef.setAlias(savedProject.getAlias());
        projectRef.setTeam(team.getName());
        studyValidationMessageEnvelope.getEntityToValidate().setProjectRef(projectRef);
        studyValidationMessageEnvelopeExpander.expandEnvelope(studyValidationMessageEnvelope);
        assertThat(savedProject,is(studyValidationMessageEnvelope.getProject().getBaseSubmittable()));

    }

    @Test
    public void testExpandEnvelopeWithProtocols() throws Exception {
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = createStudyValidationMessageEnvelope(submission.getId());
        studyValidationMessageEnvelope.getEntityToValidate().setProtocolRefs(MessageEnvelopeTestHelper.createProtocolRefs(savedProtocols));
        studyValidationMessageEnvelopeExpander.expandEnvelope(studyValidationMessageEnvelope);
        assertEquals(savedProtocols.size(),studyValidationMessageEnvelope.getProtocols().size());
        assertThat(savedProtocols.get(0).getAlias(),is(studyValidationMessageEnvelope.getProtocols().get(0).getAlias()));
    }

    private StudyValidationMessageEnvelope createStudyValidationMessageEnvelope(String submissionId) {
        StudyValidationMessageEnvelope studyValidationMessageEnvelope = new StudyValidationMessageEnvelope();
        uk.ac.ebi.subs.data.submittable.Study submittableStudy = new uk.ac.ebi.subs.data.submittable.Study();
        submittableStudy.setTeam(team);
        submittableStudy.setAccession(UUID.randomUUID().toString());
        submittableStudy.setAlias(UUID.randomUUID().toString());
        studyValidationMessageEnvelope.setEntityToValidate(submittableStudy);
        studyValidationMessageEnvelope.setSubmissionId(submissionId);
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

    private List<Protocol> createAndSaveProtocols(Team team) {
        List<Protocol> protocols = MessageEnvelopeTestHelper.createProtocols(submission, team, 3);
        return protocolRepository.saveAll(protocols);
    }
}