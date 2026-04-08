import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface SignupState {
  email: string;
  password: string;
  phoneNumber: string;
  setSignupData: (email: string, password: string, phoneNumber: string) => void;
  resetSignupData: () => void;
}

export const useSignupStore = create<SignupState>()(
  persist(
    (set) => ({
      email: '',
      password: '',
      phoneNumber: '',

      // 이제 일반 string 타입의 인자를 받을 수 있습니다.
      setSignupData: (email, password, phoneNumber) =>
        set({ email, password, phoneNumber }),

      resetSignupData: () => set({ email: '', password: '', phoneNumber: '' }),
    }),
    {
      name: 'signup-storage',
      storage: createJSONStorage(() => sessionStorage),
    },
  ),
);
