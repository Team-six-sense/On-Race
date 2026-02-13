import { RunnerRecord } from '@/features/records/types/records';

export const MOCK_RUNNER_RECORD: RunnerRecord = {
  name: '홍길동',
  bib: '12345',
  totalRaces: 12,
  bestRecord: '03:15:22',
  averagePace: '4\'38"',
  history: [
    {
      id: 1,
      date: '2023.11.05',
      event: 'JTBC 서울 마라톤',
      course: 'Full',
      time: '03:15:22',
      pace: '4\'38"',
      status: '완주',
    },
    {
      id: 2,
      date: '2023.10.15',
      event: '서울 레이스',
      course: 'Half',
      time: '01:32:10',
      pace: '4\'22"',
      status: '완주',
    },
    {
      id: 3,
      date: '2023.03.19',
      event: '서울 마라톤 (동아)',
      course: 'Full',
      time: '03:28:45',
      pace: '4\'56"',
      status: '완주',
    },
    {
      id: 4,
      date: '2022.10.30',
      event: '춘천 마라톤',
      course: 'Full',
      time: '03:45:12',
      pace: '5\'20"',
      status: '완주',
    },
  ],
};
