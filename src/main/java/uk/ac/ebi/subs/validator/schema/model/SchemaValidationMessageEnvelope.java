package uk.ac.ebi.subs.validator.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SchemaValidationMessageEnvelope {

    private String validationResultUUID;
    private int validationResultVersion;

    private String submissionId;
    private String dataTypeId;
    private String checklistId;


    private JsonNode entityToValidate;

    public String entityId() {
        if (this.entityToValidate != null &&
                this.entityToValidate.isObject() &&
                this.entityToValidate.has("id") &&
                this.entityToValidate.get("id").isTextual()
                ) {
            return entityToValidate.get("id").textValue();
        }
        return null;

    }
}
