package com.cie.hapi_service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documentreferences")
@Tag(name = "Document References", description = "API for managing DocumentReference resources")
public class DocumentReferenceController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentReferenceController.class);

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private FhirContext fhirContext;

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get document references by patient ID", description = "Retrieves all DocumentReference resources associated with a specific patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document references found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Document references not found for the patient"),
            @ApiResponse(responseCode = "503", description = "FHIR server connection failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getDocumentReferencesByPatient(
            @Parameter(description = "Patient ID", required = true, example = "123")
            @PathVariable String patientId) {
        try {
            logger.info("Fetching document references for patient ID: {}", patientId);
            
            // Search for document references by patient reference
            Bundle bundle = fhirClient.search()
                    .forResource(DocumentReference.class)
                    .where(DocumentReference.SUBJECT.hasId(patientId))
                    .returnBundle(Bundle.class)
                    .execute();

            logger.info("Found {} document references for patient: {}", bundle.getTotal(), patientId);

            // Convert bundle to JSON string
            String jsonResponse = fhirContext.newJsonParser()
                    .setPrettyPrint(true)
                    .encodeResourceToString(bundle);

            return ResponseEntity.ok(jsonResponse);
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Document references not found for patient ID: {}", patientId);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Document references not found");
            error.put("patientId", patientId);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            
        } catch (FhirClientConnectionException e) {
            logger.error("Failed to connect to FHIR server: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "FHIR server connection failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
            
        } catch (Exception e) {
            logger.error("Error fetching document references for patient ID {}: {}", patientId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("patientId", patientId);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

