import { EventOverview } from '@/features/event/types';

export const EVENT_OVERVIEW: EventOverview = {
  hasEntry: true,
  entry: {
    id: 1,
    status: 'PRE_SAVED',
    selectedCourseId: 18,
    selectedPaceId: 5,
  },
  courses: [
    {
      id: 18,
      name: '5km 성곽런',
      distanceM: 5000,
      price: 25000,
      paces: [
        {
          id: 5,
          name: '5시간 페이스',
          hour: 5,
          minutes: 0,
          capacity: 100,
        },
      ],
    },
  ],
  rateInfo: {
    entryCount: 85,
    capacity: 100,
    competitionRate: 85,
    fillRatePercent: 30,
    price: 25000,
  },
};
