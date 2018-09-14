# Schema Validator
This package contains the schema validator. This validator works as a middle layer between a Node.js® server that is the 
actual validation executor and the Submissions validation infrastructure. For more details on the JSON Schema 
Validator see its GitHub repo [here](https://github.com/EMBL-EBI-SUBS/json-schema-validator).

## Model
The model contains the java representation of the *json-schema-validator* server error - `JsonSchemaValidationError` - 
and the java representation of the request body to be sent to the validation server - `JsonSchemaValidationRequestBody`.

## Exceptions
The schema validator throws specific exceptions for unexpected errors:

- `JsonSchemaValidatorException` for any error when reaching the *json-schema-validator* server.

## Schemas
The schema validator does not hold the schemas used for the validation, these come from the DataType and Checklist objects