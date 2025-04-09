package com.example.cctv.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cctvdata")
public class Cctv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카메라 개수를 나타내는 필드
    @Column(name = "camera_count", nullable = false)
    private int cameraCount;

    // 위도
    @Column(nullable = false)
    private double latitude;

    // 경도
    @Column(nullable = false)
    private double longitude;

    // 기본 생성자
    public Cctv() {
    }

    // 생성자
    public Cctv(int cameraCount, double latitude, double longitude) {
        this.cameraCount = cameraCount;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public int getCameraCount() {
        return cameraCount;
    }

    public void setCameraCount(int cameraCount) {
        this.cameraCount = cameraCount;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
