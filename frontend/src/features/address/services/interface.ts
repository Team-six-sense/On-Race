import { ApiResponse } from '@/types/api';
import { AddressResponse, AddressListResponse } from '../types';

export interface IAddressService {
  // 배송지 API
  getAddress(): Promise<ApiResponse<AddressListResponse>>;
  getAddressById(id: number): Promise<ApiResponse<AddressResponse>>;
  createAddress(data: AddressResponse): Promise<ApiResponse<AddressResponse>>;
  updateAddress(
    id: number,
    data: AddressResponse,
  ): Promise<ApiResponse<AddressResponse>>;
  deleteAddress(id: number): Promise<ApiResponse<any>>;
  updateDefaultAddress(id: number): Promise<ApiResponse<any>>;
}
