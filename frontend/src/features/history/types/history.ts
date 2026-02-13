export type ApplicationStatus = '결제완료' | '입금대기';

export interface RecordHistory {
  id: number;
  date: string;
  event: string;
  course: string;
  time: string;
  pace: string;
  status: string;
}

export interface RunnerRecord {
  name: string;
  bib: string;
  history: RecordHistory[];
}

export interface Application {
  id: string;
  event: string;
  date: string;
  course: string;
  status: ApplicationStatus;
  appliedAt: string;
  price: string;
  step: number;
}
