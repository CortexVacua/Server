package ch.uzh.ifi.seal.soprafs20.rest.dto;

import java.util.Date;

public class UserPutUserIdDTO {

    private String token;
    private String username;
    private Date birthday;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getBirthday() {
        return birthday;
    }
}
