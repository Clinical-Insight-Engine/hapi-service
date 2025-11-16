package com.cie.hapi_service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/observations")
@Tag(name = "Observations", description = "API for managing Observation resources")
public class ObservationController {

    private static final Logger logger = LoggerFactory.getLogger(ObservationController.class);

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private FhirContext fhirContext;

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get observations by patient ID", description = "Retrieves all Observation resources associated with a specific patient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Observations found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Observations not found for the patient"),
            @ApiResponse(responseCode = "503", description = "FHIR server connection failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getObservationsByPatient(
            @Parameter(description = "Patient ID", required = true, example = "123")
            @PathVariable String patientId) {
        try {
            logger.info("Fetching observations for patient ID: {}", patientId);
            
            // Search for observations by patient reference
            Bundle bundle = fhirClient.search()
                    .forResource(Observation.class)
                    .where(Observation.SUBJECT.hasId(patientId))
                    .returnBundle(Bundle.class)
                    .execute();

            // Extract observations from the bundle
            List<Observation> observations = bundle.getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .filter(Observation.class::isInstance)
                    .map(Observation.class::cast)
                    .collect(Collectors.toList());

            logger.info("Found {} observations for patient: {}", observations.size(), patientId);

            // Convert observations to JSON string
            String jsonResponse = fhirContext.newJsonParser()
                    .setPrettyPrint(true)
                    .encodeResourceToString(bundle);

            return ResponseEntity.ok(jsonResponse);
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Observations not found for patient ID: {}", patientId);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Observations not found");
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
            logger.error("Error fetching observations for patient ID {}: {}", patientId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("patientId", patientId);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

