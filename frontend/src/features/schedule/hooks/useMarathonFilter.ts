import { useState, useMemo } from 'react';
import { MarathonEvent } from '@/features/schedule/types/schedule';

export function useMarathonFilter(events: MarathonEvent[]) {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredEvents = useMemo(() => {
    return events.filter(
      (event) =>
        event.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        event.location.toLowerCase().includes(searchTerm.toLowerCase()),
    );
  }, [events, searchTerm]);

  return {
    searchTerm,
    setSearchTerm,
    filteredEvents,
  };
}
