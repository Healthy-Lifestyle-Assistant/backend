package healthy.lifestyle.backend.activity.mental.controller;

import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutResponseDto;
import healthy.lifestyle.backend.activity.mental.service.MentalWorkoutService;
import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.user.service.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/mental_workouts")
public class MentalWorkoutController {

    @Autowired
    MentalWorkoutService mentalWorkoutService;

    @Autowired
    AuthUtil authUtil;

    @Operation(summary = "Create custom mental workout")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalWorkoutResponseDto> createCustomMentalWorkout(
            @Valid @RequestBody MentalWorkoutCreateRequestDto requestDto) {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalWorkoutResponseDto responseDto = mentalWorkoutService.createCustomMentalWorkout(userId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get default mental workout by id")
    @GetMapping("/default/{mental_workout_id}")
    public ResponseEntity<MentalWorkoutResponseDto> getDefaultMentalWorkoutById(
            @PathVariable("mental_workout_id") @IdValidation long mentalWorkoutId) {
        MentalWorkoutResponseDto responseDto = mentalWorkoutService.getMentalWorkoutById(mentalWorkoutId, true, null);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get custom mental workout by id")
    @GetMapping("/{mental_workout_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalWorkoutResponseDto> getCustomMentalWorkoutById(
            @PathVariable("mental_workout_id") @IdValidation long mentalWorkoutId) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalWorkoutResponseDto responseDto =
                mentalWorkoutService.getMentalWorkoutById(mentalWorkoutId, false, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Delete custom mental workout by id")
    @DeleteMapping("/{mental_workout_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteCustomMentalWorkout(
            @PathVariable("mentalWorkoutId") @IdValidation long mentalWorkoutId) {
        Long authenticatedUserId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        mentalWorkoutService.deleteCustomMentalWorkout(authenticatedUserId, mentalWorkoutId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
