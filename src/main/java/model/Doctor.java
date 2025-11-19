package model;

/**
 * The {@code Doctor} class represents a medical professional entity.
 * A {@code Doctor} object encapsulates the necessary information to:
 * <ul>
 *     <li> The doctor's name and surname </li>
 *     <li> The doctor's email address </li>
 *     <li> The doctor's contact information </li>
 *     <li> The doctor's address </li>
 * </ul>
 *
 *<p>
 *     This class has no id fields and no relations, so it is not directly tied to the database. It just holds the basic
 *     information about a doctor from the point of view of a "Patient"-
 *</p>
 *
 * @author MamenCortes
 */
public class Doctor {

    private String name;
    private String surname;
    private String email;
    private Integer phone;
    private String address;

    /**
     * Creates a {@code Doctor} instance with all values specified. This is typically used when all the information
     * needed to create the object is achieved. The object comes out already complete
     *
     * @param name      the doctor's name
     * @param surname   the doctor's surname
     * @param email     the doctor's email address
     * @param phone     the doctor's contact information
     * @param address   the doctor's address
     */
    public Doctor(String name, String surname, String email, Integer phone, String address) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    /**
     * Creates a default {@code Doctor} instance with most of the field values empty. This is typically
     * used when wanting to create a random Doctor that only has a phone number or when an empty instance
     * is desired and then fill it later.
     */
    public Doctor() {
        this.name = "";
        this.surname = "";
        this.email = "";
        this.phone = 123456789;
        this.address = "";
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone +
                ", address='" + address + '\'' +
                '}';
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

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
