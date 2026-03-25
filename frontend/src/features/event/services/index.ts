import { eventApi } from './api';
import { eventMock } from './mock';

const IS_MOCK = process.env.NEXT_PUBLIC_API_MODE === 'mock';

// 환경 변수에 따라 실제 사용할 객체를 내보냄
export const eventService = IS_MOCK ? eventMock : eventApi;
