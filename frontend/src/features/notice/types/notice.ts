export type NoticeCategory = '전체' | '공지' | '이벤트' | '결과' | '안내';

export interface Notice {
  id: number;
  category: Exclude<NoticeCategory, '전체'>;
  title: string;
  date: string;
  views: number;
  isPinned: boolean;
}
