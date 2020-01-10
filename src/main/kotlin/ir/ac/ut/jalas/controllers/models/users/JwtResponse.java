package ir.ac.ut.jalas.controllers.models.users;

import java.io.Serializable;

public class JwtResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;

    private final String token;
    private final String type;

    public JwtResponse(String token) {
        this.token = token;
        this.type = "Bearer";
    }

    public String getToken() {
        return this.token;
    }

    public String getType() {
        return type;
    }
}