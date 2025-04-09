package com.example.cctv.repository;

import com.example.cctv.entity.Cctv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CctvRepository extends JpaRepository<Cctv, Long> {
    // 추가적인 쿼리 메서드 정의 가능 (예: 위치별 검색 등)
}
