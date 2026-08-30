package com.booking.resource.controller;

import com.booking.resource.dto.ReservationRequest;
import com.booking.resource.dto.ReservationResponse;
import com.booking.resource.entity.ReservationStatus;
import com.booking.resource.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Endpoints for managing resource bookings/reservations")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation (User identity automatically extracted from JWT)")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse created = reservationService.createReservation(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get reservations with filtering, pagination, and sorting (ADMIN views all; USER views own)")
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            @Parameter(description = "Filter by status (PENDING, CONFIRMED, CANCELLED)") @RequestParam(required = false) ReservationStatus status,
            @Parameter(description = "Filter by minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Filter by maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Zero-based page index (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria e.g. createdAt,desc or price,asc") @RequestParam(required = false) String sort
    ) {
        Page<ReservationResponse> reservations = reservationService.getReservations(status, minPrice, maxPrice, page, size, sort);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation details by ID")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reservation (ADMIN full access; USER can update/cancel own reservation)")
    public ResponseEntity<ReservationResponse> updateReservation(@PathVariable Long id, @RequestBody ReservationRequest request) {
        ReservationResponse updated = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reservation (ADMIN only)")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
