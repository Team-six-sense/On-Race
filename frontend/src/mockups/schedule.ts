import { MarathonEvent } from '@/features/schedule/types/schedule';

export const MARATHON_DATA: MarathonEvent[] = [
  {
    id: 1,
    title: '서울 마라톤 대회 2026',
    thumbnail: '',
    status: '응모중',
    location: '서울',
    courses: ['Full', 'Half', '10km'],
    date: '2026.04.15',
  },
  {
    id: 2,
    title: '부산 바다 러닝 페스티벌',
    thumbnail: '',
    status: '신청중',
    location: '부산',
    courses: ['Half', '10km', '5km'],
    date: '2026.05.20',
  },
  {
    id: 3,
    title: '제주 올레길 마라톤',
    thumbnail: '',
    status: '응모대기',
    location: '제주',
    courses: ['Full', 'Half'],
    date: '2026.06.10',
  },
  {
    id: 4,
    title: '대구 컬러런',
    thumbnail: '',
    status: '대기중',
    location: '대구',
    courses: ['5Km', '10km'],
    date: '2026.07.05',
  },
];
