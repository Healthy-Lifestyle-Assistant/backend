package healthy.lifestyle.backend.activity.mental.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutResponseDto;
import healthy.lifestyle.backend.activity.mental.model.MentalActivity;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.mental.model.MentalWorkout;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.testconfig.BeanConfig;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.URL;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.model.User;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(BeanConfig.class)
public class MentalWorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DbUtil dbUtil;

    @Autowired
    DtoUtil dtoUtil;

    @MockBean
    FirebaseMessaging firebaseMessaging;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse(ContainerConfig.POSTGRES));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMentalWorkout_shouldReturnCreatedDtoWith201_whenValidFields() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user);
        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef), mentalType1);
        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(2, List.of(defaultHttpRef, customHttpRef), mentalType1, user);

        MentalWorkoutCreateRequestDto mentalWorkoutCreateRequestDto =
                dtoUtil.mentalWorkoutCreateRequestDto(1, List.of(defaultMental1.getId(), customMental1.getId()));

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_MENTAL_WORKOUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mentalWorkoutCreateRequestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(mentalWorkoutCreateRequestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(mentalWorkoutCreateRequestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalWorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalWorkoutResponseDto>() {});

        // Db
        MentalWorkout createdMentalWorkout = dbUtil.getMentalWorkoutById(responseDto.getId());
        assertEquals(user.getId(), createdMentalWorkout.getUser().getId());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMentalWorkout_shouldReturnErrorMessageWith400_whenAlreadyExistsWithSameTitle() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user);
        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef), mentalType1);
        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(2, List.of(defaultHttpRef, customHttpRef), mentalType1, user);

        MentalWorkout customMentalWorkout =
                dbUtil.createCustomMentalWorkout(1, List.of(defaultMental1, customMental1), user);

        MentalWorkoutCreateRequestDto requestDto =
                dtoUtil.mentalWorkoutCreateRequestDto(1, List.of(defaultMental1.getId(), customMental1.getId()));
        requestDto.setTitle(customMentalWorkout.getTitle());

        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_MENTAL_WORKOUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMentalWorkout_shouldReturnErrorMessageWith404_whenMentalActivityNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        long nonExistentMentalActivityId = 1000L;
        MentalWorkoutCreateRequestDto requestDto =
                dtoUtil.mentalWorkoutCreateRequestDto(1, List.of(nonExistentMentalActivityId));
        ApiException expectedException =
                new ApiException(ErrorMessage.MENTAL_NOT_FOUND, nonExistentMentalActivityId, HttpStatus.NOT_FOUND);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_MENTAL_WORKOUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMentalWorkout_shouldReturnErrorMessageWith400_whenMentalActivityUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user2);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef), mentalType1);
        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(2, List.of(defaultHttpRef, customHttpRef), mentalType1, user2);

        MentalWorkoutCreateRequestDto mentalWorkoutCreateRequestDto =
                dtoUtil.mentalWorkoutCreateRequestDto(1, List.of(defaultMental1.getId(), customMental1.getId()));

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, customMental1.getId(), HttpStatus.BAD_REQUEST);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_MENTAL_WORKOUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mentalWorkoutCreateRequestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getDefaultMentalWorkoutById_shouldReturnDefaultMentalWorkoutDtoWith200_whenValidRequest() throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef2), mentalType2);

        MentalWorkout defaultMentalWorkout =
                dbUtil.createDefaultMentalWorkout(1, List.of(defaultMental1, defaultMental2));

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.DEFAULT_MENTAL_WORKOUT_ID, defaultMentalWorkout.getId())
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalWorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalWorkoutResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("mentalActivities")
                .isEqualTo(defaultMentalWorkout);
        assertThat(responseDto.getMentalActivities())
                .usingRecursiveComparison()
                .ignoringFields("httpRefs", "user", "mentalTypeId")
                .isEqualTo(defaultMentalWorkout.getMentalActivitiesSortedById());
        assertEquals(
                defaultMentalWorkout.getMentalActivities().size(),
                responseDto.getMentalActivities().size());
    }

    @Test
    void getDefaultMentalWorkoutById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        long nonExistentDefaultMentalWorkoutId = 1000L;
        ApiException expectedException = new ApiException(
                ErrorMessage.MENTAL_WORKOUT_NOT_FOUND, nonExistentDefaultMentalWorkoutId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.DEFAULT_MENTAL_WORKOUT_ID, nonExistentDefaultMentalWorkoutId)
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalWorkoutById_shouldReturnDtoWith200_whenValidRequest() throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef2), mentalType2);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1, customHttpRef1), mentalType1, user);

        MentalWorkout customMentalWorkout =
                dbUtil.createCustomMentalWorkout(1, List.of(defaultMental1, defaultMental2, customMental1), user);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUT_ID, customMentalWorkout.getId())
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalWorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalWorkoutResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("mentalActivities")
                .isEqualTo(customMentalWorkout);
        assertThat(responseDto.getMentalActivities())
                .usingRecursiveComparison()
                .ignoringFields("httpRefs", "user", "mentalTypeId")
                .isEqualTo(customMentalWorkout.getMentalActivitiesSortedById());
        assertEquals(
                customMentalWorkout.getMentalActivities().size(),
                responseDto.getMentalActivities().size());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalWorkoutById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        long nonExistentCustomMentalWorkoutId = 1000L;
        ApiException expectedException = new ApiException(
                ErrorMessage.MENTAL_WORKOUT_NOT_FOUND, nonExistentCustomMentalWorkoutId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUT_ID, nonExistentCustomMentalWorkoutId)
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalWorkoutById_shouldReturnErrorMessageWith400_whenMentalWorkoutUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);
        MentalActivity customMental =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1), mentalType1, user2);

        MentalWorkout customMentalWorkout1 =
                dbUtil.createCustomMentalWorkout(1, List.of(defaultMental1, defaultMental2, customMental), user2);

        ApiException expectedException = new ApiException(
                ErrorMessage.USER_MENTAL_WORKOUT_MISMATCH, customMentalWorkout1.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUT_ID, customMentalWorkout1.getId())
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalWorkoutsWithFilter_shouldReturnValidationErrorMessageWith400_whenInvalidFilters() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        User user = dbUtil.createUser(1, role, country, timezone);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);
        MentalActivity customMental = dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1), mentalType1, user);

        MentalWorkout customMentalWorkout1 =
                dbUtil.createCustomMentalWorkout(1, List.of(defaultMental1, defaultMental2, customMental), user);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUTS)
                        .param("title", "title!")
                        .param("description", "description^")
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.title",
                        is("Title can include lower and upper-case letters, digits, spaces, and symbols . , - ( ) /")))
                .andExpect(
                        jsonPath(
                                "$.description",
                                is(
                                        "Description can include lower and upper-case letters, digits, spaces, and symbols: . , - : ; ! ? ' \" # % ( ) + =")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalWorkoutsWithFilter_shouldReturnDefaultAndCustomFilteredPageWith200_whenFilteredWithMentalTypeId()
            throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1), mentalType1, user1);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(4, List.of(defaultHttpRef1), mentalType2, user1);
        MentalActivity customMental3 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef1), mentalType1, user2);
        MentalActivity customMental4 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1), mentalType2, user2);

        MentalWorkout defaultMentalWorkout1 = dbUtil.createDefaultMentalWorkout(1, List.of(defaultMental1));
        MentalWorkout defaultMentalWorkout2 = dbUtil.createDefaultMentalWorkout(2, List.of(defaultMental2));
        MentalWorkout defaultMentalWorkout3 =
                dbUtil.createDefaultMentalWorkout(3, List.of(defaultMental1, defaultMental2));
        MentalWorkout customMentalWorkout1 =
                dbUtil.createCustomMentalWorkout(4, List.of(defaultMental1, defaultMental2, customMental1), user1);
        MentalWorkout customMentalWorkout2 =
                dbUtil.createCustomMentalWorkout(5, List.of(defaultMental1, defaultMental2, customMental2), user1);
        MentalWorkout customMentalWorkout3 =
                dbUtil.createCustomMentalWorkout(6, List.of(defaultMental1, defaultMental2, customMental3), user2);
        MentalWorkout customMentalWorkout4 =
                dbUtil.createCustomMentalWorkout(7, List.of(defaultMental1, defaultMental2, customMental4), user2);

        List<MentalWorkout> expectedFilteredMentalWorkouts =
                List.of(defaultMentalWorkout1, defaultMentalWorkout3, customMentalWorkout1, customMentalWorkout2);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUTS)
                        .param("mentalTypeId", mentalType1.getId() + "")
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.numberOfElements", is(4)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalWorkoutResponseDto> mentalWorkoutResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalWorkoutResponseDto>>() {});
        assertEquals(4, mentalWorkoutResponseDtoList.size());
        assertThat(mentalWorkoutResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "mentalActivities", "mentalTypeId")
                .isEqualTo(expectedFilteredMentalWorkouts);
        assertEquals(expectedFilteredMentalWorkouts.size(), mentalWorkoutResponseDtoList.size());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalWorkoutsWithFilter_shouldReturnCustomFilteredPageWith200_whenFilteredWithMentalTypeIds()
            throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1), mentalType1, user1);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(4, List.of(defaultHttpRef1), mentalType2, user1);
        MentalActivity customMental3 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef2), mentalType1, user1);
        MentalActivity customMental4 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user1);
        MentalActivity customMental5 =
                dbUtil.createCustomMentalActivity(7, List.of(defaultHttpRef1), mentalType1, user2);
        MentalActivity customMental6 =
                dbUtil.createCustomMentalActivity(8, List.of(defaultHttpRef1), mentalType2, user2);

        MentalWorkout defaultMentalWorkout1 = dbUtil.createDefaultMentalWorkout(1, List.of(defaultMental1));
        MentalWorkout defaultMentalWorkout2 = dbUtil.createDefaultMentalWorkout(2, List.of(defaultMental2));
        MentalWorkout customMentalWorkout1 =
                dbUtil.createCustomMentalWorkout(3, List.of(defaultMental1, customMental1), user1);
        MentalWorkout customMentalWorkout2 =
                dbUtil.createCustomMentalWorkout(4, List.of(customMental1, customMental3), user1);
        MentalWorkout customMentalWorkout3 =
                dbUtil.createCustomMentalWorkout(5, List.of(customMental3, customMental4), user1);
        MentalWorkout customMentalWorkout4 =
                dbUtil.createCustomMentalWorkout(6, List.of(defaultMental1, defaultMental2, customMental5), user2);
        MentalWorkout customMentalWorkout5 =
                dbUtil.createCustomMentalWorkout(7, List.of(defaultMental1, defaultMental2, customMental6), user2);

        List<MentalWorkout> expectedMentalWorkoutList =
                List.of(customMentalWorkout1, customMentalWorkout2, customMentalWorkout3);
        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUTS)
                        .param("isCustom", String.valueOf(true))
                        .param("mentalTypeId", mentalType1.getId() + "")
                        .param("pageSize", String.valueOf(3))
                        .param("pageNumber", String.valueOf(0))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(3)))
                .andExpect(jsonPath("$.numberOfElements", is(3)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalWorkoutResponseDto> mentalWorkoutResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalWorkoutResponseDto>>() {});
        assertEquals(3, mentalWorkoutResponseDtoList.size());
        assertThat(mentalWorkoutResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "mentalActivities", "mentalTypeId")
                .isEqualTo(expectedMentalWorkoutList);
    }

    @ParameterizedTest
    @MethodSource("getMentalWorkoutsWithFilterValidCustomFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalWorkoutsWithFilter_shouldReturnCustomFilteredPageWith200_whenValidFilters(
            String title,
            String description,
            String mentalTypeId,
            int pageSize,
            int pageNumber,
            int totalElements,
            int totalPages,
            int numberOfElementsCurrentPage,
            List<Long> resultSeeds)
            throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1), mentalType1, user1);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(4, List.of(defaultHttpRef1), mentalType2, user1);
        MentalActivity customMental3 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef2), mentalType1, user1);
        MentalActivity customMental4 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user1);
        MentalActivity customMental5 =
                dbUtil.createCustomMentalActivity(7, List.of(defaultHttpRef1), mentalType1, user2);
        MentalActivity customMental6 =
                dbUtil.createCustomMentalActivity(8, List.of(defaultHttpRef1), mentalType2, user2);

        MentalWorkout defaultMentalWorkout1 = dbUtil.createDefaultMentalWorkout(1, List.of(defaultMental1));
        MentalWorkout defaultMentalWorkout2 = dbUtil.createDefaultMentalWorkout(2, List.of(defaultMental2));
        MentalWorkout customMentalWorkout1 =
                dbUtil.createCustomMentalWorkout(3, List.of(defaultMental1, customMental1), user1);
        MentalWorkout customMentalWorkout2 =
                dbUtil.createCustomMentalWorkout(4, List.of(customMental1, customMental3), user1);
        MentalWorkout customMentalWorkout3 =
                dbUtil.createCustomMentalWorkout(5, List.of(customMental3, customMental4), user1);
        MentalWorkout customMentalWorkout4 =
                dbUtil.createCustomMentalWorkout(6, List.of(defaultMental1, defaultMental2, customMental5), user2);
        MentalWorkout customMentalWorkout5 =
                dbUtil.createCustomMentalWorkout(7, List.of(defaultMental1, defaultMental2, customMental6), user2);
        MentalWorkout customMentalWorkout6 = dbUtil.createCustomMentalWorkout(8, List.of(customMental2), user1);

        List<MentalWorkout> expectedFilteredMentalWorkouts = Stream.of(
                        defaultMentalWorkout1,
                        defaultMentalWorkout2,
                        customMentalWorkout1,
                        customMentalWorkout2,
                        customMentalWorkout3,
                        customMentalWorkout4,
                        customMentalWorkout5,
                        customMentalWorkout6)
                .filter(mentalWorkout -> resultSeeds.stream()
                        .anyMatch(seed -> mentalWorkout.getTitle().contains(String.valueOf(seed))))
                .toList();

        String sortDirection = "ASC";
        String sortField = "id";
        Optional<String> metalFilter = Stream.of(mentalType1, mentalType2)
                .filter(type -> type.getName().equals(mentalTypeId))
                .map(type -> type.getId().toString())
                .findFirst();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUTS)
                        .param("isCustom", String.valueOf(true))
                        .param("title", title)
                        .param("description", description)
                        .param("mentalTypeId", metalFilter.orElse(null))
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElementsCurrentPage)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalWorkoutResponseDto> mentalWorkoutResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalWorkoutResponseDto>>() {});
        assertEquals(numberOfElementsCurrentPage, mentalWorkoutResponseDtoList.size());
        assertThat(mentalWorkoutResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "user", "mentalActivities", "mentalTypeId", "httpRefs", "nutrition", "exercise")
                .isEqualTo(expectedFilteredMentalWorkouts);
    }

    static Stream<Arguments> getMentalWorkoutsWithFilterValidCustomFilters() {
        return Stream.of(
                // Custom, positive
                Arguments.of(null, null, "AFFIRMATION", 2, 0, 3, 2, 2, List.of(3L, 4L)),
                Arguments.of("mentalWorkout", null, "AFFIRMATION", 2, 0, 3, 2, 2, List.of(3L, 4L)),
                Arguments.of(null, "description", "AFFIRMATION", 2, 0, 3, 2, 2, List.of(3L, 4L)),
                Arguments.of("mentalWorkout", "description", "AFFIRMATION", 2, 0, 3, 2, 2, List.of(3L, 4L)),
                Arguments.of(null, null, "MEDITATION", 1, 0, 1, 1, 1, List.of(8L)),
                Arguments.of("mentalWorkout", null, "MEDITATION", 1, 0, 1, 1, 1, List.of(8L)),
                Arguments.of(null, "description", "MEDITATION", 1, 0, 1, 1, 1, List.of(8L)),
                Arguments.of("mentalWorkout", "description", "MEDITATION", 1, 0, 1, 1, 1, List.of(8L)),

                // Custom, empty
                Arguments.of("non existent", null, "AFFIRMATION", 2, 0, 0, 0, 0, Collections.emptyList()),
                Arguments.of(null, "non existent", "MEDITATION", 2, 0, 0, 0, 0, Collections.emptyList()));
    }

    @ParameterizedTest
    @MethodSource("getMentalWorkoutsWithFilterValidDefaultFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalWorkoutsWithFilter_shouldReturnDefaultFilteredPageWith200_whenValidFilters(
            String title,
            String description,
            String mentalTypeId,
            int pageSize,
            int pageNumber,
            int totalElements,
            int totalPages,
            int numberOfElementsCurrentPage,
            List<Long> resultSeeds)
            throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);
        MentalActivity defaultMental3 = dbUtil.createDefaultMentalActivity(3, List.of(defaultHttpRef2), mentalType1);
        MentalActivity defaultMental4 = dbUtil.createDefaultMentalActivity(4, List.of(defaultHttpRef2), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1), mentalType1, user1);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(4, List.of(defaultHttpRef1), mentalType2, user1);
        MentalActivity customMental3 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef2), mentalType1, user1);
        MentalActivity customMental4 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user1);
        MentalActivity customMental5 =
                dbUtil.createCustomMentalActivity(7, List.of(defaultHttpRef1), mentalType1, user2);
        MentalActivity customMental6 =
                dbUtil.createCustomMentalActivity(8, List.of(defaultHttpRef1), mentalType2, user2);

        MentalWorkout defaultMentalWorkout1 = dbUtil.createDefaultMentalWorkout(1, List.of(defaultMental1));
        MentalWorkout defaultMentalWorkout2 = dbUtil.createDefaultMentalWorkout(2, List.of(defaultMental2));
        MentalWorkout defaultMentalWorkout3 = dbUtil.createDefaultMentalWorkout(3, List.of(defaultMental3));
        MentalWorkout defaultMentalWorkout4 = dbUtil.createDefaultMentalWorkout(4, List.of(defaultMental4));
        MentalWorkout customMentalWorkout1 = dbUtil.createCustomMentalWorkout(5, List.of(customMental1), user1);
        MentalWorkout customMentalWorkout2 =
                dbUtil.createCustomMentalWorkout(6, List.of(customMental1, customMental3), user1);
        MentalWorkout customMentalWorkout3 =
                dbUtil.createCustomMentalWorkout(7, List.of(customMental3, customMental4), user1);
        MentalWorkout customMentalWorkout4 =
                dbUtil.createCustomMentalWorkout(8, List.of(defaultMental1, defaultMental2, customMental5), user2);
        MentalWorkout customMentalWorkout5 =
                dbUtil.createCustomMentalWorkout(9, List.of(defaultMental1, defaultMental2, customMental6), user2);
        MentalWorkout customMentalWorkout6 = dbUtil.createCustomMentalWorkout(10, List.of(customMental2), user1);

        List<MentalWorkout> expectedFilteredMentalWorkouts = Stream.of(
                        defaultMentalWorkout1,
                        defaultMentalWorkout2,
                        defaultMentalWorkout3,
                        defaultMentalWorkout4,
                        customMentalWorkout1,
                        customMentalWorkout2,
                        customMentalWorkout3,
                        customMentalWorkout6)
                .filter(mentalWorkout -> resultSeeds.stream()
                        .anyMatch(seed -> mentalWorkout.getTitle().equals("MentalWorkout " + seed)))
                .toList();

        String sortDirection = "ASC";
        String sortField = "id";
        Optional<String> metalFilter = Stream.of(mentalType1, mentalType2)
                .filter(type -> type.getName().equals(mentalTypeId))
                .map(type -> type.getId().toString())
                .findFirst();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUTS)
                        .param("isCustom", String.valueOf(false))
                        .param("title", title)
                        .param("description", description)
                        .param("mentalTypeId", metalFilter.orElse(""))
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElementsCurrentPage)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalWorkoutResponseDto> mentalWorkoutResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalWorkoutResponseDto>>() {});
        assertEquals(numberOfElementsCurrentPage, mentalWorkoutResponseDtoList.size());
        assertThat(mentalWorkoutResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "user", "mentalActivities", "mentalTypeId", "httpRefs", "nutrition", "exercise")
                .isEqualTo(expectedFilteredMentalWorkouts);
    }

    static Stream<Arguments> getMentalWorkoutsWithFilterValidDefaultFilters() {
        return Stream.of(
                // Default, positive
                Arguments.of(null, null, "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 3L)),
                Arguments.of("mentalWorkout", null, "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 3L)),
                Arguments.of(null, "description", "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 3L)),
                Arguments.of("mentalWorkout", "description", "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 3L)),
                Arguments.of(null, null, "MEDITATION", 2, 0, 2, 1, 2, List.of(2L, 4L)),
                Arguments.of("mentalWorkout", null, "MEDITATION", 2, 0, 2, 1, 2, List.of(2L, 4L)),
                Arguments.of(null, "description", "MEDITATION", 2, 0, 2, 1, 2, List.of(2L, 4L)),
                Arguments.of("mentalWorkout", "description", "MEDITATION", 2, 0, 2, 1, 2, List.of(2L, 4L)),

                // Default, empty
                Arguments.of("non existent", null, "AFFIRMATION", 2, 0, 0, 0, 0, Collections.emptyList()),
                Arguments.of(null, "non existent", "MEDITATION", 2, 0, 0, 0, 0, Collections.emptyList()));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalWorkoutsWithFilter_shouldReturnDefaultFilteredPageWith200_whenFilteredWithMentalTypeIds()
            throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);
        MentalActivity defaultMental3 = dbUtil.createDefaultMentalActivity(3, List.of(defaultHttpRef2), mentalType1);
        MentalActivity defaultMental4 = dbUtil.createDefaultMentalActivity(4, List.of(defaultHttpRef2), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1), mentalType1, user1);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(4, List.of(defaultHttpRef1), mentalType2, user1);
        MentalActivity customMental3 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef2), mentalType1, user1);
        MentalActivity customMental4 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user1);
        MentalActivity customMental5 =
                dbUtil.createCustomMentalActivity(7, List.of(defaultHttpRef1), mentalType1, user2);
        MentalActivity customMental6 =
                dbUtil.createCustomMentalActivity(8, List.of(defaultHttpRef1), mentalType2, user2);

        MentalWorkout defaultMentalWorkout1 = dbUtil.createDefaultMentalWorkout(1, List.of(defaultMental1));
        MentalWorkout defaultMentalWorkout2 = dbUtil.createDefaultMentalWorkout(2, List.of(defaultMental2));
        MentalWorkout defaultMentalWorkout3 = dbUtil.createDefaultMentalWorkout(3, List.of(defaultMental3));
        MentalWorkout defaultMentalWorkout4 = dbUtil.createDefaultMentalWorkout(4, List.of(defaultMental4));
        MentalWorkout customMentalWorkout1 =
                dbUtil.createCustomMentalWorkout(5, List.of(customMental3, customMental4), user1);
        MentalWorkout customMentalWorkout2 =
                dbUtil.createCustomMentalWorkout(6, List.of(defaultMental1, defaultMental2, customMental5), user2);
        MentalWorkout customMentalWorkout3 =
                dbUtil.createCustomMentalWorkout(7, List.of(defaultMental1, defaultMental2, customMental6), user2);

        List<MentalWorkout> expectedMentalWorkoutList = List.of(defaultMentalWorkout1, defaultMentalWorkout3);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTAL_WORKOUTS)
                        .param("isCustom", String.valueOf(false))
                        .param("mentalTypeId", mentalType1.getId() + "")
                        .param("pageSize", String.valueOf(2))
                        .param("pageNumber", String.valueOf(0))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.numberOfElements", is(2)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalWorkoutResponseDto> mentalWorkoutResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalWorkoutResponseDto>>() {});
        assertEquals(2, mentalWorkoutResponseDtoList.size());
        assertThat(mentalWorkoutResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user", "mentalActivities", "mentalTypeId")
                .isEqualTo(expectedMentalWorkoutList);
    }
}
