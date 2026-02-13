import { ResultProps } from '@/types/api';

export const wrapMockResponse = (data: any): ResultProps => ({
  message: 'Mock data success',
  params: {},
  data: data,
  totalCount: Array.isArray(data) ? data.length : 1,
  timestamp: new Date(),
});
