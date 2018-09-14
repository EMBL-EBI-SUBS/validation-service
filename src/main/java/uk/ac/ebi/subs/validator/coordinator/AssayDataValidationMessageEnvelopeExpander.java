package uk.ac.ebi.subs.validator.coordinator;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.AssayRef;
import uk.ac.ebi.subs.data.component.ProtocolRef;
import uk.ac.ebi.subs.data.component.ProtocolUse;
import uk.ac.ebi.subs.data.submittable.Assay;
import uk.ac.ebi.subs.data.submittable.Protocol;
import uk.ac.ebi.subs.repository.repos.submittables.AssayRepository;
import uk.ac.ebi.subs.repository.repos.submittables.ProtocolRepository;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.validator.data.AssayDataValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssayDataValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<AssayDataValidationMessageEnvelope> {

    AssayRepository assayRepository;
    SampleRepository sampleRepository;
    ProtocolRepository protocolRepository;

    public AssayDataValidationMessageEnvelopeExpander(AssayRepository assayRepository, SampleRepository sampleRepository, ProtocolRepository protocolRepository) {
        this.assayRepository = assayRepository;
        this.sampleRepository = sampleRepository;
        this.protocolRepository = protocolRepository;
    }

    @Override
    public void expandEnvelope(AssayDataValidationMessageEnvelope assayDataValidationMessageEnvelope) {
        final List<AssayRef> assayRefs = assayDataValidationMessageEnvelope.getEntityToValidate().getAssayRefs();
        final List<Submittable<uk.ac.ebi.subs.data.submittable.Assay>> assays = new ArrayList<>();
        final List<Submittable<Protocol>> protocols = new ArrayList<>();

        for (AssayRef assayRef : assayRefs) {
            uk.ac.ebi.subs.repository.model.Assay assayStoredSubmittable;

            if (assayRef.getAccession() != null && !assayRef.getAccession().isEmpty()) {
                assayStoredSubmittable = assayRepository.findFirstByAccessionOrderByCreatedDateDesc(assayRef.getAccession());
            } else {
                assayStoredSubmittable = assayRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(assayRef.getTeam(), assayRef.getAlias());
            }

            if (assayStoredSubmittable != null) {
                Submittable<uk.ac.ebi.subs.data.submittable.Assay> assaySubmittable = new Submittable<>(assayStoredSubmittable, assayStoredSubmittable.getSubmission().getId());
                assays.add(assaySubmittable);
            }
            expandProtocols(assayStoredSubmittable, protocols);
        }

        assayDataValidationMessageEnvelope.setAssays(assays);
        assayDataValidationMessageEnvelope.setProtocols(protocols);
    }

    private void expandProtocols(Assay assay, List<Submittable<Protocol>> protocols) {
        List<ProtocolUse> protocolUses = assay.getProtocolUses();
        if (protocols != null && !protocolUses.isEmpty()) {
            for (ProtocolUse protocolUse : protocolUses) {
                if (protocolUse != null) {
                    ProtocolRef protocolRef = protocolUse.getProtocolRef();
                    uk.ac.ebi.subs.repository.model.Protocol protocol;
                    if (protocolRef != null && protocolRef.getAccession() != null && !protocolRef.getAccession().isEmpty()) {
                        protocol = protocolRepository.findFirstByAccessionOrderByCreatedDateDesc(protocolRef.getAccession());
                    } else {
                        protocol = protocolRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(protocolRef.getTeam(), protocolRef.getAlias());
                    }
                    if (!addedBefore(protocol, protocols)) {
                        Submittable<uk.ac.ebi.subs.data.submittable.Protocol> protocolSubmittable = new Submittable<>(protocol, protocol.getSubmission().getId());
                        protocols.add(protocolSubmittable);
                    }
                }
            }
        }
    }

    private boolean addedBefore(Protocol protocol, List<Submittable<Protocol>> protocols) {
        for (Submittable<Protocol> protocolSubmittable : protocols) {
            if (protocolSubmittable.getAlias() != null && !protocolSubmittable.getAlias().isEmpty() && protocolSubmittable.getTeam() != null) {
                if (protocol.getAlias() != null && !protocol.getAlias().isEmpty() && protocol.getTeam() != null) {
                    if (protocol.getAlias().equalsIgnoreCase(protocolSubmittable.getAlias()) &&
                            protocol.getTeam().equals(protocolSubmittable.getTeam())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
