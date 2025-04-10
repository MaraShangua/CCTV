# CCTV 백엔드 서버

이 프로젝트는 **Java + Spring Boot + MySQL** 기반의 CCTV 관리용 백엔드 서버입니다.  
CRUD 기능 외에도, **두 좌표 사이의 직선 경로에 가까운 CCTV n개를 반환하는 위치 기반 검색 기능**을 제공합니다.

---

## 기술 스택

- Java 11
- Spring Boot
- Spring Data JPA
- MySQL
- Maven
- IntelliJ IDEA
- Docker, Docker Compose

---

## 데이터 모델

| 필드명         | 타입     | 설명                        |
|----------------|----------|-----------------------------|
| `id`           | Long     | CCTV 고유 식별자 (자동 생성) |
| `cameraCount`  | Integer  | 카메라 개수                 |
| `latitude`     | Double   | 위도 (-90 ~ 90)             |
| `longitude`    | Double   | 경도 (-180 ~ 180)           |

---

# API 명세서

## GET /api/cctv
전체 CCTV 데이터 조회

### 응답 (200 OK)
```json
[
  {
    "id": 1,
    "cameraCount": 4,
    "latitude": 37.5665,
    "longitude": 126.9780
  }
]
```

## GET /api/cctv/{id}
ID로 CCTV 데이터 조회

### 응답 (200 OK)
```json
{
  "id": 1,
  "cameraCount": 4,
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

### 오류 응답 (404 또는 500)
```json
{
  "error": "Internal Server Error",
  "message": "ID로 CCTV 데이터 조회에 실패하였습니다."
}
```

## POST /api/cctv
새로운 CCTV 데이터 등록

### 요청 예시
```json
{
  "cameraCount": 3,
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

### 응답 (201 Created)
```json
{
  "id": 2,
  "cameraCount": 3,
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

### 오류 응답 (400 또는 500)
```json
{
  "error": "Bad Request",
  "message": "등록할 CCTV 데이터가 null입니다."
}
```

## PUT /api/cctv/{id}
CCTV 데이터 수정

### 요청 예시
```json
{
  "cameraCount": 5,
  "latitude": 37.5678,
  "longitude": 126.9800
}
```

### 응답 (200 OK)
```json
{
  "id": 2,
  "cameraCount": 5,
  "latitude": 37.5678,
  "longitude": 126.9800
}
```

### 오류 응답
```json
{
  "error": "Bad Request",
  "message": "업데이트할 CCTV 데이터가 null입니다."
}
```

## DELETE /api/cctv/{id}
CCTV 데이터 삭제

### 응답 (200 OK)
본문 없음

### 오류 응답 (500)
```json
{
  "error": "Internal Server Error",
  "message": "CCTV 데이터 삭제에 실패하였습니다."
}
```

## GET /api/cctv/route
시작점과 도착점 좌표를 기준으로 직선 경로에 가까운 CCTV n개 반환

### 요청 예시
```
GET /api/cctv/route?startLat=37.5665&startLon=126.9780&endLat=37.5700&endLon=126.9820&n=3
```

### 응답 (200 OK)
```json
[
  {
    "id": 3,
    "cameraCount": 2,
    "latitude": 37.5671,
    "longitude": 126.9789
  },
  {
    "id": 4,
    "cameraCount": 1,
    "latitude": 37.5679,
    "longitude": 126.9794
  }
]
```

### 오류 응답 (400)
```json
{
  "error": "Bad Request",
  "message": "'n'은 1 이상의 값을 가져야 합니다."
}
```

## 전역 예외 응답 포맷

### 잘못된 요청 (HTTP 400)
```json
{
  "error": "Bad Request",
  "message": "위도는 -90과 90 사이여야 합니다."
}
```

### 서버 내부 오류 (HTTP 500)
```json
{
  "error": "Internal Server Error",
  "message": "예기치 못한 오류가 발생하였습니다."
}
```
