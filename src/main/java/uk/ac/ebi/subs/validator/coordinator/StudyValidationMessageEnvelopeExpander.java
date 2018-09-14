package uk.ac.ebi.subs.validator.coordinator;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.data.component.ProtocolRef;
import uk.ac.ebi.subs.data.submittable.Protocol;
import uk.ac.ebi.subs.repository.model.Project;

import uk.ac.ebi.subs.repository.repos.submittables.ProjectRepository;
import uk.ac.ebi.subs.repository.repos.submittables.ProtocolRepository;
import uk.ac.ebi.subs.validator.data.StudyValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<StudyValidationMessageEnvelope> {

    ProjectRepository projectRepository;
    ProtocolRepository protocolRepository;

    public StudyValidationMessageEnvelopeExpander(ProjectRepository projectRepository, ProtocolRepository protocolRepository) {
        this.projectRepository = projectRepository;
        this.protocolRepository = protocolRepository;
    }

    @Override
    void expandEnvelope(StudyValidationMessageEnvelope validationMessageEnvelope) {
        final ProjectRef projectRef = validationMessageEnvelope.getEntityToValidate().getProjectRef();

        Project project;

        if (projectRef != null && projectRef.getAccession() != null && !projectRef.getAccession().isEmpty()) {
            project = projectRepository.findFirstByAccessionOrderByCreatedDateDesc(projectRef.getAccession());
        } else {

            project = projectRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(projectRef.getTeam(), projectRef.getAlias());
        }

        if (project != null) {
            Submittable<uk.ac.ebi.subs.data.submittable.Project> projectSubmittable = new Submittable<>(project, project.getSubmission().getId());
            validationMessageEnvelope.setProject(projectSubmittable);
        }

        List<ProtocolRef> protocolRefs = validationMessageEnvelope.getEntityToValidate().getProtocolRefs();
        List<Submittable<Protocol>> protocols = new ArrayList<>();

        for(ProtocolRef protocolRef :protocolRefs){
            Protocol protocol;
            if (protocolRef != null && protocolRef.getAccession() != null && !protocolRef.getAccession().isEmpty()) {
                protocol = protocolRepository.findFirstByAccessionOrderByCreatedDateDesc(protocolRef.getAccession());
            } else {
                protocol = protocolRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(protocolRef.getTeam(), protocolRef.getAlias());
            }
            protocols.add(new Submittable<>(protocol, project == null ? null : project.getSubmission().getId()));
        }
        validationMessageEnvelope.setProtocols(protocols);

    }
}
