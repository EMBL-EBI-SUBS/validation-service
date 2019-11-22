package uk.ac.ebi.subs.validator.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.repository.model.Sample;
import uk.ac.ebi.subs.repository.repos.submittables.SampleRepository;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.model.Submittable;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class SampleValidationMessageEnvelopeExpander extends ValidationMessageEnvelopeExpander<SampleValidationMessageEnvelope> {
    private SampleRepository sampleRepository;
    private RestTemplate restTemplate;

    @Value("${biosamples.client.uri}")
    private String sampleStorageURI;

    public SampleValidationMessageEnvelopeExpander(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    void expandEnvelope(SampleValidationMessageEnvelope validationMessageEnvelope) {
        final List<SampleRelationship> sampleRelationships = validationMessageEnvelope.getEntityToValidate().getSampleRelationships();

        for (SampleRelationship sampleRelationship : sampleRelationships) {

            Sample sample;

            if (sampleRelationship.getAccession() != null && !sampleRelationship.getAccession().isEmpty()) {
                sample = findSampleByAccession(sampleRelationship.getAccession());
            } else {
                sample = sampleRepository.findFirstByTeamNameAndAliasOrderByCreatedDateDesc(sampleRelationship.getTeam(), sampleRelationship.getAlias());
            }

            if (canAddSubmittable(validationMessageEnvelope, sample)) {
                Submittable<uk.ac.ebi.subs.data.submittable.Sample> sampleSubmittable = new Submittable<>(sample, sample.getSubmission().getId());
                validationMessageEnvelope.getSampleList().add(sampleSubmittable);
            }
        }
    }

    private Sample findSampleByAccession(String accessionID) {
        Sample sample = sampleRepository.findByAccession(accessionID);

        if (sample == null) {
            try {
                final ResponseEntity<String> response =
                        restTemplate.getForEntity(sampleStorageURI + "samples/" + accessionID, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    ObjectMapper mapper = new ObjectMapper();
                    sample = mapper.readValue(response.getBody(), Sample.class);
                }
            } catch (IOException e) {
                log.error("Processing sample search from Sample storage repository has failed with sample ID: {}", accessionID);
            } catch (RestClientException rce) {
                log.error("Getting sample information from Sample storage repository resulted with an error.");
                log.error("Error message: " + rce.getMessage());
            }

        }

        return sample;
    }
}