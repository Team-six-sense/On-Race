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
export type StatusId = 'UPCOMING' | 'OPEN' | 'CLOSED';
export type TypeId = 'LOTTERY' | 'FIRST_COME';
export type CategoryId =
  | 'all'
  | 'MARATHON'
  | 'PLAY_RUN'
  | 'EXPERIENCE'
  | 'CLASS'
  | 'ETC';

// MAP
const STATUS_MAP: Record<string, string> = {
  UPCOMING: '오픈 대기',
  OPEN: '신청중',
  CLOSED: '모집 마감',
  RESULT: '모집 마감',
};

const TYPE_MAP: Record<string, string> = {
  LOTTERY: '응모',
  FIRST_COME: '선착',
};

const CATEGORY_MAP: Record<string, string> = {
  all: '전체보기',
  MARATHON: '마라톤',
  PLAY_RUN: '플레이 런',
  EXPERIENCE: '체험단',
  CLASS: '러닝클래스',
  ETC: '기타',
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

export const CATEGORIES = Object.entries(CATEGORY_MAP).map(([id, label]) => ({
  id,
  label,
}));
// 변환 함수 (Getter)
export const getStatusLabel = (id: string) => STATUS_MAP[id] || '미지정';
export const getTypeLabel = (id: string) => TYPE_MAP[id] || '미지정';
export const getCategoryLabel = (id: string) => CATEGORY_MAP[id] || '미지정';
