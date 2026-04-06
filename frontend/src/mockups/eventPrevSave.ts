import { EventPrevSave } from '@/features/event/types';

export const EVENT_PREV_SAVE: EventPrevSave = {
  id: 42,
  eventId: 1,
  status: 'PRE_SAVED',
  course: {
    id: 1,
    name: '풀코스',
    distanceM: 42195,
    price: 50000,
  },
  pace: {
    id: 3,
    name: '4시간 30분 페이스',
    hour: 4,
    minutes: 30,
  },
};
