package uk.ac.ebi.subs.validator.coordinator.messages;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class StoredSubmittableDeleteMessage {
    @NonNull
    private String submissionId;
}
