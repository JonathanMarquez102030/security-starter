package com.jonathanmarquez.security.exceptions.prueba;

import com.jonathanmarquez.security.exceptions.customexceptions.ConflictException;
import com.jonathanmarquez.security.exceptions.customexceptions.ForbiddenException;
import com.jonathanmarquez.security.exceptions.customexceptions.ResourceNotFoundException;
import com.jonathanmarquez.security.exceptions.customexceptions.UnauthorizedException;
import com.jonathanmarquez.security.exceptions.ApiResponseFactory;
import com.jonathanmarquez.security.exceptions.response.SuccessApiResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de prueba para verificar las respuestas estandarizadas de la API.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Validated
public class TestResponseController {

    private final ApiResponseFactory responseFactory;

    // ========== SUCCESS RESPONSES ==========

    /**
     * Prueba respuesta exitosa con datos (200 OK)
     */
    @GetMapping("/success")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> testSuccess() {
        Map<String, Object> data = Map.of(
            "id", 1,
            "name", "Usuario de prueba",
            "email", "test@example.com",
            "active", true
        );

        SuccessApiResponse<Map<String, Object>> response = 
            responseFactory.success(data, "Datos obtenidos exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Prueba respuesta de recurso creado (201 Created)
     */
    @PostMapping("/created")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> testCreated() {
        Map<String, Object> createdResource = Map.of(
            "id", 123,
            "name", "Nuevo recurso",
            "createdAt", "2024-01-01T10:30:00"
        );

        SuccessApiResponse<Map<String, Object>> response = 
            responseFactory.created(createdResource, "Recurso creado exitosamente");

        return ResponseEntity.status(201).body(response);
    }

    /**
     * Prueba respuesta sin contenido (204 No Content)
     */
    @DeleteMapping("/no-content")
    public ResponseEntity<SuccessApiResponse<Void>> testNoContent() {
        SuccessApiResponse<Void> response = 
            responseFactory.noContent("Recurso eliminado exitosamente");

        return ResponseEntity.status(204).body(response);
    }

    // ========== ERROR RESPONSES ==========

    /**
     * Prueba validación con @Valid - generará MethodArgumentNotValidException
     */
    @PostMapping("/validation")
    public ResponseEntity<?> testValidation(
            @RequestParam @NotBlank(message = "El nombre es obligatorio") String name,
            @RequestParam @Email(message = "El formato del email es inválido") String email) {
        
        // Si llega aquí, la validación pasó
        Map<String, Object> data = Map.of("name", name, "email", email);
        return ResponseEntity.ok(responseFactory.success(data, "Validación exitosa"));
    }

    /**
     * Prueba recurso no encontrado (404)
     */
    @GetMapping("/not-found")
    public ResponseEntity<?> testNotFound() {
        throw new ResourceNotFoundException("El usuario con ID 999 no fue encontrado");
    }

    /**
     * Prueba no autorizado (401)
     */
    @GetMapping("/unauthorized")
    public ResponseEntity<?> testUnauthorized() {
        throw new UnauthorizedException("Token de acceso inválido o expirado");
    }

    /**
     * Prueba prohibido (403)
     */
    @GetMapping("/forbidden")
    public ResponseEntity<?> testForbidden() {
        throw new ForbiddenException("No tiene permisos para acceder a este recurso");
    }

    /**
     * Prueba conflicto (409)
     */
    @PostMapping("/conflict")
    public ResponseEntity<?> testConflict() {
        throw new ConflictException("El email ya está registrado en el sistema");
    }

    /**
     * Prueba error interno del servidor (500)
     */
    @GetMapping("/internal-error")
    public ResponseEntity<?> testInternalError() {
        throw new RuntimeException("Error simulado en la base de datos");
    }

    /**
     * Prueba bad request personalizado (400)
     */
    @GetMapping("/bad-request")
    public ResponseEntity<?> testBadRequest(@RequestParam(required = false) String param) {
        if (param == null || param.trim().isEmpty()) {
            // Simular un error de negocio que resulta en bad request
            throw new IllegalArgumentException("El parámetro 'param' es requerido");
        }
        
        return ResponseEntity.ok(responseFactory.success(
            Map.of("param", param), 
            "Parámetro recibido correctamente"
        ));
    }

    // ========== RESPONSES CON DATOS COMPLEJOS ==========

    /**
     * Prueba con datos complejos anidados
     */
    @GetMapping("/complex-data")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> testComplexData() {
        Map<String, Object> userProfile = Map.of(
            "user", Map.of(
                "id", 1,
                "username", "johndoe",
                "profile", Map.of(
                    "firstName", "John",
                    "lastName", "Doe",
                    "age", 30
                )
            ),
            "preferences", Map.of(
                "theme", "dark",
                "notifications", true,
                "language", "es"
            ),
            "metadata", Map.of(
                "createdAt", "2024-01-01T00:00:00",
                "lastLogin", "2024-01-15T14:30:00"
            )
        );

        SuccessApiResponse<Map<String, Object>> response = 
            responseFactory.success(userProfile, "Perfil de usuario obtenido exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Prueba con lista de elementos
     */
    @GetMapping("/list-data")
    public ResponseEntity<SuccessApiResponse<Map<String, Object>>> testListData() {
        Map<String, Object> data = Map.of(
            "users", java.util.List.of(
                Map.of("id", 1, "name", "Usuario 1"),
                Map.of("id", 2, "name", "Usuario 2"),
                Map.of("id", 3, "name", "Usuario 3")
            ),
            "pagination", Map.of(
                "page", 1,
                "pageSize", 10,
                "totalItems", 3,
                "totalPages", 1
            )
        );

        SuccessApiResponse<Map<String, Object>> response = 
            responseFactory.success(data, "Lista de usuarios obtenida exitosamente");

        return ResponseEntity.ok(response);
    }
}