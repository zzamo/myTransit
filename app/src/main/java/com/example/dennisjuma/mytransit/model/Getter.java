package com.example.dennisjuma.mytransit.model;

public class Getter {

    String route, full_names, reason, gender, phone, hname, placeAddress, placeId, postKey, email, fname, sname;
    long timestamp;

    public Getter() {
    }

    public Getter(String route, String full_names, String reason, String gender, String phone, String hname, String placeAddress, String placeId, String postKey, String email, String fname, String sname, long timestamp) {
        this.route = route;
        this.full_names = full_names;
        this.reason = reason;
        this.gender = gender;
        this.phone = phone;
        this.hname = hname;
        this.placeAddress = placeAddress;
        this.placeId = placeId;
        this.postKey = postKey;
        this.email = email;
        this.fname = fname;
        this.sname = sname;
        this.timestamp = timestamp;
    }

    public String getPhone() {
        return phone;
    }

    public String getRoute() {
        return route;
    }

    public String getEmail() {
        return email;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public String getPlaceId() {
        return placeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getGender() {
        return gender;
    }

    public String getHname() {
        return hname;
    }


    public String getFull_names() {
        return full_names;
    }

    public String getReason() {
        return reason;
    }

    public String getPostKey() {
        return postKey;
    }

    public String getFname() {
        return fname;
    }

    public String getSname() {
        return sname;
    }
}
