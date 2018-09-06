package uk.ac.ebi.subs.validator.coordinator;

import uk.ac.ebi.subs.repository.model.Sample;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SampleAssertionHelper {

    public static void assertSampleList(List<Sample> savedSampleList,
                                  List<uk.ac.ebi.subs.data.submittable.Sample> sampleListFromEnvelope) {
        for (Sample sample : savedSampleList) {
            uk.ac.ebi.subs.data.submittable.Sample sampleFromEnvelope =
                    getSampleFromEnvelope(sampleListFromEnvelope, sample.getId());
            assertThat(sample.getId(), is(sampleFromEnvelope.getId()));
            assertThat(sample.getAlias(), is(sampleFromEnvelope.getAlias()));
            assertThat(sample.getTeam(), is(sampleFromEnvelope.getTeam()));
            assertThat(sample.getAccession(), is(sampleFromEnvelope.getAccession()));
            assertThat(sample.getTaxonId(), is(sampleFromEnvelope.getTaxonId()));
            assertThat(sample.getReleaseDate(), is(sampleFromEnvelope.getReleaseDate()));
        }
    }

    private static uk.ac.ebi.subs.data.submittable.Sample getSampleFromEnvelope(
            List<uk.ac.ebi.subs.data.submittable.Sample> sampleListFromEnvelope, String sampleId) {
        for (uk.ac.ebi.subs.data.submittable.Sample sampleFromEnvelope : sampleListFromEnvelope) {
            if (sampleFromEnvelope.getId().equals(sampleId)) {
                return sampleFromEnvelope;
            }
        }

        return null;
    }

}
