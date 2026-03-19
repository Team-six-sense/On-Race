import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface SignupState {
  name: string;
  email: string;
  password: string;
  phoneNumber: string;
  setSignupData: (
    name: string,
    email: string,
    password: string,
    phoneNumber: string,
  ) => void;
  resetSignupData: () => void;
}

export const useSignupStore = create<SignupState>()(
  persist(
    (set) => ({
      name: '',
      email: '',
      password: '',
      phoneNumber: '',

      // 이제 일반 string 타입의 인자를 받을 수 있습니다.
      setSignupData: (name, email, password, phoneNumber) =>
        set({ name, email, password, phoneNumber }),

      resetSignupData: () =>
        set({ name: '', email: '', password: '', phoneNumber: '' }),
    }),
    {
      name: 'signup-storage',
      storage: createJSONStorage(() => sessionStorage),
    },
  ),
);
