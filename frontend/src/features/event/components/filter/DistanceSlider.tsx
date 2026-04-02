'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';

interface Props {
  // min, max는 고정값으로 사용하므로 Props에서 제거하거나 초기값으로만 활용
  onChange: (values: { min: number; max: number }) => void;
}

const DistanceSlider = ({ onChange }: Props) => {
  const MIN_LIMIT = 0;
  const MAX_LIMIT = 43; // UI상의 최대 단계 (42 다음 단계)
  const ACTUAL_MAX = 42.195; // 실제 데이터용 최대값
  const STEP = 1;

  const [minVal, setMinVal] = useState(MIN_LIMIT);
  const [maxVal, setMaxVal] = useState(MAX_LIMIT);
  const rangeRef = useRef<HTMLDivElement>(null);

  // 입력된 숫자를 실제 거리 값으로 변환 (43 -> 42.195)
  const formatValue = (val: number) => {
    if (val >= 43) return ACTUAL_MAX;
    return val;
  };

  // 백분율 계산 함수 (UI 드로잉용)
  const getPercent = useCallback(
    (value: number) =>
      Math.round(((value - MIN_LIMIT) / (MAX_LIMIT - MIN_LIMIT)) * 100),
    [MIN_LIMIT, MAX_LIMIT],
  );

  // UI 업데이트 (검은색 막대 범위 조절)
  useEffect(() => {
    const minPercent = getPercent(minVal);
    const maxPercent = getPercent(maxVal);

    if (rangeRef.current) {
      rangeRef.current.style.left = `${minPercent}%`;
      rangeRef.current.style.width = `${maxPercent - minPercent}%`;
    }
  }, [minVal, maxVal, getPercent]);

  // 부모에게 데이터 전달
  useEffect(() => {
    onChange({
      min: formatValue(minVal),
      max: formatValue(maxVal),
    });
  }, [minVal, maxVal]);

  return (
    <div className="flex items-start w-full pr-4 gap-2">
      <label className="text-sm font-bold shrink-0 w-10 h-8 flex items-center">
        거리
      </label>

      <div className="flex-1 flex flex-col">
        {/* 툴팁 영역 */}
        <div className="relative w-full h-8 pointer-events-none">
          {/* 최소값 툴팁 */}
          <div
            className="absolute flex flex-col items-center bottom-0"
            style={{
              left: `calc(${getPercent(minVal)}% + ${12 - getPercent(minVal) * 0.24}px)`,
              transform: 'translateX(-50%)',
            }}
          >
            <div className="bg-black text-white text-[11px] font-bold px-2 py-1 rounded shadow-sm whitespace-nowrap">
              {formatValue(minVal)}km
            </div>
            <div className="w-0 h-0 border-l-[5px] border-l-transparent border-r-[5px] border-r-transparent border-t-[5px] border-t-black" />
          </div>

          {/* 최대값 툴팁 */}
          <div
            className="absolute flex flex-col items-center bottom-0"
            style={{
              left: `calc(${getPercent(maxVal)}% + ${12 - getPercent(maxVal) * 0.24}px)`,
              transform: 'translateX(-50%)',
            }}
          >
            <div className="bg-black text-white text-[11px] font-bold px-2 py-1 rounded shadow-sm whitespace-nowrap">
              {formatValue(maxVal)}km
            </div>
            <div className="w-0 h-0 border-l-[5px] border-l-transparent border-r-[5px] border-r-transparent border-t-[5px] border-t-black" />
          </div>
        </div>

        {/* 실제 슬라이더 트랙 */}
        <div className="relative w-full h-6 flex items-center">
          <input
            type="range"
            min={MIN_LIMIT}
            max={MAX_LIMIT}
            step={STEP}
            value={minVal}
            onChange={(e) => {
              const value = Number(e.target.value);
              setMinVal(Math.min(value, maxVal - STEP));
            }}
            className="absolute w-full h-0 pointer-events-none appearance-none z-[30] outline-none 
                       [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-6 [&::-webkit-slider-thumb]:h-6 
                       [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-black 
                       [&::-webkit-slider-thumb]:pointer-events-auto [&::-webkit-slider-thumb]:cursor-pointer
                       [&::-moz-range-thumb]:w-6 [&::-moz-range-thumb]:h-6 [&::-moz-range-thumb]:border-none 
                       [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:bg-black"
            style={{ zIndex: minVal > MAX_LIMIT - 5 ? 35 : undefined }}
          />
          <input
            type="range"
            min={MIN_LIMIT}
            max={MAX_LIMIT}
            step={STEP}
            value={maxVal}
            onChange={(e) => {
              const value = Number(e.target.value);
              setMaxVal(Math.max(value, minVal + STEP));
            }}
            className="absolute w-full h-0 pointer-events-none appearance-none z-[31] outline-none 
                       [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-6 [&::-webkit-slider-thumb]:h-6 
                       [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-black 
                       [&::-webkit-slider-thumb]:pointer-events-auto [&::-webkit-slider-thumb]:cursor-pointer
                       [&::-moz-range-thumb]:w-6 [&::-moz-range-thumb]:h-6 [&::-moz-range-thumb]:border-none 
                       [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:bg-black"
          />

          <div className="relative w-full h-1.5 bg-gray-200 rounded-full overflow-hidden">
            <div
              ref={rangeRef}
              className="absolute h-full bg-black rounded-full"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default DistanceSlider;
