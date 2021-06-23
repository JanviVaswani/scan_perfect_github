package com.renturapp.scansist;

public class PickList {

  int pickListID;
  //String pickListDatetime;
  //String pickListAirwayBill;
  String pickListDescription;
  //String pickListNotes;
  //int pickListStatus;

  //public PickList ( int pickListID, String pickListDatetime,String pickListAirwayBill,String pickListDescription, String pickListNotes, int pickListStatus) {
  public PickList ( int pickListID, String pickListDescription) {
    this.pickListID = pickListID;
    //this.pickListDatetime = pickListDatetime;
    //this.pickListAirwayBill = pickListAirwayBill;
    this.pickListDescription = pickListDescription;
    //this.pickListNotes = pickListNotes;
    //this.pickListStatus = pickListStatus;

  }

}
