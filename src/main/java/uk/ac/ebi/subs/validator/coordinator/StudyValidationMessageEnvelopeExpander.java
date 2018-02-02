package uk.ac.ebi.subs.validator.coordinator;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.data.submittable.Project;
import uk.ac.ebi.subs.repository.repos.submittables.ProjectRepository;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

@Service
public class StudyValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<StudyValidationMessageEnvelope> {
    ProjectRepository projectRepository;

    public StudyValidationMessageEnvelopeExpander(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    void expandEnvelope(StudyValidationMessageEnvelope validationMessageEnvelope, String submissionId) {
        final ProjectRef projectRef = validationMessageEnvelope.getEntityToValidate().getProjectRef();

        Project project;

        if (projectRef != null && projectRef.getAccession() != null && !projectRef.getAccession().isEmpty()) {
            project = projectRepository.findFirstByAccessionOrderByCreatedDateDesc(projectRef.getAccession());
        } else {
            project = projectRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(projectRef.getTeam(), projectRef.getAlias());
        }

        Submittable<Project> projectSubmittable = new Submittable<>(project,submissionId);

        validationMessageEnvelope.setProject(projectSubmittable);

    }
}
