package com.renturapp.scansist.Model;

public class Scan {

    public int scanID;
    public int clauseID;
    public String clauseCode;
    public String scanBarCode;
    public String scanDateTime;

    public Scan(int scanID, int clauseID, String clauseCode, String scanBarCode, String scanDateTime) {

        this.scanID = scanID;
        this.clauseID = clauseID;
        this.clauseCode = clauseCode;
        this.scanBarCode = scanBarCode;
        this.scanDateTime = scanDateTime;

    }

}
