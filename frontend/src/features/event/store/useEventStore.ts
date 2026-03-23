import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { Event } from '../types';

interface EventState {
  event: Event | null;

  setEvent: (event: Event | null) => void;
  resetEvent: () => void;
}

export const useEventStore = create<EventState>()(
  persist(
    (set) => ({
      event: null,
      setEvent: (event) => set({ event }),
      resetEvent: () => set({ event: null }),
    }),
    {
      name: 'event-storage',
      storage: createJSONStorage(() => sessionStorage),
    },
  ),
);
