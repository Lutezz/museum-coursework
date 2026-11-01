package ua.edu.nupp.kpp.server;

public class Inspection {
    private Long id;
    private long exhibitId;
    private String inspectionDate;
    private double damageSeverity;

    public Inspection() {}
    public Inspection(Long id, long exhibitId, String inspectionDate, double damageSeverity) {
        this.id = id; this.exhibitId = exhibitId; this.inspectionDate = inspectionDate; this.damageSeverity = damageSeverity;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getExhibitId() { return exhibitId; }
    public void setExhibitId(long exhibitId) { this.exhibitId = exhibitId; }
    public String getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(String inspectionDate) { this.inspectionDate = inspectionDate; }
    public double getDamageSeverity() { return damageSeverity; }
    public void setDamageSeverity(double damageSeverity) { this.damageSeverity = damageSeverity; }
}
