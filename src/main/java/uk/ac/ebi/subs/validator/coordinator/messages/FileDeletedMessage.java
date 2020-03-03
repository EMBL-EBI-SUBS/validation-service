package uk.ac.ebi.subs.validator.coordinator.messages;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileDeletedMessage {

    private String submissionId;
}
