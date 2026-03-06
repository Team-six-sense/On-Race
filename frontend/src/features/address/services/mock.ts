import { wrapMockResponse } from '@/utils/api';
import { IAddressService } from './interface';
import { MOCK_ADDRESS, MOCK_ADDRESS_LIST } from '@/mockups';
import { AddressResponse } from '../types';

export const addressMock: IAddressService = {
  getAddress: async () => wrapMockResponse(MOCK_ADDRESS_LIST),
  getAddressById: async (id: string) => wrapMockResponse(MOCK_ADDRESS),
  createAddress: async (data: AddressResponse) => wrapMockResponse(data),
  updateAddress: async (id: string, data: AddressResponse) =>
    wrapMockResponse(data),
  deleteAddress: async (id: string) => wrapMockResponse(),
  updateDefaultAddress: async (id: string) => wrapMockResponse(),
};
