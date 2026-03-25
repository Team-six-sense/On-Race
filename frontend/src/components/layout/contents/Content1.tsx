'use client';

export default function Content1() {
  const platformContents = [
    {
      id: 1,
      tag: 'Marathon',
      title: '마라톤',
      subtitle:
        '5km부터 42.195km 풀코스까지\n새로운 한계에 도전하는 지역 기반 마라톤',
      img: '/image/platform2/contents1.png',
      gridClass: 'md:col-span-2',
    },
    {
      id: 2,
      tag: 'Play Run',
      title: '플레이 런',
      subtitle:
        '보물찾기, 경찰과 도둑처럼 특별한 테마를 더해\n놀이처럼 즐기는 미니 시티 런',
      img: '/image/platform2/contents2.jpg',
      gridClass: 'md:col-span-1',
    },
    {
      id: 3,
      tag: 'Trial Crew',
      title: '체험단',
      subtitle:
        'On의 혁신이 담긴 신제품을\n실제 러닝 코스에서 체험해 볼 수 있는 기회',
      img: '/image/platform2/contents3.png',
      gridClass: 'md:col-span-1',
    },
    {
      id: 4,
      tag: 'Runnin Class',
      title: '러닝 클래스',
      subtitle:
        '전문 트레이너와 인플루언서의 코칭을 통해\n달리는 즐거움을 배울 수 있는 그룹 클래스',
      img: '/image/platform2/contents4.png',
      gridClass: 'md:col-span-2',
    },
  ];

  return (
    <section className="py-24 bg-white">
      <div className="max-w-[1200px] mx-auto px-6">
        {/* 상단 텍스트 영역 (중앙 정렬) */}
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-5xl font-bold mb-6 tracking-tight text-gray-900">
            러닝을 경험하는 새로운 방식
          </h2>
          <p className="text-lg leading-relaxed max-w-3xl mx-auto whitespace-pre-line">
            {`온레이스에서는 총 4가지 카테고리의 러닝 이벤트가 개최됩니다.\n나만의 스타일과 목적에 맞춰 달리고, 운동화 끈을 묶는 순간의 설렘을 느껴보세요.`}
          </p>
        </div>

        {/* 그리드 레이아웃 (1열 2:1 / 2열 1:2) */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {platformContents.map((content) => (
            <div
              key={content.id}
              className={`relative h-[480px] rounded-lg overflow-hidden shadow-lg ${content.gridClass}`}
            >
              {/* 배경 이미지 */}
              <img
                src={content.img}
                alt={content.title}
                className="absolute inset-0 w-full h-full object-cover"
              />

              {/* 하단 콘텐츠 영역 */}
              <div className="absolute bottom-0 left-0 p-10 w-full">
                {/* 타이틀 위 태그 추가 */}
                <span className="inline-block text-xs font-bold text-lime-600 mb-2">
                  {content.tag}
                </span>

                <h3 className="text-3xl font-bold text-white mb-2">
                  {content.title}
                </h3>

                {/* 서브타이틀 */}
                <p className="text-white text-sm font-medium whitespace-pre-line">
                  {content.subtitle}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
