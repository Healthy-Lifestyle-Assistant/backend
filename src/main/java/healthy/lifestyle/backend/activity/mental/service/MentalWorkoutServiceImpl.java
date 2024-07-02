package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalActivityResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutResponseDto;
import healthy.lifestyle.backend.activity.mental.model.MentalActivity;
import healthy.lifestyle.backend.activity.mental.model.MentalWorkout;
import healthy.lifestyle.backend.activity.mental.repository.MentalActivityRepository;
import healthy.lifestyle.backend.activity.mental.repository.MentalWorkoutRepository;
import healthy.lifestyle.backend.activity.workout.dto.*;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.VerificationUtil;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentalWorkoutServiceImpl implements MentalWorkoutService {
    @Autowired
    MentalWorkoutRepository mentalWorkoutRepository;

    @Autowired
    MentalActivityRepository mentalActivityRepository;

    @Autowired
    UserService userService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    VerificationUtil verificationUtil;

    @Override
    @Transactional
    public MentalWorkoutResponseDto createCustomMentalWorkout(long userId, MentalWorkoutCreateRequestDto requestDto) {
        List<MentalWorkout> mentalWorkoutsWithSameTitle =
                mentalWorkoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), userId);

        if (!mentalWorkoutsWithSameTitle.isEmpty()) {
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        }

        User user = userService.getUserById(userId);
        Set<MentalActivity> mentalActivitySet = new HashSet<>();

        for (long mentalActivityId : requestDto.getMentalActivityIds()) {
            MentalActivity mentalActivity = mentalActivityRepository
                    .findById(mentalActivityId)
                    .orElseThrow(() ->
                            new ApiException(ErrorMessage.MENTAL_NOT_FOUND, mentalActivityId, HttpStatus.NOT_FOUND));

            if (mentalActivity.isCustom() && !mentalActivity.getUser().equals(user))
                throw new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, mentalActivityId, HttpStatus.BAD_REQUEST);

            mentalActivitySet.add(mentalActivity);
        }

        MentalWorkout mentalWorkout = MentalWorkout.builder()
                .isCustom(true)
                .user(user)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .mentalActivities(mentalActivitySet)
                .build();
        MentalWorkout savedMentalWorkout = mentalWorkoutRepository.save(mentalWorkout);
        userService.addMentalWorkoutToUser(user, savedMentalWorkout);

        MentalWorkoutResponseDto mentalWorkoutResponseDto =
                modelMapper.map(savedMentalWorkout, MentalWorkoutResponseDto.class);

        List<MentalActivityResponseDto> mentalActivityResponseDtoList =
                savedMentalWorkout.getMentalActivitiesSortedById().stream()
                        .map(mentalActivity -> modelMapper.map(mentalActivity, MentalActivityResponseDto.class))
                        .peek(elt -> {
                            List<HttpRefResponseDto> httpRefsSorted = elt.getHttpRefs().stream()
                                    .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                                    .toList();
                            elt.setHttpRefs(httpRefsSorted);
                        })
                        .toList();

        mentalWorkoutResponseDto.setMentalActivities(mentalActivityResponseDtoList);
        return mentalWorkoutResponseDto;
    }

    @Override
    @Transactional
    public MentalWorkoutResponseDto getMentalWorkoutById(long mentalWorkoutId, boolean requiredDefault, Long userId) {

        MentalWorkout mentalWorkout = mentalWorkoutRepository
                .findById(mentalWorkoutId)
                .orElseThrow(() ->
                        new ApiException(ErrorMessage.MENTAL_WORKOUT_NOT_FOUND, mentalWorkoutId, HttpStatus.NOT_FOUND));

        if (mentalWorkout.isCustom() && requiredDefault)
            throw new ApiException(
                    ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        if (!mentalWorkout.isCustom() && !requiredDefault)
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        if (userId != null) {
            User user = userService.getUserById(userId);
            if (mentalWorkout.isCustom()
                    && (user.getMentalWorkouts() == null
                            || !user.getMentalWorkouts().contains(mentalWorkout)))
                throw new ApiException(
                        ErrorMessage.USER_MENTAL_WORKOUT_MISMATCH, mentalWorkoutId, HttpStatus.BAD_REQUEST);
        }

        MentalWorkoutResponseDto mentalWorkoutDto = modelMapper.map(mentalWorkout, MentalWorkoutResponseDto.class);

        List<MentalActivityResponseDto> mentalActivitiesSorted = mentalWorkoutDto.getMentalActivities().stream()
                .sorted(Comparator.comparingLong(MentalActivityResponseDto::getId))
                .toList();

        mentalWorkoutDto.setMentalActivities(mentalActivitiesSorted);
        return mentalWorkoutDto;
    }

    @Override
    @Transactional
    public Page<MentalWorkoutResponseDto> getMentalWorkoutsWithFilters(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            Long mentalTypeId,
            String sortField,
            String sortDirection,
            int currentPageNumber,
            int pageSize) {

        Pageable pageable = PageRequest.of(
                currentPageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        Page<MentalWorkout> entitiesPage = null;

        // Default and custom
        if (isCustom == null && userId != null) {
            entitiesPage = mentalWorkoutRepository.findDefaultAndCustomMentalWorkoutsWithFilter(
                    userId, title, description, mentalTypeId, pageable);
        }

        // Default only
        else if (isCustom != null && !isCustom && userId == null) {
            entitiesPage = mentalWorkoutRepository.findDefaultOrCustomMentalWorkoutsWithFilter(
                    false, null, title, description, mentalTypeId, pageable);
        }

        // Custom only
        else if (isCustom != null && isCustom && userId != null) {
            entitiesPage = mentalWorkoutRepository.findDefaultOrCustomMentalWorkoutsWithFilter(
                    true, userId, title, description, mentalTypeId, pageable);
        } else {
            throw new ApiExceptionCustomMessage("Invalid args combination", HttpStatus.BAD_REQUEST);
        }

        Page<MentalWorkoutResponseDto> dtoPage =
                entitiesPage.map(entity -> modelMapper.map(entity, MentalWorkoutResponseDto.class));
        return dtoPage;
    }
}
