export interface RaceHistory {
  id: number;
  date: string;
  event: string;
  course: 'Full' | 'Half' | '10K'; // 코스 타입 구체화
  time: string;
  pace: string;
  status: string;
}

export interface RunnerRecord {
  name: string;
  bib: string;
  totalRaces: number;
  bestRecord: string;
  averagePace: string;
  history: RaceHistory[];
}
