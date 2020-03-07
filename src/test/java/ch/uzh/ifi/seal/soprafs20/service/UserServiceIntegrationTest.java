package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.SopraServiceException;
import ch.uzh.ifi.seal.soprafs20.exceptions.UsernameAlreadyExists;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

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

    @BeforeEach
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

        User testUser = new User();
        testUser.setUsername("");
        testUser.setPassword("testPassword");
        User createdUser = userService.createUser(testUser);

        User testUser2 = new User();
        testUser2.setUsername("testUsername");
        testUser.setPassword("");
        User createdUser2 = userService.createUser(testUser2);

        User testUser3 = new User();
        testUser3.setUsername("testUsername");
        testUser.setPassword("");
        User createdUser3 = userService.createUser(testUser3);


        // check that an error is thrown
        String exceptionMessage = "The username provided is not unique. Therefore, the user could not be created!";
        UsernameAlreadyExists exception = assertThrows(UsernameAlreadyExists.class, () -> userService.createUser(testUser2), exceptionMessage);
        assertEquals(exceptionMessage, exception.getMessage());
    }
}
