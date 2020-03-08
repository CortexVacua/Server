package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.*;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @AfterEach
    public void setup() {
        userRepository.deleteAll();
    }



    @Test
    public void createUser_validInputs_success() {
        // given
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");

        // when
        User createdUser = userService.createUser(testUser);

        // then
        assertNotNull(createdUser.getId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNotNull(createdUser.getToken());
        assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
    }

    @Test
    public void createUser_duplicateUsername_throwsException() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User createdUser = userService.createUser(testUser);

        // attempt to create second user with same username
        User testUser2 = new User();

        // change the name but forget about the username
        testUser2.setUsername("testUsername");
        testUser.setPassword("testPassword");

        // check that an error is thrown
        String exceptionMessage = "The username provided is not unique. Therefore, the user could not be created!";
        UsernameAlreadyExists exception = assertThrows(UsernameAlreadyExists.class, () -> userService.createUser(testUser2), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    public void createUser_emptyStrings_throwsException() {
        assertNull(userRepository.findByUsername("testUsername"));

//        given users with empty strings in username, password or both

        User testUser = new User();
        testUser.setUsername("");
        testUser.setPassword("testPassword");

        User testUser2 = new User();
        testUser2.setUsername("testUsername");
        testUser2.setPassword("");

        User testUser3 = new User();
        testUser3.setUsername("");
        testUser3.setPassword("");


        // check that an error is thrown
        String exceptionMessage = "Username and/or password can't consist of an empty string!";
        IllegalRegistrationInput exception = assertThrows(IllegalRegistrationInput.class, () -> userService.createUser(testUser), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
        IllegalRegistrationInput exception2 = assertThrows(IllegalRegistrationInput.class, () -> userService.createUser(testUser2), exceptionMessage);
        assertEquals(exceptionMessage, exception2.getMessage());
        IllegalRegistrationInput exception3 = assertThrows(IllegalRegistrationInput.class, () -> userService.createUser(testUser3), exceptionMessage);
        assertEquals(exceptionMessage, exception3.getMessage());
    }

    @Test
    public void login_validCredentials() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);

        User loggedInUser=userService.loginUser(newUser);

//        check if everything has gone right
        assertEquals(loggedInUser.getStatus(),UserStatus.ONLINE);
        assertEquals(newUser.getId(), loggedInUser.getId());
        assertEquals(newUser.getUsername(), loggedInUser.getUsername());
        assertEquals(newUser.getPassword(), loggedInUser.getPassword());
        assertEquals(newUser.getAccountCreationDate(), loggedInUser.getAccountCreationDate());
        assertEquals(newUser.getBirthday(), loggedInUser.getBirthday());
        assertEquals(newUser.getToken(), loggedInUser.getToken());
    }

    @Test
    public void login_validCredentials_but_alreadyLoggedIn() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);

//        loginUser
        userService.loginUser(newUser);

        // check that an error is thrown if user is already logged in
        assertThrows(UserAlreadyLoggedIn.class, () -> userService.loginUser(newUser));

    }

    @Test
    public void login_invalidCredentials() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);

        User testUser2 = new User();
        testUser2.setUsername("Wrong");
        testUser2.setPassword("testPassword");

        User testUser3 = new User();
        testUser3.setUsername("testUsername");
        testUser3.setPassword("Wrong");


        // check that an error is thrown if wrong username or password is used
        String exceptionMessage = "No user with this username exists.";
        String exceptionMessage2 = "Incorrect password.";
        UserCredentialsWrong exception = assertThrows(UserCredentialsWrong.class, () -> userService.loginUser(testUser2), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
        UserCredentialsWrong exception2 = assertThrows(UserCredentialsWrong.class, () -> userService.loginUser(testUser3), exceptionMessage2);
        assertEquals(exceptionMessage2, exception2.getMessage());
    }

    @Test
    public void logout_valid_Token() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);

        userService.loginUser(newUser);
        userService.logOutUser(newUser);

        assertEquals(newUser.getStatus(), UserStatus.OFFLINE);
    }

    @Test
    public void logout_invalid_Token() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);
        testUser.setToken("sdfaadsfadfasf");

