package ua.edu.nupp.kpp.server;

public class Specialist {
    private Long id;
    private String fullName;
    private String specialization;

    public Specialist() {}
    public Specialist(Long id, String fullName, String specialization) {
        this.id = id; this.fullName = fullName; this.specialization = specialization;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
