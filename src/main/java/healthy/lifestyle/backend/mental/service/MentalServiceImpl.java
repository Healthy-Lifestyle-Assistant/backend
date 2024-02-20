package healthy.lifestyle.backend.mental.service;

import healthy.lifestyle.backend.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.mental.model.Mental;
import healthy.lifestyle.backend.mental.model.MentalType;
import healthy.lifestyle.backend.mental.repository.MentalRepository;
import healthy.lifestyle.backend.mental.repository.MentalTypeRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
public class MentalServiceImpl implements MentalService {
    @Autowired
    MentalRepository mentalRepository;

    @Autowired
    HttpRefRepository httpRefRepository;

    @Autowired
    UserService userService;

    @Autowired
    MentalTypeRepository mentalTypeRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public MentalResponseDto getMentalById(long mentalId, boolean requiredDefault, Long userId) {
        Mental mental = mentalRepository
                .findById(mentalId)
                .orElseThrow(() -> new ApiException(ErrorMessage.MENTAL_NOT_FOUND, mentalId, HttpStatus.NOT_FOUND));

        if (mental.isCustom() && requiredDefault)
            throw new ApiException(
                    ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        if (!mental.isCustom() && !requiredDefault)
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        if (userId != null) {
            User user = userService.getUserById(userId);
            if (mental.isCustom()
                    && (user.getMentals() == null || !user.getMentals().contains(mental)))
                throw new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, mentalId, HttpStatus.BAD_REQUEST);
        }

        MentalResponseDto mentalResponseDto = modelMapper.map(mental, MentalResponseDto.class);

        List<HttpRefResponseDto> httpRefsSorted = mentalResponseDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();
        mentalResponseDto.setHttpRefs(httpRefsSorted);
        return mentalResponseDto;
    }

    @Override
    @Transactional
    public Page<MentalResponseDto> getMentalWithFilter(
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

        Page<Mental> entitiesPage = null;

        Optional<MentalType> mentalType =
                mentalTypeId != null ? mentalTypeRepository.findById(mentalTypeId) : Optional.empty();

        // Default and custom
        if (isCustom == null && userId != null) {
            entitiesPage = mentalRepository.findDefaultAndCustomWithFilter(
                    userId, title, description, mentalType.orElse(null), pageable);
        }
        // Default only
        else if (isCustom != null && !isCustom && userId == null) {
            entitiesPage = mentalRepository.findDefaultOrCustomWithFilter(
                    false, null, title, description, mentalType.orElse(null), pageable);
        }
        // Custom only
        else if (isCustom != null && isCustom && userId != null) {
            entitiesPage = mentalRepository.findDefaultOrCustomWithFilter(
                    true, userId, title, description, mentalType.orElse(null), pageable);
        } else {
            throw new ApiExceptionCustomMessage("Invalid args combination", HttpStatus.BAD_REQUEST);
        }

        Page<MentalResponseDto> dtoPage = entitiesPage.map(entity -> modelMapper.map(entity, MentalResponseDto.class));
        return dtoPage;
    }
}