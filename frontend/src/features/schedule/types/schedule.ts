export type EventStatus = '응모중' | '신청중' | '응모대기' | '대기중';

export interface MarathonEvent {
  id: number;
  title: string;
  thumbnail: string;
  status: EventStatus;
  location: string;
  month: string;
  day: string;
  date: string; // YYYY.MM.DD
  courses: string[];
}
