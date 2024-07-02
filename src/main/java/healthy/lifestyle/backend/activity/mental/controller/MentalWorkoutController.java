package healthy.lifestyle.backend.activity.mental.controller;

import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutResponseDto;
import healthy.lifestyle.backend.activity.mental.service.MentalWorkoutService;
import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleOptionalValidation;
import healthy.lifestyle.backend.user.service.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @Operation(summary = "Get default mental workouts")
    @GetMapping("/default_mental_workouts")
    public ResponseEntity<Page<MentalWorkoutResponseDto>> getDefaultMentalWorkouts(
            @RequestParam(required = false) @TitleOptionalValidation(min = 2) String title,
            @RequestParam(required = false) @DescriptionOptionalValidation(min = 2) String description,
            @RequestParam(required = false) Long mentalTypeId,
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Page<MentalWorkoutResponseDto> responseDtoPage = mentalWorkoutService.getMentalWorkoutsWithFilters(
                false, null, title, description, mentalTypeId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(responseDtoPage);
    }

    @Operation(summary = "Get list default and custom mental workouts")
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<MentalWorkoutResponseDto>> getMentalWorkouts(
            @RequestParam(required = false) Boolean isCustom,
            @RequestParam(required = false) @TitleOptionalValidation(min = 2) String title,
            @RequestParam(required = false) @DescriptionOptionalValidation(min = 2) String description,
            @RequestParam(required = false) Long mentalTypeId,
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Long userId = null;
        if (isCustom == null || isCustom)
            userId = authUtil.getUserIdFromAuthentication(
                    SecurityContextHolder.getContext().getAuthentication());
        Page<MentalWorkoutResponseDto> dtoPage = mentalWorkoutService.getMentalWorkoutsWithFilters(
                isCustom, userId, title, description, mentalTypeId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(dtoPage);
    }
}
