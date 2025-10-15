package model;

import java.util.ArrayList;
import java.util.Random;

public class ModelManager {
    public static Patient generateRandomPatient() {
        String[] POSSIBLE_NAMES = {"Alice", "Bob", "Charlie", "David", "Eve"};
        String[] POSSIBLE_SURNAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones"};
        String[] POSSIBLE_SEXES = {"Male", "Female"};
        String EMAIL_DOMAIN = "@example.com";  // Fixed domain for email generation
        Random random = new Random();
        // Generate random values
        String name = POSSIBLE_NAMES[random.nextInt(POSSIBLE_NAMES.length)];
        String surname = POSSIBLE_SURNAMES[random.nextInt(POSSIBLE_SURNAMES.length)];
        String email = name.toLowerCase() + "." + surname.toLowerCase() + EMAIL_DOMAIN;
        int phone = 600000000 + random.nextInt(100000000);  // Random 10-digit number (1,000,000,000 to 1,999,999,999)
        String sex = POSSIBLE_SEXES[random.nextInt(POSSIBLE_SEXES.length)];
        // Generate a random date of birth (between 1950-01-01 and 2000-12-31)
        int year = 1950 + random.nextInt(51);  // 1950 to 2000
        int month = 1 + random.nextInt(12);    // 1 to 12
        int day = 1 + random.nextInt(28);      // 1 to 28 (to avoid invalid dates like Feb 30)
        String dateOfBirth = String.format("%02d-%02d-%04d", day, month, year);  // Format as "YYYY-MM-DD"
        // Create and populate the Patient object
        Patient patient = new Patient();
        patient.setName(name);
        patient.setSurname(surname);
        patient.setEmail(email);
        patient.setPhone(phone);  // Assuming setPhone accepts an Integer
        patient.setSex(sex);
        patient.setDateOfBirth(dateOfBirth);
        return patient;
    }

    public static Doctor generateRandomDoctor() {
        String[] POSSIBLE_NAMES = {"Alice", "Bob", "Charlie", "David", "Eve"};
        String[] POSSIBLE_SURNAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones"};
        String[] POSSIBLE_ADDRESSES = {
                "123 Main St, Anytown",
                "456 Oak Ave, Othercity",
                "789 Pine Rd, Sometown",
                "101 Elm Blvd, Bigtown",
                "202 Maple Ln, Smallville"
        };
        String EMAIL_DOMAIN = "@example.com";  // Fixed domain for email generation

        Random random = new Random();
        // Generate random values
        String name = POSSIBLE_NAMES[random.nextInt(POSSIBLE_NAMES.length)];
        String surname = POSSIBLE_SURNAMES[random.nextInt(POSSIBLE_SURNAMES.length)];
        String email = name.toLowerCase() + "." + surname.toLowerCase() + EMAIL_DOMAIN;
        int phone = 900000000 + random.nextInt(100000000);  // Random 10-digit number (1,000,000,000 to 1,999,999,999)
        String address = POSSIBLE_ADDRESSES[random.nextInt(POSSIBLE_ADDRESSES.length)];
        // Create and populate the Doctor object
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSurname(surname);
        doctor.setEmail(email);
        doctor.setPhone(phone);  // Assuming setPhone accepts an Integer
        doctor.setAddress(address);
        return doctor;
    }

    public static ArrayList<Report> generateRandomReports() {
        String[] POSSIBLE_SYMPTOMS = {"Fever", "Cough", "Headache", "Fatigue", "Nausea"};
        String[] POSSIBLE_STATES = {"Normal", "Abnormal", "Elevated"};
        Random random = new Random();
        ArrayList<Report> reports = new ArrayList<>();
        // Step 1: Generate a common date and create at least one Symptoms and one Signal with it
        String commonDate = generateRandomDate(random);  // Generate a random date to share
        Symptoms sharedSymptoms = new Symptoms();
            sharedSymptoms.setDate(commonDate);
        int numSymptoms = 1 + random.nextInt(4);  // 1 to 4 symptoms
            for (int i = 0; i < numSymptoms; i++) {
            sharedSymptoms.addSymptom(POSSIBLE_SYMPTOMS[random.nextInt(POSSIBLE_SYMPTOMS.length)]);
        }
            reports.add(sharedSymptoms);
        Signal sharedSignal = new Signal();
            sharedSignal.setDate(commonDate);  // Same date as sharedSymptoms
            sharedSignal.setComments("Random comments for shared signal");
            sharedSignal.setSamplingFrequency(50 + random.nextInt(151));  // 50 to 200
            sharedSignal.setECG(POSSIBLE_STATES[random.nextInt(POSSIBLE_STATES.length)]);
            sharedSignal.setACC(POSSIBLE_STATES[random.nextInt(POSSIBLE_STATES.length)]);
            sharedSignal.setTimeStamp("14:30:00");  // Fixed timestamp for simplicity, or you can randomize
            reports.add(sharedSignal);
        // Step 2: Generate additional reports with random dates
        int additionalReports = 3 + random.nextInt(6);  // 3 to 8 more reports
                for (int i = 0; i < additionalReports; i++) {
            if (random.nextBoolean()) {  // Randomly choose Symptoms
                Symptoms sym = new Symptoms();
                sym.setDate(generateRandomDate(random));  // Random date
                int numSym = 1 + random.nextInt(4);  // 1 to 4 symptoms
                for (int j = 0; j < numSym; j++) {
                    sym.addSymptom(POSSIBLE_SYMPTOMS[random.nextInt(POSSIBLE_SYMPTOMS.length)]);
                }
                reports.add(sym);
            } else {  // Randomly choose Signal
                Signal sig = new Signal();
                sig.setDate(generateRandomDate(random));  // Random date
                sig.setComments("Random comments " + i);
                sig.setSamplingFrequency(50 + random.nextInt(151));  // 50 to 200
                sig.setECG(POSSIBLE_STATES[random.nextInt(POSSIBLE_STATES.length)]);
                sig.setACC(POSSIBLE_STATES[random.nextInt(POSSIBLE_STATES.length)]);
                sig.setTimeStamp("12:00:00");  // Fixed, or randomize if needed
                reports.add(sig);
            }
        }
        return reports;  // Return the list of Reports
    }

    // Helper method to generate a random date in "YYYY-MM-DD" format
    private static String generateRandomDate(Random random) {
        int year = 2000 + random.nextInt(24);  // 2000 to 2023
        int month = 1 + random.nextInt(12);    // 1 to 12
        int day = 1 + random.nextInt(28);      // 1 to 28 to avoid invalid dates
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    public static void main(String[] args) {
        Patient randomPatient = ModelManager.generateRandomPatient();
        Doctor randomDoctor = ModelManager.generateRandomDoctor();
        System.out.println(randomPatient);
        System.out.println(randomDoctor);// Prints the generated Patient object

        ArrayList<Report> randomReports = generateRandomReports();
        for (Report report : randomReports) {
            System.out.println(report);  // Prints each report using its toString method
        }
    }
}
