/**
 * 코스 정보 인터페이스
 */
export interface Course {
  id: number;
  name: string;
  distanceM: number;
  price: number;
}

/**
 * 마라톤 이벤트 상세 인터페이스
 
 상태
  - 오픈 대기: UPCOMING
  - 신청중: OPEN
  - 신청 마감: CLOSED
 타입
  - LOTTERY: 응모
  - FIRST_COME: 선착
 카테고리
  - 마라톤: 'MARATHON',
  - 플레이 런: 'PLAY_RUN',
  - 체험단: 'EXPERIENCE',
  - 러닝 클래스: 'CLASS',
  - 기타: 'ETC',
 */
export interface MarathonEvent {
  id: number;
  title: string;
  status: 'UPCOMING' | 'OPEN' | 'CLOSED' | 'RESULT'; // API 명세에 따른 상태값들
  type: 'LOTTERY' | 'FIRST_COME'; // 응모 방식
  thumbnailImg: string;
  eventAt: string; // ISO 8601
  appStartAt: string; // ISO 8601
  appEndAt: string; // ISO 8601
  venueAddress: string;
  courses: Course[];

  // 임시로 추가
  resultAt: string; // ISO 8601
  category: 'MARATHON' | 'PLAY_RUN' | 'EXPERIENCE' | 'CLASS' | 'ETC';
}

/**
 * API의 'data' 필드에 들어갈 페이징 구조
 */
export interface MarathonList {
  content: MarathonEvent[];
  nextCursor: number | null;
  hasNext: boolean;
}
