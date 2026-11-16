package cz.uhk.ppro.akademietrutnov.domain.model;

public enum RegistrationStatus {
    CONFIRMED("Potvrzeno"),
    WAITLISTED("V pořadníku"),
    CANCELLED("Zrušeno"),
    COMPLETED("Úspěšně dokončeno"),
    FAILED("Nedokončeno");

    private final String displayName;

    RegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
