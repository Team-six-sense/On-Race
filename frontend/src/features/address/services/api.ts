import axios from 'axios';
import { ApiResponse } from '@/types/api';
import { IAddressService } from './interface';
import { AddressResponse, AddressListResponse } from '../types';

// Next.js API Route를 호출하기 위한 인스턴스
const apiClient = axios.create({
  // 상대 경로를 사용하면 브라우저에서는 현재 도메인(localhost:3000 등)을 자동으로 사용합니다.
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const addressApi: IAddressService = {
  getAddress: async () => {
    const response =
      await apiClient.get<ApiResponse<AddressListResponse>>('/address');
    return response.data;
  },
  getAddressById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<AddressResponse>>(
      `/events`,
      {
        params: { id },
      },
    );
    return response.data;
  },
  createAddress: async (data: AddressResponse) => {
    const response = await apiClient.post<ApiResponse<AddressResponse>>(
      '/address',
      data,
    );
    return response.data;
  },
  updateAddress: async (id: number, data: AddressResponse) => {
    const response = await apiClient.put<ApiResponse<AddressResponse>>(
      `/address`,
      data,
      {
        params: { id },
      },
    );
    return response.data;
  },
  deleteAddress: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(
      `/address/${id}`,
      {
        params: { id },
      },
    );
    return response.data;
  },
  updateDefaultAddress: async (id: number) => {
    const response = await apiClient.patch<ApiResponse<void>>(`/address/${id}`);
    return response.data;
  },
};
