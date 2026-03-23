import { useState, useMemo } from 'react';
import { Event } from '@/features/event/types';

export function useMarathonFilter(
  events: Event[],
  initialSearchDistance = { min: 0, max: 100 },
  initialSearchDate = {
    start: null as string | null,
    end: null as string | null,
  },
  initialSearchLocation = 'ALL',
  initialSearchTerm = '',
  initialSearchType = 'ALL',
) {
  const [searchDistance, setSearchDistance] = useState(initialSearchDistance);
  const [searchDate, setSearchDate] = useState(initialSearchDate);
  const [searchLocation, setSearchLocation] = useState(initialSearchLocation);
  const [searchTerm, setSearchTerm] = useState(initialSearchTerm);
  const [searchType, setSearchType] = useState(initialSearchType);

  const formatToLocalISO = (date: Date | string | null): string | null => {
    if (!date) return null;
    const d = new Date(date);

    // 유효하지 않은 날짜 체크
    if (isNaN(d.getTime())) return null;

    const pad = (n: number) => n.toString().padStart(2, '0');

    const year = d.getFullYear();
    const month = pad(d.getMonth() + 1);
    const day = pad(d.getDate());
    const hours = pad(d.getHours());
    const minutes = pad(d.getMinutes());
    const seconds = pad(d.getSeconds());

    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  };

  // 날짜 변경 시 Date 객체를 받아도 자동으로 포맷팅해주는 래퍼 함수
  const handleSetSearchDate = (range: {
    start: Date | string | null;
    end: Date | string | null;
  }) => {
    setSearchDate({
      start: formatToLocalISO(range.start),
      end: formatToLocalISO(range.end),
    });
  };

  const filteredEvents = useMemo(() => {
    return events.filter((event) => {
      // 지역 필터 (null, undefined, 'all', 빈 문자열일 경우 전체 허용)
      const isNoLocationFilter =
        !searchLocation ||
        searchLocation.toLocaleUpperCase() === 'ALL' ||
        searchLocation.trim() === '';

      const locationMatch = isNoLocationFilter
        ? true
        : event.venue
            ?.toLocaleUpperCase()
            .includes(searchLocation.trim().toLocaleUpperCase());

      // 검색어 필터 (null, 빈 문자열일 경우 전체 허용)
      const isNoSearchFilter = !searchTerm || searchTerm.trim() === '';
      const titleMatch = isNoSearchFilter
        ? true
        : event.title
            ?.toLocaleUpperCase()
            .includes(searchTerm.trim().toLocaleUpperCase());

      // 거리 필터 (searchDistance 자체가 null이거나 특정 조건일 때 처리)
      const isNoDistanceFilter = !searchDistance;
      const eventDistances = event.courses;
      // const distanceMatch = isNoDistanceFilter
      //   ? true
      //   : eventDistances.some(
      //       (d) =>
      //         d.distanceM / 1000 >= searchDistance.min &&
      //         d.distanceM / 1000 <= searchDistance.max,
      //     );

      // 날짜 필터 (start와 end가 모두 null이면 전체 허용)
      const eventDateStr = event.eventAt;

      const isNoDateFilter = !searchDate.start && !searchDate.end;
      const startDateMatch =
        !searchDate.start || eventDateStr >= searchDate.start;
      const endDateMatch = !searchDate.end || eventDateStr <= searchDate.end;

      const dateMatch = isNoDateFilter ? true : startDateMatch && endDateMatch;

      // 카테고리 필터
      const isNoCategoryFilter =
        !searchType ||
        searchType.toLocaleUpperCase() === 'ALL' ||
        searchType.trim() === '';

      const categoryMatch = isNoCategoryFilter
        ? true
        : event.type
            ?.toLocaleUpperCase()
            .includes(searchType.trim().toLocaleUpperCase());

      // 모든 조건을 통과해야 결과에 포함
      return (
        locationMatch &&
        titleMatch &&
        // distanceMatch &&
        dateMatch &&
        categoryMatch
      );
    });
  }, [
    events,
    searchTerm,
    searchLocation,
    searchDistance,
    searchDate,
    searchType,
  ]);

  return {
    searchLocation,
    searchTerm,
    searchDistance,
    searchDate,
    searchType,
    setSearchLocation,
    setSearchTerm,
    setSearchDistance,
    setSearchDate: handleSetSearchDate,
    setSearchType,
    filteredEvents,
  };
}
