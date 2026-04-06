import { AddressList, Address } from '@/features/address/types';

export const MOCK_ADDRESS: Address = {
  id: 1,
  label: '우리집',
  receiverName: '김',
  phone: '010-1234-5678',
  zipcode: '06234',
  address1: '서울특별시 강남구 테헤란로 427',
  address2: '위워크 타워 10층',
  memo: '부재 시 문 앞에 놓아주세요.',
  isDefault: true,
};

// 주소 목록 Mock 데이터
export const MOCK_ADDRESS_LIST: AddressList = {
  data: [
    {
      id: 1,
      receiverName: '김',
      label: '우리집',
      phone: '010-1234-5678',
      zipcode: '06234',
      address1: '서울특별시 강남구 테헤란로 427',
      address2: '위워크 타워 10층',
      memo: '부재 시 문 앞에 놓아주세요.',
      isDefault: true,
    },
    {
      id: 2,
      receiverName: '이',
      label: '회사',
      phone: '010-9876-5432',
      zipcode: '04524',
      address1: '서울특별시 중구 세종대로 110',
      address2: '서울시청 3층',
      memo: '배송 전 연락 부탁드립니다.',
      isDefault: false,
    },
    {
      id: 3,
      receiverName: '박',
      label: '기타',
      phone: '010-5555-4444',
      zipcode: '48058',
      address1: '부산광역시 해운대구 수영강변대로 120',
      address2: '영화의전당 2층 관리실',
      memo: '경비실에 맡겨주세요.',
      isDefault: false,
    },
  ],
};
