package healthy.lifestyle.backend.admin.users.service;

import healthy.lifestyle.backend.admin.users.repository.UserAdminRepository;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import healthy.lifestyle.backend.users.repository.RoleRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserAdminServiceImpl implements UserAdminService {

    private final UserAdminRepository userAdminRepository;

    private final RoleRepository roleRepository;

    private final CountryRepository countryRepository;

    private final ModelMapper modelMapper;

    public UserAdminServiceImpl(
            UserAdminRepository userAdminRepository,
            RoleRepository roleRepository,
            CountryRepository countryRepository,
            ModelMapper modelMapper) {
        this.userAdminRepository = userAdminRepository;
        this.roleRepository = roleRepository;
        this.countryRepository = countryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<UserResponseDto> getUsersByFilters(
            Long roleId, String username, String email, String fullName, Long countryId, Integer age) {
        Optional<Role> role = roleId != null ? roleRepository.findById(roleId) : Optional.empty();
        Optional<Country> country = countryId != null ? countryRepository.findById(countryId) : Optional.empty();
        List<User> users = userAdminRepository
                .findByFilters(role.orElse(null), username, email, fullName, country.orElse(null), age)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, null, HttpStatus.NOT_FOUND));

        return users.stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .sorted(Comparator.comparing(UserResponseDto::getId))
                .toList();
    }
}
