// constants.ts
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
