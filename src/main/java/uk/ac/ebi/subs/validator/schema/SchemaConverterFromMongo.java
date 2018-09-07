package uk.ac.ebi.subs.validator.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Our current version of Mongo cannot store documents with keys that start with '$' or contains a '.'.
 * JSON schema must contain a '$schema' key and there are keys which has a '.'.
 * <p>
 * We re-write the schema, to convert placeholders to the real characters.
 */
class SchemaConverterFromMongo {

    private static ObjectMapper mapper = new ObjectMapper();
    private static SimpleModule module = new SimpleModule();


    private static Map<String, String> placeholders;

    static {
        mapper.registerModule(module);

        placeholders = new HashMap<>();
        placeholders.put("#dollar#", "$");
        placeholders.put("#dot#", ".");
    }

    static JsonNode fixStoredJson(String storedSchemaJson) {
        try {
            ObjectNode schemaAsNode = mapper.readValue(storedSchemaJson, ObjectNode.class);

            return convertJsonNode(schemaAsNode);

        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("The JSON conversion failed. The cause is: %s", e.getMessage()), e.getCause());
        }
    }

    static JsonNode convertJsonNode(JsonNode jsonNode) {
        try {
            if (jsonNode.isArray()){
                fixArrayNode(jsonNode);
            }
            if (jsonNode.isObject()) {
                convertObjectNode((ObjectNode) jsonNode);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("The JSON conversion failed. The cause is: %s", e.getMessage()), e.getCause());
        }

        return jsonNode;
    }

    private static void convertObjectNode(ObjectNode objectNode) throws IOException {
        Iterator<Map.Entry<String, JsonNode>> fieldEntriesIterator = objectNode.fields();
        List<Map.Entry<String, JsonNode>> fieldEntries = new ArrayList<>();
        fieldEntriesIterator.forEachRemaining(fieldEntries::add);

        for (Map.Entry<String, JsonNode> fieldEntry : fieldEntries) {
            String fieldName = fieldEntry.getKey();

            final JsonNode fieldEntryValue = fieldEntry.getValue();

            fixJsonNodeIfApplicable(fieldEntryValue);

            final String fixedFieldName = checkAndReplaceFieldName(fieldName);
            if (!fieldName.equals(fixedFieldName)) {
                JsonNode value = objectNode.remove(fieldName);
                objectNode.set(fixedFieldName, value);
            }
        }
    }

    private static void fixJsonNodeIfApplicable(JsonNode fieldEntryValue) throws IOException {
        if ((fieldEntryValue instanceof ObjectNode) || (fieldEntryValue instanceof ArrayNode)) {
            convertJsonNode(fieldEntryValue);
        }
    }

    private static void fixArrayNode(JsonNode arrayNode) throws IOException {
        for (JsonNode valueNode : arrayNode){
            fixJsonNodeIfApplicable(valueNode);
        }
    }

    private static String checkAndReplaceFieldName(String fieldName) {
        String fixedFieldName = fieldName;

        for (String placeholder : placeholders.keySet()) {
            if (fixedFieldName.contains(placeholder)) {
                fixedFieldName = fixedFieldName.replace(placeholder, placeholders.get(placeholder));
            }
        }

        return fixedFieldName;
    }
}
