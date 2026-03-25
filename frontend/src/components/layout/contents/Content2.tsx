import Image from 'next/image';

export default function Content2() {
  return (
    <section className="py-4">
      {/* 카드 컨테이너: 가로로 긴 형태 (h-[320px] ~ h-[400px]) */}
      <div className="relative flex h-[300px] max-w-[1200px] mx-auto bg-[#B8F023] ">
        {/* 이미지 영역 (전체 배경으로 깔기) */}
        <div className="absolute right-0 top-0 w-full md:w-3/5 h-full">
          <Image
            src="/image/platform2/banner.jpg"
            alt="OnRace Running"
            fill
            className="object-cover object-center"
            priority
          />
          {/* 이미지 위에 덮이는 그라데이션: 왼쪽 배경색에서 오른쪽 투명으로 */}
          <div className="absolute inset-0 bg-gradient-to-r from-[#B8F023] via-[#B8F023]/10 to-transparent" />
        </div>

        {/* 텍스트 콘텐츠 영역 */}
        <div className="relative z-10 w-full md:w-3/5 p-8 md:p-14 flex flex-col justify-center">
          {/* 태그 */}
          <div className="inline-block w-fit mb-2">
            <span className="text-xs md:text-sm font-bold ">Coming Soon</span>
          </div>

          {/* 메인 타이틀 */}
          <h2 className="text-xl font-bold text-black mb-2">
            함께 달릴 준비 되셨나요?
          </h2>

          {/* 상세 설명 */}
          <div className="text-black text-base mb-2">
            <p className="mb-1">
              같은 코스를 달린 동료를 찾고 내 기록을 공유할 수 있는 온레이스
              커뮤니티가 곧 찾아옵니다.
            </p>
            <p> 새롭게 추가될 신규 기능을 기대해 주세요.</p>
          </div>
        </div>
      </div>
    </section>
  );
}
