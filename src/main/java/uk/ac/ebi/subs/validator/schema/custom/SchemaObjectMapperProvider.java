package uk.ac.ebi.subs.validator.schema.custom;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDate;

public class SchemaObjectMapperProvider {

    public static ObjectMapper createCustomObjectMapper () {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY); // Null fields and empty collections are not included in the serialization.
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateCustomSerializer());
        mapper.registerModule(module);
        return mapper;
    }

}
