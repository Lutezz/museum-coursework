package ua.edu.nupp.kpp.client;

public class Exhibit {
    private Long id;
    private String inventoryNumber;
    private String name;
    private String material;
    private double materialSensitivity;
    private double exposureSignificance;

    public Exhibit() {}
    public Exhibit(Long id, String inventoryNumber, String name, String material, double materialSensitivity, double exposureSignificance) {
        this.id = id; this.inventoryNumber = inventoryNumber; this.name = name; this.material = material; this.materialSensitivity = materialSensitivity; this.exposureSignificance = exposureSignificance;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public double getMaterialSensitivity() { return materialSensitivity; }
    public void setMaterialSensitivity(double materialSensitivity) { this.materialSensitivity = materialSensitivity; }
    public double getExposureSignificance() { return exposureSignificance; }
    public void setExposureSignificance(double exposureSignificance) { this.exposureSignificance = exposureSignificance; }
}
