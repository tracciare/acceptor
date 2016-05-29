package re.traccia.model;

import java.util.List;

/**
 * Created by fiorenzo on 29/05/16.
 */
public class User {
    private String name;
    private String surname;
    private String email;
    private String creditcard;
    private List<String> plateNumbers;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreditcard() {
        return creditcard;
    }

    public void setCreditcard(String creditcard) {
        this.creditcard = creditcard;
    }

    public List<String> getPlateNumbers() {
        return plateNumbers;
    }

    public void setPlateNumbers(List<String> plateNumbers) {
        this.plateNumbers = plateNumbers;
    }
}
