package com.cie.hapi_service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.hl7.fhir.r4.model.Bundle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "API for managing Patient resources")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private FhirContext fhirContext;

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieves a Patient resource by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "503", description = "FHIR server connection failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getPatientById(
            @Parameter(description = "Patient ID", required = true, example = "123")
            @PathVariable String id) {
        try {
            logger.info("Fetching patient with ID: {}", id);
            
            // Read the patient by ID from the FHIR server
            Patient patient = fhirClient.read()
                    .resource(Patient.class)
                    .withId(id)
                    .execute();

            logger.info("Successfully retrieved patient: {}", id);

            return ResponseEntity.ok(fhirContext.newJsonParser().encodeResourceToString(patient));
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Patient not found with ID: {}", id);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Patient not found");
            error.put("patientId", id);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            
        } catch (FhirClientConnectionException e) {
            logger.error("Failed to connect to FHIR server: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "FHIR server connection failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
            
        } catch (Exception e) {
            logger.error("Error fetching patient with ID {}: {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("patientId", id);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search patients by name", description = "Searches for Patient resources matching the provided name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - name parameter is required"),
            @ApiResponse(responseCode = "503", description = "FHIR server connection failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> searchPatientsByName(
            @Parameter(description = "Patient name to search for", required = true, example = "John Doe")
            @RequestParam String name) {
        try {
            logger.info("Searching for patients with name: {}", name);
            
            // Search for patients by name
            Bundle bundle = fhirClient.search()
                    .forResource(Patient.class)
                    .where(Patient.NAME.matches().value(name))
                    .returnBundle(Bundle.class)
                    .execute();

            logger.info("Found {} patients matching name: {}", bundle.getTotal(), name);

            // Convert bundle to JSON string
            String jsonResponse = fhirContext.newJsonParser()
                    .setPrettyPrint(true)
                    .encodeResourceToString(bundle);

            return ResponseEntity.ok(jsonResponse);
            
        } catch (FhirClientConnectionException e) {
            logger.error("Failed to connect to FHIR server: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "FHIR server connection failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
            
        } catch (Exception e) {
            logger.error("Error searching for patients with name {}: {}", name, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("name", name);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

