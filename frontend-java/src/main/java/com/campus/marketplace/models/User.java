package com.campus.marketplace.models;

import com.google.gson.annotations.SerializedName;

public class User {
    private int id;
    @SerializedName("student_id")
    private String studentId;
    @SerializedName("full_name")
    private String fullName;
    private String email;
    private String phone;
    private String department;
    @SerializedName("year_of_study")
    private int yearOfStudy;
    private String role; // "student", "admin"
    @SerializedName("verification_status")
    private String verificationStatus; // "pending", "approved", "rejected"
    @SerializedName("rejection_reason")
    private String rejectionReason;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId != null ? studentId : ""; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName != null ? fullName : ""; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email != null ? email : ""; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone != null ? phone : ""; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department != null ? department : ""; }
    public void setDepartment(String department) { this.department = department; }

    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public String getRole() { return role != null ? role : "student"; }
    public void setRole(String role) { this.role = role; }

    public String getVerificationStatus() { return verificationStatus != null ? verificationStatus : "pending"; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }
    public boolean isApproved() { return "approved".equalsIgnoreCase(verificationStatus); }
    public boolean isPending() { return "pending".equalsIgnoreCase(verificationStatus); }
    public boolean isRejected() { return "rejected".equalsIgnoreCase(verificationStatus); }

    @Override
    public String toString() {
        return fullName + " (" + email + ")";
    }
}
