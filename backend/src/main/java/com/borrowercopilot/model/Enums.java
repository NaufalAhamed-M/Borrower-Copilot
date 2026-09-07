package com.borrowercopilot.model;

public final class Enums {
    private Enums() {}

    public enum LoanType {
        PERSONAL,
        HOME,
        LAP,
        GOLD,
        TWO_WHEELER,
        BUSINESS
    }
    public enum IncomeType {
        SALARIED,
        SELF_EMPLOYED,
        INFORMAL_GIG
    }
    public enum IncomeStability {
        STABLE,
        VARIABLE,
        HIGHLY_VARIABLE,
        UNKNOWN
    }
    public enum Decision {
        BORROW,
        BORROW_LESS,
        DONT_BORROW
    }
    public enum Confidence {
        HIGH,
        MEDIUM,
        LOW
    }
}
