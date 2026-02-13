export interface ResultProps {
  message: string;
  params: object;
  data?: any; // 데이터 목록 또는 단일 객체
  totalCount?: number;
  timestamp: Date;
}

// 이전에 정의한 TemplateProps를 각 도메인에 맞게 확장하거나 공통으로 사용합니다.
export interface BasePayloadProps {
  projectName: string;
  type: string;
  status: string;
  startedAt?: Date;
  endedAt?: Date;
  techStack?: string;
  description: string;
}
