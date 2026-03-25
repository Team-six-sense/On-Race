import { Button } from '@/components/ui/button';

export default function Content3() {
  return (
    <section className="py-4">
      <div className="flex flex-col items-center justify-center h-[300px] max-w-[1200px] mx-auto bg-black text-white  ">
        {/* 메인 콘텐츠 */}
        <main className="pb-6">
          <div className="flex flex-col justify-center items-center space-y-4 ">
            <p className="text-sm text-lime-600">NextStop</p>
            <div>
              <p className="text-4xl font-bold">지금까지 꿈꿔왔던 러닝</p>
              <p className="text-4xl font-bold">온레이스로 시작하세요</p>
            </div>
            <p className="text-sm">가장 공정한 출발선에서 함께 달려요</p>
          </div>
        </main>

        {/* 하단 버튼 섹션 */}
        <footer>
          <Button variant="outline" rounded="full">
            이벤트 전체보기
          </Button>
        </footer>
      </div>
    </section>
  );
}
