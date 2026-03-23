import { EventDetails } from '@/features/event/types';

export const EVENT_DETAILS: EventDetails = {
  id: 1,
  lotteryAnnouncedAt: '2026-03-15T10:00:00',
  notice: '참가자 유의사항...',
  courses: [
    {
      id: 1,
      name: '풀코스',
      mapUrl: '/images/courses/full-map.jpg',
      distanceMeter: 42195,
      price: 50000,
      courseCapacity: 500,
      paces: [
        { id: 1, name: '4시간 페이스', hour: 4, minutes: 0, capacity: 100 },
      ],
    },
  ],
  packages: [
    { id: 1, name: '기본 패키지', price: 0, description: '기본 참가 키트' },
  ],
  thumbnailImg: [
    { id: 1, type: 'THUMBNAIL', url: '/image/thumb1.jpg', sort: 1 },
  ],
  detailImg: [{ id: 3, type: 'DETAIL', url: '/image/detail.png', sort: 1 }],
};
