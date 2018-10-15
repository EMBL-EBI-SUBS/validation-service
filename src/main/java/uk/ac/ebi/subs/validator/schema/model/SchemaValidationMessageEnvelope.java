package uk.ac.ebi.subs.validator.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
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
                ((ObjectNode) this.entityToValidate).has("id") &&
                ((ObjectNode) this.entityToValidate).get("id").isTextual()
                ) {
            return ((ObjectNode) entityToValidate).get("id").textValue();
        }
        return null;

    }
}
