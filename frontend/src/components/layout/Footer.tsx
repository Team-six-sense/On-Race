import Link from 'next/link';

export default function Footer() {
  return (
    <footer className="bg-gray-50 border-t border-gray-200">
      <div className="max-w-7xl mx-auto px-4 py-12 md:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row justify-between gap-10">
          
          {/* 왼쪽: 로고 및 설명 */}
          <div className="md:w-1/3">
            <div className="flex items-center gap-2 mb-3">
              <div className="bg-gray-200 w-12 h-12 flex rounded-lg" />
              <h2 className="text-xl font-bold text-black">LOGO</h2>
            </div>
            <p className="text-gray-600 text-sm leading-relaxed">
              러닝 이벤트 티켓팅 플랫폼 <br />
              온러닝과 함께하세요
            </p>
          </div>

          {/* 오른쪽: 서비스 약관 및 고객센터 */}
          <div className="grid grid-cols-3 gap-1">
            
             {/* 서비스 정책 섹션 */}
            <div>
              <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
                서비스
              </h3>
              <ul className="space-y-3">
                <li>
                  <Link href="/" className="text-sm text-gray-600 hover:text-black transition-colors">
                    이벤트
                  </Link>
                </li>
                <li>
                  <Link href="/" className="text-sm text-gray-600 hover:text-black transition-colors">
                    마이페이지
                  </Link>
                </li>
              </ul>
            </div>

            {/* 서비스 정책 섹션 */}
            <div>
              <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
                약관 및 정책
              </h3>
              <ul className="space-y-3">
                <li>
                  <Link href="/" className="text-sm text-gray-600 hover:text-black transition-colors">
                    이용약관
                  </Link>
                </li>
                <li>
                  <Link href="/" className="text-sm text-gray-600 hover:text-black transition-colors">
                    개인정보 처리방침
                  </Link>
                </li>
                <li>
                  <Link href="/" className="text-sm text-gray-600 hover:text-black transition-colors">
                    환불/교환정책
                  </Link>
                </li>
              </ul>
            </div>

            {/* 고객센터 섹션 */}
            <div>
              <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
                고객센터
              </h3>
              <ul className="space-y-3">
                <li>
                  <p className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                    이메일: contact@onrunning.com
                  </p>
                </li>
                <li>
                  <p className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                    전화: 1234-5678
                  </p>
                </li>
                <li>
                  <p className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
                    운영시간: 평일 09:00~18:00
                  </p>
                </li>
              </ul>
            </div>

          </div>
        </div>
      </div>
    </footer>
  );
}