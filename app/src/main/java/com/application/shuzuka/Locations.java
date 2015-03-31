package com.application.shuzuka;

/**
 * Created by dinesh on 27/3/15.
 */
public class Locations {

    private String  doctorName=" ";

    public String getDoctorClinicName() {
        return doctorClinicName;
    }

    public void setDoctorClinicName(String doctorClinicName) {
        this.doctorClinicName = doctorClinicName;
    }

    public Integer getTotalTokensCount() {
        return totalTokensCount;
    }

    public void setTotalTokensCount(Integer totalTokensCount) {
        this.totalTokensCount = totalTokensCount;
    }

    public Integer getTotalTokensCheckedIn() {
        return totalTokensCheckedIn;
    }

    public void setTotalTokensCheckedIn(Integer totalTokensCheckedIn) {
        this.totalTokensCheckedIn = totalTokensCheckedIn;
    }

    public Integer getDoctorClinicId() {
        return doctorClinicId;
    }

    public void setDoctorClinicId(Integer doctorClinicId) {
        this.doctorClinicId = doctorClinicId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    private String  doctorClinicName=" ";
    private Integer totalTokensCount=0;
    private Integer totalTokensCheckedIn=0;
    private Integer doctorClinicId=0;
    private Double lat;
    private Double lng;
    private String doctorClinicAddress="";

    public String getDoctorClinicAddress() {
        return doctorClinicAddress;
    }

    public void setDoctorClinicAddress(String doctorClinicAddress) {
        this.doctorClinicAddress = doctorClinicAddress;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setLatLng(double lat,double lng){
        this.lat = lat;
        this.lng = lng;
    }

}
