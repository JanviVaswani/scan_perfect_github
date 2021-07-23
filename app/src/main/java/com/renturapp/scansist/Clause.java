package com.renturapp.scansist;

public class Clause {
    public int clauseID;
    String clauseCode;
    String clauseDescription;

    public Clause(int clauseID, String clauseCode, String clauseDescription) {
        this.clauseID = clauseID;
        this.clauseCode = clauseCode;
        this.clauseDescription = clauseDescription;
    }
}
