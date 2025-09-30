package ceu.biolab.sendSerialization;

import java.io.Serializable;

public class Client implements Serializable{

    private static final long serialVersionUID = -6291904286218553733L;

    private final String name;
    private final int age;
    private final float salary;
    
    public Client(String nombre, int edad, float saldo) {
        this.name = nombre;
        this.age = edad;
        this.salary = saldo;
    }

    @Override
    public String toString() {
        return ("Name: " + name + "\nAge: " + age
                + "\nSalary: " + salary);
    }
}