//    make sure error is thrown because of wrong token
        String exceptionMsg ="No user with same token as your session exists.";
        UserNotAvailable exception = assertThrows(UserNotAvailable.class, () -> userService.logOutUser(testUser), exceptionMsg);
        assertEquals(exceptionMsg, exception.getMessage());
    }

    @Test
    public void logout_alreadyLoggedOut() {
        assertNull(userRepository.findByUsername("testUsername"));

//        keep user offline
        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);

//    make sure error is thrown because user is already logged out

        assertThrows(UserAlreadyLoggedOut.class, () -> userService.logOutUser(testUser));
      }

    @Test
    public void update_validInput() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);
        newUser.setUsername("123");
        newUser.setBirthday(new Date());

        userService.updateUser(newUser, newUser.getId().toString());

//    make sure error is thrown because user is already logged out

        assertEquals(userRepository.findByToken(newUser.getToken()).getUsername(), newUser.getUsername());}

    @Test
    public void update_UserIdDoesNotExist() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);
        newUser.setUsername("123");
        newUser.setBirthday(new Date());



//    make sure error is thrown if id does not map to user
        String exceptionMsg= "No user with specified ID exists.";
        UserNotAvailable exception= assertThrows(UserNotAvailable.class, () -> userService.updateUser(newUser, "007"));
        assertEquals(exception.getMessage(),exceptionMsg);

    }

    @Test
    public void update_UserIdDoesNotMatchToken() {
        userRepository.deleteAll();
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User testUser2 = new User();
        testUser2.setUsername("testUsername2");
        testUser2.setPassword("testPassword");
        User newUser=userService.createUser(testUser);
        User newUser2=userService.createUser(testUser2);

        newUser.setUsername("123");
        newUser.setBirthday(new Date());


//    make sure error is thrown if id and token do not match
        String exceptionMsg= "You are not authorized to change this user, since tokens do not match.";
        UserCredentialsWrong exception= assertThrows(UserCredentialsWrong.class, () -> userService.updateUser(newUser, newUser2.getId().toString()));
        assertEquals(exception.getMessage(),exceptionMsg);
    }

    @Test
    public void update_UsernameAlreadyExists() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User testUser2 = new User();
        testUser2.setUsername("testUsername2");
        testUser2.setPassword("testPassword");
        User newUser=userService.createUser(testUser);
        userService.createUser(testUser2);
        newUser.setUsername("testUsername2");
        newUser.setBirthday(new Date());

//    make sure error is thrown if id and token do not match
        String exceptionMsg= "Username is already in use!";
        UsernameAlreadyExists exception= assertThrows(UsernameAlreadyExists.class, () -> userService.updateUser(newUser, newUser.getId().toString()));
        assertEquals(exception.getMessage(),exceptionMsg);
    }

    @Test
    public void getUser_userIdExists() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);
        User getUser=userService.getUser(testUser);


//    make sure everything works
        assertEquals(getUser.getId(), newUser.getId());
        assertEquals(getUser.getUsername(), newUser.getUsername());
        assertEquals(getUser.getPassword(), newUser.getPassword());
        assertEquals(getUser.getToken(), newUser.getToken());
    }

    @Test
    public void getUser_userIdDoesNotExists() {
        assertNull(userRepository.findByUsername("testUsername"));

        User testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        User newUser=userService.createUser(testUser);
        newUser.setId(111L);


//    make sure exception thrown when id does not exist
        String exceptionMsg= "No user with this id exists, that can be fetched.";
        UserNotAvailable exception= assertThrows(UserNotAvailable.class, () -> userService.getUser(newUser));
        assertEquals(exception.getMessage(),exceptionMsg);
    }
}
