'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';

interface Props {
  min: number;
  max: number;
  step?: number;
  onChange: (values: { min: number; max: number }) => void;
}

const DistanceSlider = ({ min, max, step = 1, onChange }: Props) => {
  const [minVal, setMinVal] = useState(min);
  const [maxVal, setMaxVal] = useState(max);
  const rangeRef = useRef<HTMLDivElement>(null);

  // 백분율 계산 함수
  const getPercent = useCallback(
    (value: number) => Math.round(((value - min) / (max - min)) * 100),
    [min, max],
  );

  // 1. UI 업데이트 (검은색 막대 범위 조절)
  useEffect(() => {
    const minPercent = getPercent(minVal);
    const maxPercent = getPercent(maxVal);

    if (rangeRef.current) {
      rangeRef.current.style.left = `${minPercent}%`;
      rangeRef.current.style.width = `${maxPercent - minPercent}%`;
    }
  }, [minVal, maxVal, getPercent]);

  // 2. 부모에게 데이터 전달 (Infinite Loop 방지 위해 onChange 제외)
  useEffect(() => {
    onChange({ min: minVal, max: maxVal });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [minVal, maxVal]);

  return (
    <div className="relative w-full h-10 flex items-center justify-center">
      {/* 실제 Input들 (투명 핸들) */}
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={minVal}
        onChange={(e) =>
          setMinVal(Math.min(Number(e.target.value), maxVal - step))
        }
        className="absolute w-full h-0 pointer-events-none appearance-none z-[30] outline-none 
                   [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-6 [&::-webkit-slider-thumb]:h-6 
                   [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-black 
                   [&::-webkit-slider-thumb]:pointer-events-auto [&::-webkit-slider-thumb]:cursor-pointer
                   [&::-moz-range-thumb]:w-6 [&::-moz-range-thumb]:h-6 [&::-moz-range-thumb]:border-none 
                   [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:bg-black"
        style={{ zIndex: minVal > max - 100 ? 35 : undefined }}
      />
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={maxVal}
        onChange={(e) =>
          setMaxVal(Math.max(Number(e.target.value), minVal + step))
        }
        className="absolute w-full h-0 pointer-events-none appearance-none z-[31] outline-none 
                   [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-6 [&::-webkit-slider-thumb]:h-6 
                   [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-black 
                   [&::-webkit-slider-thumb]:pointer-events-auto [&::-webkit-slider-thumb]:cursor-pointer
                   [&::-moz-range-thumb]:w-6 [&::-moz-range-thumb]:h-6 [&::-moz-range-thumb]:border-none 
                   [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:bg-black"
      />

      {/* 시각적 레이어 */}
      <div className="relative w-full h-1.5 bg-gray-200 rounded-full">
        {/* 선택된 범위 하이라이트 (Black) */}
        <div ref={rangeRef} className="absolute h-full bg-black rounded-full" />

        {/* 말풍선 컨테이너 (정중앙 정렬 보정) */}
        <div className="absolute w-full h-full top-0 left-0 pointer-events-none">
          {/* 최소값 말풍선 */}
          <div
            className="absolute flex flex-col items-center"
            style={{
              left: `calc(${getPercent(minVal)}% + ${12 - getPercent(minVal) * 0.24}px)`,
              transform: 'translateX(-50%)',
              top: '24px',
            }}
          >
            <div className="w-0 h-0 border-l-[5px] border-l-transparent border-r-[5px] border-r-transparent border-b-[5px] border-b-black" />
            <div className="bg-black text-white text-[11px] font-bold px-2 py-1 rounded shadow-sm whitespace-nowrap">
              {minVal}km
            </div>
          </div>

          {/* 최대값 말풍선 */}
          <div
            className="absolute flex flex-col items-center"
            style={{
              left: `calc(${getPercent(maxVal)}% + ${12 - getPercent(maxVal) * 0.24}px)`,
              transform: 'translateX(-50%)',
              top: '24px',
            }}
          >
            <div className="w-0 h-0 border-l-[5px] border-l-transparent border-r-[5px] border-r-transparent border-b-[5px] border-b-black" />
            <div className="bg-black text-white text-[11px] font-bold px-2 py-1 rounded shadow-sm whitespace-nowrap">
              {maxVal}km
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DistanceSlider;
