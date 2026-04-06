import { wrapMockResponse } from '@/utils/api';
import { IOrderService } from './interface';

export const orderMock: IOrderService = {
  getOrder: async () => wrapMockResponse(),
  getOrderDetails: async (id) => wrapMockResponse(),
  postOrderConfirm: async (id) => wrapMockResponse(),
  postOrderCheckout: async (data) => wrapMockResponse(),
  postOrderCheckoutInfo: async (data) => wrapMockResponse(),
  postPaymentConfirm: async (data) => wrapMockResponse(),
};
