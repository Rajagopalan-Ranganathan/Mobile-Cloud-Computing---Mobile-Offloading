package fi.aalti.mobcompoffloading;



public class UserLogin {
    private String email;
    private String password;
    public String token; // Get the token from the response and use it for further communication - For security Reasons

        //Create a constructor
        public UserLogin(String email, String password) {
            this.email = email;
        this.password = password;
    }

}