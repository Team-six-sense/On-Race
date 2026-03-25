// Design
export const STATES = [
  'Default',
  'Hovered',
  'Pressed',
  'Focused',
  'Disabled',
] as const;

export const SIZES = [
  { label: 'Large', value: 'lg' },
  { label: 'Default', value: 'default' },
  { label: 'Small', value: 'sm' },
] as const;

export type MenuType =
  | 'icon'
  | 'shadow'
  | 'button'
  | 'icon'
  | 'input'
  | 'checkbox'
  | 'radioGroup'
  | 'selectbox'
  | 'modal';
export type ButtonType = 'text' | 'icon' | 'fab';
export type StateType = (typeof STATES)[number];
export type SizeType = (typeof SIZES)[number]['value'];
export type RoundedSize = 'full' | 'none';

// ----------------

// 타입 정의
export type StatusId =
  | 'READY'
  | 'IN_PROGRESS'
  | 'CLOSING_SOON'
  | 'END'
  | 'DRAW_COMPLETED';
export type TypeId = 'LOTTERY' | 'FIRST_COME';
export type CategoryId =
  | 'ALL'
  | 'MARATHON'
  | 'PLAY_RUN'
  | 'EXPERIENCE'
  | 'CLASS'
  | 'ETC';

// MAP
const STATUS_MAP: Record<string, string> = {
  READY: '오픈 예정',
  IN_PROGRESS: '모집중',
  CLOSING_SOON: '마감 임박',
  END: '모집 마감',
  DRAW_COMPLETED: '결과',
};

const STATUS_CONFIG_MAP: Record<string, string> = {
  READY: 'bg-yellow-50 text-yellow-500',
  IN_PROGRESS: 'bg-blue-50 text-blue-500',
  CLOSING_SOON: 'bg-red-50 text-red-500',
  END: 'bg-gray-100 text-gray-400',
  DRAW_COMPLETED: 'bg-gray-100 text-gray-400',
};

const TYPE_MAP: Record<string, string> = {
  ALL: '전체보기',
  MARATHON: '마라톤',
  PLAY_RUN: '플레이 런',
  EXPERIENCE: '체험단',
  CLASS: '러닝클래스',
  ETC: '기타',
};

const APP_TYPE_MAP: Record<string, string> = {
  LOTTERY: '추첨',
  FIRST_COME: '선착순',
};

const DATE_FILTER_OPTIONS_MAP: Record<string, string> = {
  ALL: '전체',
  IN_ONE_MONTH: '1개월 이내',
  IN_THREE_MONTH: '3개월 이내',
  IN_SIX_MONTH: '6개월 이내',
};

// 상수 데이터
export const STATUS = Object.entries(STATUS_MAP).map(([id, label]) => ({
  id,
  label,
}));

export const TYPE = Object.entries(TYPE_MAP).map(([id, label]) => ({
  id,
  label,
}));

export const APP_TYPE = Object.entries(APP_TYPE_MAP).map(([id, label]) => ({
  id,
  label,
}));

export const DATE_FILTER_OPTIONS = Object.entries(DATE_FILTER_OPTIONS_MAP).map(
  ([id, label]) => ({
    id,
    label,
  }),
);

// 변환 함수 (Getter)
export const getStatusLabel = (id: string) => STATUS_MAP[id] || '미지정';
export const getStatusConfig = (id: string) => STATUS_CONFIG_MAP[id] || '';
export const getTypeLabel = (id: string) => TYPE_MAP[id] || '미지정';
export const getAppTypeLabel = (id: string) => APP_TYPE_MAP[id] || '미지정';
export const getDateFilterOption = (id: string) =>
  DATE_FILTER_OPTIONS_MAP[id] || '미지정';
