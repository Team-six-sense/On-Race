import Link from 'next/link';

export default function Footer() {
  return (
    <footer className="border-t border-gray-100">
      <div className="max-w-7xl mx-auto px-4 py-4 md:px-6 md:py-6 lg:px-8 lg:py-8">
        <div className="flex flex-col md:flex-row justify-between gap-10">
          {/* 왼쪽: 로고 및 설명 */}
          <div className="md:w-2/3">
            <div className="flex items-center gap-2 mb-4">
              <div className="bg-gray-200 w-10 h-10 flex rounded-lg" />
              <h2 className="text-xl font-bold text-black">LOGO</h2>
            </div>
            <div className="space-y-1">
              <p className="text-gray-600 text-sm leading-relaxed">
                (주) 온레이스 | 대표이사 홍길동 | 서울 특별시 강남구 테헤란로
                123
              </p>
              <p className="text-gray-600 text-sm leading-relaxed">
                고객센터: 1234-5678 | 이메일: help@contact.com
              </p>
              <p className="text-gray-600 text-sm leading-relaxed">
                사업자등록번호: 123-45-67890 | 통신판매업신고:
                제2026-서울강남-12345호
              </p>
            </div>
          </div>

          {/* 오른쪽: 서비스 메뉴 및 고객센터 */}
          <div className="flex gap-12">
            {/* 서비스 섹션 */}
            <div>
              <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
                서비스
              </h3>
              <ul className="space-y-2">
                <li>
                  <Link
                    href="/schedule"
                    className="text-sm text-gray-600 hover:text-black transition-colors"
                  >
                    이벤트
                  </Link>
                </li>
                <li>
                  <Link
                    href="/mypage"
                    className="text-sm text-gray-600 hover:text-black transition-colors"
                  >
                    마이페이지
                  </Link>
                </li>
              </ul>
            </div>

            {/* 고객상담 섹션 */}
            <div>
              <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
                고객상담
              </h3>
              <ul className="space-y-1">
                <li className="text-sm text-gray-600">평일 09:00 ~ 18:00</li>
                <li className="text-sm text-gray-400">
                  (점심시간 12:00 ~ 13:00 제외)
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* 추가된 최하단 영역: 정책 메뉴 및 Copyright */}
        <div className="mt-4 pt-4 border-t border-gray-200 flex flex-col md:flex-row justify-between items-center gap-4">
          <div className="flex flex-wrap justify-center md:justify-start gap-x-6 gap-y-2">
            <Link
              href="/policy/privacy"
              className="text-sm font-bold text-gray-600 hover:text-black transition-colors"
            >
              개인정보 처리방침
            </Link>
            <Link
              href="/policy/terms"
              className="text-sm text-gray-600 hover:text-black transition-colors"
            >
              이용약관
            </Link>
            <Link
              href="/policy/refund"
              className="text-sm text-gray-600 hover:text-black transition-colors"
            >
              환불/교환정책
            </Link>
          </div>
        </div>
        <div>
          <p className="text-sm text-gray-500 pt-4">
            © 2026 On. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
}
