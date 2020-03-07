package ch.uzh.ifi.seal.soprafs20.controller;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.*;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPostDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPutDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserPutUserIdDTO;
import ch.uzh.ifi.seal.soprafs20.rest.dto.UserTokenDTO;
import ch.uzh.ifi.seal.soprafs20.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean UserRepository userRepository;

    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);

        List<User> allUsers = Collections.singletonList(user);

        // this mocks the UserService -> we define above what the userService should return when getUsers() is called
        given(userService.getUsers()).willReturn(allUsers);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }

    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);


        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setUsername("testPassword");

        given(userService.createUser(Mockito.any())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));}

    @Test
    public void createUser_duplicateUsername_userCreated() throws Exception {
        // given
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        given(userService.createUser(Mockito.any())).willThrow(UsernameAlreadyExists.class);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    @Test
    public void createUser_InvalidInput() throws Exception {
        // given
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        given(userService.createUser(Mockito.any())).willThrow(IllegalRegistrationInput.class);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void login_validCredentials() throws Exception {
        // given
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setPassword("testPassword");

        User user= new User();
        user.setId(1L);
        user.setToken("test");

        given(userService.loginUser(Mockito.any())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("token", is(user.getToken())))
                .andExpect(jsonPath("id", is(user.getId().intValue())));
    }

    @Test
    public void login_validCredentials_but_alreadyLoggedIn() throws Exception {
        // given
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setPassword("testPassword");


        given(userService.loginUser(Mockito.any())).willThrow(new UserAlreadyLoggedIn());

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void login_invalidCredentials() throws Exception {
        // given
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setPassword("testPassword");

        given(userService.loginUser(Mockito.any())).willThrow(UserCredentialsWrong.class);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void logout_valid_Token() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testUsername");
        userTokenDTO.setId(123L);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isOk());
    }

    @Test
    public void logout_invalid_Token() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testUsername");
        userTokenDTO.setId(123L);

        doThrow(new UserNotAvailable("No user with same token as yours exists.")).when(userService).logOutUser(Mockito.any());

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void logout_userAlreadyLoggedOut() throws Exception {
        // given
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setToken("testUsername");
        userTokenDTO.setId(123L);

        doThrow(new UserAlreadyLoggedOut()).when(userService).logOutUser(Mockito.any());

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTokenDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void update_validInput() throws Exception {
        // given
        UserPutUserIdDTO userPutUserIdDTO = new UserPutUserIdDTO();
        userPutUserIdDTO.setToken("test");
        userPutUserIdDTO.setUsername("testUsername");
        userPutUserIdDTO.setBirthday(new Date());

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/users/{userId}",1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutUserIdDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNoContent());
    }


    @Test
    public void update_userIdDoesNotExist() throws Exception {
        // given
        UserPutUserIdDTO userPutUserIdDTO = new UserPutUserIdDTO();
        userPutUserIdDTO.setToken("test");
        userPutUserIdDTO.setUsername("testUsername");
        userPutUserIdDTO.setBirthday(new Date());

        doThrow(new UserNotAvailable("No user with this userId exists.")).when(userService).updateUser(Mockito.any(),Mockito.any());

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/users/{userId}",1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutUserIdDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void update_userIdDoesNotMatchToken() throws Exception {
        // given
        UserPutUserIdDTO userPutUserIdDTO = new UserPutUserIdDTO();
        userPutUserIdDTO.setToken("test");
        userPutUserIdDTO.setUsername("testUsername");
        userPutUserIdDTO.setBirthday(new Date());

        doThrow(new UserCredentialsWrong("You are not authorized to change profile attributes with your token!")).when(userService).updateUser(Mockito.any(),Mockito.any());

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/users/{userId}",1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutUserIdDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void update_userNameAlreadyTaken() throws Exception {
        // given
        UserPutUserIdDTO userPutUserIdDTO = new UserPutUserIdDTO();
        userPutUserIdDTO.setToken("test");
        userPutUserIdDTO.setUsername("testUsername");
        userPutUserIdDTO.setBirthday(new Date());

        doThrow(new UsernameAlreadyExists("Username is not unique!")).when(userService).updateUser(Mockito.any(),Mockito.any());

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = put("/users/{userId}",1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutUserIdDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }



    @Test
    public void getUser_UserIdExists() throws Exception {
        // given
        User user= new User();
        user.setAccountCreationDate(new Date());
        user.setBirthday(new Date());
        user.setUsername("test");
        user.setId(1L);
        user.setStatus(UserStatus.ONLINE);

        given(userService.getUser(Mockito.any())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = get("/users/{userId}", 1);

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(user.getId().intValue())))
                .andExpect(jsonPath("username", is(user.getUsername())))
                .andExpect(jsonPath("status", is(user.getStatus().toString())));
    }

    @Test
    public void getUser_UserIdDoesNotExists() throws Exception {
        // given
        given(userService.getUser(Mockito.any())).willThrow(new UserNotAvailable("No User with this id available!"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = get("/users/{userId}", 1);

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }


    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     * @param object
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new SopraServiceException(String.format("The request body could not be created.%s", e.toString()));
        }
    }
}