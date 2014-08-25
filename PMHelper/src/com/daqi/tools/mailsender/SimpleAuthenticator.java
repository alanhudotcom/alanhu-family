package com.daqi.tools.mailsender;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SimpleAuthenticator extends Authenticator{  
    String userName=null;  
    String password=null;  
       
    public SimpleAuthenticator(){  
    }  
    public SimpleAuthenticator(String username, String password) {   
        this.userName = username;   
        this.password = password;   
    }   
    protected PasswordAuthentication getPasswordAuthentication(){  
        return new PasswordAuthentication(userName, password);  
    }  
}  
   