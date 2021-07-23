package com.renturapp.scansist.Model;

public class ScanModel {
    Integer gRowNum;
    public Integer rackID;
    Integer companyID;
    Integer rackNumber;
    Integer rackBayNumber;
    Integer rackShelfNumber;
    public String rackDescription;
    Boolean rackIsDeleted;
    public String rackBarcode;
    Integer getType;

    //Picklists
    public Integer pickListID;
    public String pickListDescription;
    public String pickListBarcode;
}
