export type EventStatus = '응모중' | '신청중' | '응모대기' | '대기중';

export interface MarathonEvent {
  id: number;
  thumbnail: string;
  title: string;
  date: string; // YYYY.MM.DD
  location: string;
  courses: string[];
  status: EventStatus;
}
