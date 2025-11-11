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
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private FhirContext fhirContext;

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable String id) {
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
}

