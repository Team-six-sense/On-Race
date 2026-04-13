import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { Event, EventDetails, SalesInfo } from '../types';

interface EventState {
  event: Event | null;
  eventDetails: EventDetails | null;
  eventSaleInfo: SalesInfo | null;
  course: string;
  pace: string;

  selectedBasicOption: string;
  selectedOptions: string[];
  setEvent: (event: Event | null) => void;
  setEventDetails: (eventDetails: EventDetails | null) => void;
  setEventSaleInfo: (eventSaleInfo: SalesInfo | null) => void;
  setCourse: (course: string) => void;
  setPace: (pace: string) => void;
  setSelectedBasicOption: (options: string) => void;
  setSelectedOptions: (options: string[]) => void;
  resetEvent: () => void;
  resetEventDetails: () => void;
}

export const useEventStore = create<EventState>()(
  persist(
    (set) => ({
      event: null,
      eventDetails: null,
      eventSaleInfo: null,
      course: '',
      pace: '',
      selectedBasicOption: '',
      selectedOptions: [],
      setEvent: (event) => set({ event }),
      setEventDetails: (eventDetails) => set({ eventDetails }),
      setEventSaleInfo: (eventSaleInfo) => set({ eventSaleInfo }),
      resetEvent: () => set({ event: null }),
      resetEventDetails: () => set({ eventDetails: null }),
      setCourse: (course) => set({ course }),
      setPace: (pace) => set({ pace }),
      setSelectedBasicOption: (selectedBasicOption) =>
        set({ selectedBasicOption }),
      setSelectedOptions: (options) => set({ selectedOptions: options }),
    }),
    {
      name: 'event-storage',
      storage: createJSONStorage(() => sessionStorage),
    },
  ),
);
