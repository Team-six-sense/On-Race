import { wrapMockResponse } from '@/utils/api';
import { IAddressService } from './interface';
import { MOCK_ADDRESS, MOCK_ADDRESS_LIST } from '@/mockups';

export const addressMock: IAddressService = {
  getAddress: async () => wrapMockResponse(MOCK_ADDRESS_LIST),
  getDefaultAddress: async () => wrapMockResponse(MOCK_ADDRESS),
  getAddressById: async (id) => wrapMockResponse(MOCK_ADDRESS),
  postAddress: async (data) => wrapMockResponse(MOCK_ADDRESS),
  updateAddress: async (id, data) => wrapMockResponse(MOCK_ADDRESS),
  deleteAddress: async (id) => wrapMockResponse(),
  updateDefaultAddress: async (id) => wrapMockResponse(),
};
