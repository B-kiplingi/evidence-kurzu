package cz.uhk.ppro.akademietrutnov.domain.model;

public enum TermStatus {
    PLANNED("Plánovaný"),
    IN_PROGRESS("Probíhající"),
    COMPLETED("Ukončený"),
    CANCELLED("Zrušený");

    private final String displayName;

    TermStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
