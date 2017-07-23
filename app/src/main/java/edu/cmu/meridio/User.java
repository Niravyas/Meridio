package edu.cmu.meridio;

/**
 * Created by yadav on 7/22/2017.
 */

public class User {
    static User user;
    static String userID;
    private User(){};
    public static User getInstance(){
        if (user == null){
            user = new User();
        }
        return user;
    }

    public void setUserID(String userID){
        User u = User.getInstance();
        u.userID = userID;
    }

    public String getUserID(){
        User u = User.getInstance();
        return u.userID;
    }
}
