package com.example.cctv.controller;

import com.example.cctv.entity.Cctv;
import com.example.cctv.repository.CctvRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cctv")
public class CctvController {

    private final CctvRepository cctvRepository;

    public CctvController(CctvRepository cctvRepository) {
        this.cctvRepository = cctvRepository;
    }

    // 전체 CCTV 데이터를 조회하는 엔드포인트
    @GetMapping
    public ResponseEntity<List<Cctv>> getAllCctv() {
        try {
            List<Cctv> cctvs = cctvRepository.findAll();
            return ResponseEntity.ok(cctvs);
        } catch (Exception e) {
            throw new RuntimeException("CCTV 데이터 조회에 실패하였습니다.", e);
        }
    }

    // 특정 ID의 CCTV 데이터를 조회하는 엔드포인트
    @GetMapping("/{id}")
    public ResponseEntity<Cctv> getCctvById(@PathVariable Long id) {
        try {
            Optional<Cctv> cctv = cctvRepository.findById(id);
            return cctv.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            throw new RuntimeException("ID로 CCTV 데이터 조회에 실패하였습니다.", e);
        }
    }

    // 새로운 CCTV 데이터를 생성하는 엔드포인트
    @PostMapping
    public ResponseEntity<Cctv> createCctv(@RequestBody Cctv cctv) {
        try {
            if (cctv == null) {
                throw new IllegalArgumentException("등록할 CCTV 데이터가 null입니다.");
            }
            // 추가적인 입력값 검증을 여기에 추가할 수 있습니다.
            Cctv savedCctv = cctvRepository.save(cctv);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCctv);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("CCTV 데이터 생성에 실패하였습니다.", e);
        }
    }

    // 기존의 CCTV 데이터를 업데이트하는 엔드포인트
    @PutMapping("/{id}")
    public ResponseEntity<Cctv> updateCctv(@PathVariable Long id, @RequestBody Cctv updatedCctv) {
        try {
            if (updatedCctv == null) {
                throw new IllegalArgumentException("업데이트할 CCTV 데이터가 null입니다.");
            }
            Optional<Cctv> optionalCctv = cctvRepository.findById(id);
            if (!optionalCctv.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Cctv cctv = optionalCctv.get();
            cctv.setCameraCount(updatedCctv.getCameraCount());
            cctv.setLatitude(updatedCctv.getLatitude());
            cctv.setLongitude(updatedCctv.getLongitude());
            Cctv savedCctv = cctvRepository.save(cctv);
            return ResponseEntity.ok(savedCctv);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("CCTV 데이터 업데이트에 실패하였습니다.", e);
        }
    }

    // 특정 CCTV 데이터를 삭제하는 엔드포인트
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCctv(@PathVariable Long id) {
        try {
            Optional<Cctv> optionalCctv = cctvRepository.findById(id);
            if (!optionalCctv.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            cctvRepository.delete(optionalCctv.get());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("CCTV 데이터 삭제에 실패하였습니다.", e);
        }
    }

    // 두 좌표 사이의 직선 경로 상에서 가장 가까운 CCTV n개를 반환하는 엔드포인트
    @GetMapping("/route")
    public ResponseEntity<List<Cctv>> getCctvOnRoute(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon,
            @RequestParam int n) {
        try {
            // 입력값 유효성 검사
            if (n <= 0) {
                throw new IllegalArgumentException("'n'은 1 이상의 값을 가져야 합니다.");
            }
            if (!isValidLatitude(startLat) || !isValidLatitude(endLat)) {
                throw new IllegalArgumentException("위도는 -90과 90 사이여야 합니다.");
            }
            if (!isValidLongitude(startLon) || !isValidLongitude(endLon)) {
                throw new IllegalArgumentException("경도는 -180과 180 사이여야 합니다.");
            }

            List<Cctv> allCctv = cctvRepository.findAll();
            List<CctvDistanceInfo> candidates = new ArrayList<>();

            for (Cctv c : allCctv) {
                // 시작점-도착점 직선 상에 대한 투영 비율 계산
                double t = getProjectionFactor(startLat, startLon, endLat, endLon, c.getLatitude(), c.getLongitude());
                // t 값을 0~1 사이로 클램핑하여 투영 위치 결정
                double clampedT = Math.max(0, Math.min(1, t));

                // 투영 위치 계산
                double projLat = startLat + clampedT * (endLat - startLat);
                double projLon = startLon + clampedT * (endLon - startLon);

                // CCTV와 투영 위치간의 수직 거리(Haversine 공식, 미터 단위)
                double perpendicularDistance = haversine(c.getLatitude(), c.getLongitude(), projLat, projLon);
                // 시작점부터 투영 위치까지의 거리(경로상의 순서를 위해)
                double distanceAlong = haversine(startLat, startLon, projLat, projLon);
                candidates.add(new CctvDistanceInfo(c, perpendicularDistance, distanceAlong));
            }

            // 수직 거리 기준, 그 후 경로 상의 순서를 위해 정렬
            candidates.sort(Comparator.comparingDouble(CctvDistanceInfo::getPerpendicularDistance)
                    .thenComparingDouble(CctvDistanceInfo::getDistanceAlong));

            List<Cctv> result = candidates.stream()
                    .limit(n)
                    .map(CctvDistanceInfo::getCctv)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("경로 기반 CCTV 데이터 조회에 실패하였습니다.", e);
        }
    }

    // 헬퍼 메서드: 위도 유효성 검사 (-90 ~ 90)
    private boolean isValidLatitude(double latitude) {
        return latitude >= -90 && latitude <= 90;
    }

    // 헬퍼 메서드: 경도 유효성 검사 (-180 ~ 180)
    private boolean isValidLongitude(double longitude) {
        return longitude >= -180 && longitude <= 180;
    }

    // 헬퍼 메서드: 점이 시작점과 도착점을 잇는 직선에 대해 어느 위치에 있는지 계산 (투영 비율 t)
    private double getProjectionFactor(double startLat, double startLon, double endLat, double endLon,
                                       double pointLat, double pointLon) {
        double dx = endLat - startLat;
        double dy = endLon - startLon;
        double denominator = dx * dx + dy * dy;
        if (denominator == 0) return 0; // 시작점과 도착점이 동일한 경우
        return ((pointLat - startLat) * dx + (pointLon - startLon) * dy) / denominator;
    }

    // 헬퍼 메서드: Haversine 공식을 사용하여 두 좌표간의 거리를 미터 단위로 계산
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 지구 반지름 (미터)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 내부 헬퍼 클래스: CCTV와 계산된 거리 정보를 함께 저장
    private static class CctvDistanceInfo {
        private Cctv cctv;
        private double perpendicularDistance;
        private double distanceAlong;

        public CctvDistanceInfo(Cctv cctv, double perpendicularDistance, double distanceAlong) {
            this.cctv = cctv;
            this.perpendicularDistance = perpendicularDistance;
            this.distanceAlong = distanceAlong;
        }

        public Cctv getCctv() {
            return cctv;
        }

        public double getPerpendicularDistance() {
            return perpendicularDistance;
        }

        public double getDistanceAlong() {
            return distanceAlong;
        }
    }
}
