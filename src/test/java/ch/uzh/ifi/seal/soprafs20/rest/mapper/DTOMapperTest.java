package ch.uzh.ifi.seal.soprafs20.rest.mapper;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.rest.dto.*;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation works.
 */
public class DTOMapperTest {
    @Test
    public void testCreateUser_fromUserPostDTO_toUser_success() {
        // create UserPostDTO
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("username");
        userPostDTO.setPassword("password");

        // MAP -> Create user
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // check content
        assertEquals(userPostDTO.getUsername(), user.getUsername());
        assertEquals(userPostDTO.getPassword(), user.getPassword());
    }

    @Test
    public void testGetUser_fromUser_toUserGetDTO_success() {
        // create User
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);
        long i=123;
        user.setId(i);
        user.setBirthday(new Date());
        user.setAccountCreationDate(new Date());


        // MAP -> Create UserGetDTO
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // check content
        assertEquals(user.getId(), userGetDTO.getId());
        assertEquals(user.getUsername(), userGetDTO.getUsername());
        assertEquals(user.getStatus(), userGetDTO.getStatus());
        assertEquals(user.getAccountCreationDate(), userGetDTO.getAccountCreationDate());
        assertEquals(user.getBirthday(),userGetDTO.getBirthday());
        ;
    }

    @Test
    public void testCreateUser_from_UserPutDTO_success(){
        // create UserPutDTO
        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("username");
        userPutDTO.setPassword("password");

        // MAP -> Create user
        User user = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        // check content
        assertEquals(userPutDTO.getUsername(), user.getUsername());
        assertEquals(userPutDTO.getPassword(), user.getPassword());
    }

    @Test
    public void testCreateUser_from_String_success(){
//        Create String
        String str= "123";
        String badStr= "abc";

//        Map to User
        User user1 = DTOMapper.INSTANCE.convertUserIdStringToEntity(str);


//        check content
        assertEquals(user1.getId(),Long.valueOf(str));
        assertThrows(NumberFormatException.class, () ->{DTOMapper.INSTANCE.convertUserIdStringToEntity(badStr);});
    }

    @Test
    public void testCreateUserTokenDTO_from_User_success(){
//        Create User
        User user = new User();
        user.setToken("123");
        long i= 123;
        user.setId(i);

//        Map to UserTokenDTO
        UserTokenDTO userTokenDTO = DTOMapper.INSTANCE.convertEntityToUserTokenDTO(user);

//        check content
        assertEquals(userTokenDTO.getToken(), user.getToken());
        assertEquals(userTokenDTO.getId(), user.getId());
    }

    @Test
    public void testCreateUser_from_UserTokenDTO_success(){
//        Create User
        UserTokenDTO userTokenDTO= new UserTokenDTO();
        long i= 123;
        userTokenDTO.setId(i);
        userTokenDTO.setToken("abc");

//        Map to UserTokenDTO
        User user = DTOMapper.INSTANCE.convertUserTokenDTOToEntity(userTokenDTO);

//        check content
        assertEquals(userTokenDTO.getToken(), user.getToken());
        assertEquals(userTokenDTO.getId(), user.getId());
    }

    @Test
    public void testCreateUser_from_UserPutUserIdDTO(){
//        Create UserPutUserIdDTO
        UserPutUserIdDTO userPutUserIdDTO = new UserPutUserIdDTO();
        userPutUserIdDTO.setToken("123");
        userPutUserIdDTO.setUsername("234");
        userPutUserIdDTO.setBirthday(new Date());

//        Map to User
        User user = DTOMapper.INSTANCE.convertUserPutUserIdDTOToEntity(userPutUserIdDTO);

//        check content
        assertEquals(user.getToken(), userPutUserIdDTO.getToken());
        assertEquals(user.getBirthday(),userPutUserIdDTO.getBirthday());
        assertEquals(user.getUsername(), userPutUserIdDTO.getUsername());


    }
}
