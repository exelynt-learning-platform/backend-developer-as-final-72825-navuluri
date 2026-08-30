package com.booking.resource.service;

import com.booking.resource.dto.ReservationRequest;
import com.booking.resource.dto.ReservationResponse;
import com.booking.resource.entity.Reservation;
import com.booking.resource.entity.ReservationStatus;
import com.booking.resource.entity.Resource;
import com.booking.resource.entity.Role;
import com.booking.resource.entity.User;
import com.booking.resource.exception.BadRequestException;
import com.booking.resource.exception.ResourceNotFoundException;
import com.booking.resource.repository.ReservationRepository;
import com.booking.resource.repository.ResourceRepository;
import com.booking.resource.repository.UserRepository;
import com.booking.resource.specification.ReservationSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        User currentUser = getCurrentUser();

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new BadRequestException("Resource is currently not available for booking");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().isEqual(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        BigDecimal calculatedPrice = request.getPrice();
        if (calculatedPrice == null) {
            if (resource.getPricePerHour() != null) {
                long durationMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
                if (durationMinutes <= 0) {
                    calculatedPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                } else {
                    BigDecimal hoursBd = BigDecimal.valueOf(durationMinutes)
                            .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
                    calculatedPrice = resource.getPricePerHour().multiply(hoursBd).setScale(2, RoundingMode.HALF_UP);
                }
            } else {
                calculatedPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Users creating reservations always start with PENDING status
        ReservationStatus initialStatus = ReservationStatus.PENDING;

        Reservation reservation = new Reservation(
                currentUser,
                resource,
                request.getStartTime(),
                request.getEndTime(),
                initialStatus,
                calculatedPrice
        );

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    public Page<ReservationResponse> getReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort
    ) {
        User currentUser = getCurrentUser();
        Long filterUserId = currentUser.getRole() == Role.ROLE_ADMIN ? null : currentUser.getId();

        Pageable pageable = createPageable(page, size, sort);
        Specification<Reservation> spec = ReservationSpecification.filterReservations(filterUserId, status, minPrice, maxPrice);

        return reservationRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public ReservationResponse getReservationById(Long id) {
        User currentUser = getCurrentUser();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (currentUser.getRole() != Role.ROLE_ADMIN && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this reservation");
        }

        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        User currentUser = getCurrentUser();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            // ADMIN can update all fields
            if (request.getResourceId() != null) {
                Resource resource = resourceRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));
                reservation.setResource(resource);
            }
            if (request.getStartTime() != null) {
                reservation.setStartTime(request.getStartTime());
            }
            if (request.getEndTime() != null) {
                reservation.setEndTime(request.getEndTime());
            }
            if (request.getStatus() != null) {
                reservation.setStatus(request.getStatus());
            }
            if (request.getPrice() != null) {
                reservation.setPrice(request.getPrice());
            }
        } else {
            // USER can only cancel their own reservation
            if (!reservation.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to update this reservation");
            }
            if (request.getStatus() != ReservationStatus.CANCELLED) {
                throw new BadRequestException("Users can only cancel their own reservations. Use status: CANCELLED");
            }
            reservation.setStatus(ReservationStatus.CANCELLED);
        }

        Reservation updated = reservationRepository.save(reservation);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteReservation(Long id) {
        User currentUser = getCurrentUser();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Only Administrators can delete reservations");
        }

        reservationRepository.delete(reservation);
    }

    /**
     * Gets the current authenticated user from SecurityContext.
     * Fetches from DB to get full User entity including role.
     * Authentication is guaranteed to be set by JwtAuthenticationFilter before reaching services.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + username));
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();
        Sort.Direction direction = (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getUsername(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getPrice(),
                reservation.getCreatedAt()
        );
    }
}
