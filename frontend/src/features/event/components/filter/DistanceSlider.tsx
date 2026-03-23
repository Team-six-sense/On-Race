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

  // UI 업데이트 (검은색 막대 범위 조절)
  useEffect(() => {
    const minPercent = getPercent(minVal);
    const maxPercent = getPercent(maxVal);

    if (rangeRef.current) {
      rangeRef.current.style.left = `${minPercent}%`;
      rangeRef.current.style.width = `${maxPercent - minPercent}%`;
    }
  }, [minVal, maxVal, getPercent]);

  // 부모에게 데이터 전달 (Infinite Loop 방지 위해 onChange 제외)
  useEffect(() => {
    onChange({ min: minVal, max: maxVal });
  }, [minVal, maxVal]);

  return (
    <div className="flex items-start w-full pr-4 gap-2">
      {/* 좌측 레이블: 툴팁 박스(h-8)와 높이를 맞추기 위해 h-8 flex items-center 적용 */}
      <label className="text-sm font-bold shrink-0 w-10 h-8 flex items-center">
        거리
      </label>

      {/* 우측 슬라이더 및 툴팁 영역 */}
      <div className="flex-1 flex flex-col">
        {/* 툴팁이 돌아다닐 수 있는 영역 (Label과 같은 높이 h-8 확보) */}
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
              {minVal}km
            </div>
            {/* 아래 방향 화살표 */}
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
              {maxVal}km
            </div>
            <div className="w-0 h-0 border-l-[5px] border-l-transparent border-r-[5px] border-r-transparent border-t-[5px] border-t-black" />
          </div>
        </div>

        {/* 실제 슬라이더 트랙 영역 */}
        <div className="relative w-full h-6 flex items-center">
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

          {/* 시각적 배경 트랙 */}
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
