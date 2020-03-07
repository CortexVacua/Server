package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByUsername_success() {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");
        user.setAccountCreationDate(new Date());
        user.setPassword("password");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByUsername(user.getUsername());

        // then
        assertNotNull(found.getId());
        assertEquals(found.getUsername(), user.getUsername());
        assertEquals(found.getToken(), user.getToken());
        assertEquals(found.getStatus(), user.getStatus());
        assertEquals(found.getBirthday(), user.getBirthday());
        assertEquals(found.getAccountCreationDate(), user.getAccountCreationDate());
        assertEquals(found.getPassword(), user.getPassword());
    }

    @Test
    public void findById_success() {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");
        user.setAccountCreationDate(new Date());
        user.setPassword("password");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User username = userRepository.findByUsername(user.getUsername());
        Optional<User> found= userRepository.findById(username.getId());

        // then
        assertNotNull(found.get().getId());
        assertEquals(found.get().getUsername(), user.getUsername());
        assertEquals(found.get().getToken(), user.getToken());
        assertEquals(found.get().getStatus(), user.getStatus());
        assertEquals(found.get().getBirthday(), user.getBirthday());
        assertEquals(found.get().getAccountCreationDate(), user.getAccountCreationDate());
        assertEquals(found.get().getPassword(), user.getPassword());
    }

    @Test
    public void findByToken_success() {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");
        user.setAccountCreationDate(new Date());
        user.setPassword("password");

        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByToken(user.getToken());

        // then
        assertNotNull(found.getId());
        assertEquals(found.getUsername(), user.getUsername());
        assertEquals(found.getToken(), user.getToken());
        assertEquals(found.getStatus(), user.getStatus());
        assertEquals(found.getBirthday(), user.getBirthday());
        assertEquals(found.getAccountCreationDate(), user.getAccountCreationDate());
        assertEquals(found.getPassword(), user.getPassword());
    }
}
