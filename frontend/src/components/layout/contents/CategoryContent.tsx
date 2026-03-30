'use client';

import { useRouter } from 'next/navigation';

export default function CategoryContent() {
  const router = useRouter();

  const categorys = [
    {
      id: 1,
      type: 'MARATHON',
      tag: 'Marathon',

      title: '마라톤',
      subtitle:
        '5km부터 42.195km 풀코스까지\n새로운 한계에 도전하는 지역 기반 마라톤',
      img: '/image/category/marathon.png',
      gridClass: 'md:col-span-2',
    },
    {
      id: 2,
      type: 'PLAY_RUN',
      tag: 'Play Run',

      title: '플레이 런',
      subtitle:
        '보물찾기, 경찰과 도둑처럼 특별한 테마를 더해\n놀이처럼 즐기는 미니 시티 런',
      img: '/image/category/playRun.jpg',
      gridClass: 'md:col-span-1',
    },
    {
      id: 3,
      type: 'EXPERIENCE',
      tag: 'Trial Crew',
      title: '체험단',
      subtitle:
        'On의 혁신이 담긴 신제품을\n실제 러닝 코스에서 체험해 볼 수 있는 기회',
      img: '/image/category/trialCrow.png',
      gridClass: 'md:col-span-1',
    },
    {
      id: 4,
      type: 'CLASS',
      tag: 'Running Class',
      title: '러닝 클래스',
      subtitle:
        '전문 트레이너와 인플루언서의 코칭을 통해\n달리는 즐거움을 배울 수 있는 그룹 클래스',
      img: '/image/category/runningClass.png',
      gridClass: 'md:col-span-2',
    },
  ];

  const handleCardClick = (type: string) => {
    // 쿼리 스트링 방식으로 이동 (?id=1)
    router.push(`/event?category=${type}`);
  };

  return (
    <section className="bg-white">
      <div className="max-w-[1440px] p-30 mx-auto w-full">
        {/* 상단 텍스트 영역 (중앙 정렬) */}
        <div className="flex flex-col mb-10">
          <div className="text-4xl md:text-5xl font-bold mb-6 text-gray-900">
            러닝을 경험하는 새로운 방식
          </div>
          <div className="text-lg whitespace-pre-line">
            온레이스에서는 총 4가지 카테고리의 러닝 이벤트가 개최됩니다.
            <br />
            나만의 스타일과 목적에 맞춰 달리고, 운동화 끈을 묶는 순간의 설렘을
            느껴보세요.
          </div>
        </div>

        {/* 그리드 레이아웃 (1열 2:1 / 2열 1:2) */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {categorys.map((category) => (
            <div
              key={category.id}
              onClick={() => handleCardClick(category.type)}
              className={`relative h-[480px] rounded-lg overflow-hidden shadow-lg cursor-pointer transition-transform hover:scale-[1.02] ${category.gridClass}`}
            >
              {/* 배경 이미지 */}
              <img
                src={category.img}
                alt={category.title}
                className="absolute inset-0 w-full h-full object-cover"
              />

              {/* 하단 콘텐츠 영역 */}
              <div className="absolute bottom-0 left-0 p-8 w-full bg-gradient-to-t from-black/60 to-transparent">
                <span className="inline-block text-base font-bold text-font-accent mb-1">
                  {category.tag}
                </span>
                <h3 className="text-3xl font-bold text-white mb-1">
                  {category.title}
                </h3>
                <p className="text-white text-sm font-medium whitespace-pre-line">
                  {category.subtitle}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
