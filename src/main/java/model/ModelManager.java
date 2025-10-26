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

    public static ArrayList<SignalRecording> generateRandomSignalRecordings() {
        Random random = new Random();
        ArrayList<SignalRecording> signalReports = new ArrayList<>();
        //ArrayList<Report> patientReports = new ArrayList<>();
        // Step 1: Generate a common date and create at least one Signal with it
        String commonDate = generateRandomDate(random);  // Generate a random date to share

        SignalRecording sharedReport = new SignalRecording();
            sharedReport.setDate(commonDate);  // Same date as sharedSymptoms
            sharedReport.setComments("Random comments for shared signal");
            sharedReport.setSamplingFrequency(50 + random.nextInt(151));  // 50 to 200
            signalReports.add(sharedReport);
        // Step 2: Generate additional reports with random dates
        int additionalReports = 3 + random.nextInt(6);  // 3 to 8 more reports

        for (int i = 0; i < additionalReports; i++) {
            SignalRecording sig = new SignalRecording();
            sig.setDate(generateRandomDate(random));  // Random date
            sig.setComments("Random comments " + i);
            sig.setSamplingFrequency(50 + random.nextInt(151));  // 50 to 200
            signalReports.add(sig);
        }
        return signalReports;  // Return the list of Reports*/
    }

    public static ArrayList<SymptomReport> generateRandomSymptomReports() {
        Random random = new Random();
        ArrayList<SymptomReport> signalReports = new ArrayList<>();
        //int numSymptoms = 1 + random.nextInt(15);  // 1 to 15 symptoms
        int numSymptoms = 15;

        for (int i = 0; i < numSymptoms; i++) {// Randomly choose Symptoms
            SymptomReport sym = new SymptomReport();
            sym.setDate(generateRandomDate(random));  // Random date
            sym.setSymptomType(SymptomType.values()[random.nextInt(SymptomType.values().length)]);
            signalReports.add(sym);
        }
        return signalReports;  // Return the list of Reports*/
    }

    // Helper method to generate a random date in "YYYY-MM-DD" format
    private static String generateRandomDate(Random random) {
        //int year = 2000 + random.nextInt(24);  // 2000 to 2023
        int year = 2025;
        int month = 1 + random.nextInt(12);    // 1 to 12
        int day = 1 + random.nextInt(28);      // 1 to 28 to avoid invalid dates
        return String.format("%02d/%02d/%04d", day, month, year);
    }

    public static void main(String[] args) {
        Patient randomPatient = ModelManager.generateRandomPatient();
        Doctor randomDoctor = ModelManager.generateRandomDoctor();
        System.out.println(randomPatient);
        System.out.println(randomDoctor);// Prints the generated Patient object

        /*ArrayList<Report> randomReports = generateRandomReports();
        for (Report report : randomReports) {
            System.out.println(report);  // Prints each report using its toString method
        }*/
    }
}
