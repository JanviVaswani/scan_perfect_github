package com.renturapp.scansist;

public class Scan {

    int scanID;
    int clauseID;
    String clauseCode;
    String scanBarCode;
    String scanDateTime;

    public Scan(int scanID,int clauseID,String clauseCode,String scanBarCode, String scanDateTime) {

        this.scanID = scanID;
        this.clauseID = clauseID;
        this.clauseCode = clauseCode;
        this.scanBarCode = scanBarCode;
        this.scanDateTime = scanDateTime;

    }

}
