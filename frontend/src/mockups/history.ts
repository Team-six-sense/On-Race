import { RunnerRecord, Application } from '@/features/history/types/history';

export const MOCK_RECORD_DATA: RunnerRecord = {
  name: '홍길동',
  bib: '12345',
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
  ],
};

export const MOCK_APPLICATIONS: Application[] = [
  {
    id: 'APP-2024-001',
    event: '2024 서울 마라톤 (동아)',
    date: '2024.03.17',
    course: 'Full',
    status: '결제완료',
    appliedAt: '2024.01.10',
    price: '100,000원',
    step: 100,
  },
  {
    id: 'APP-2024-002',
    event: '2024 경기 하프 마라톤',
    date: '2024.05.12',
    course: 'Half',
    status: '입금대기',
    appliedAt: '2024.02.01',
    price: '50,000원',
    step: 30,
  },
];
