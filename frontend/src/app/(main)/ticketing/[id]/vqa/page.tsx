'use client';

import { useParams, useRouter } from 'next/navigation';
import { useQueue } from '@/features/ticketing/hooks';

export default function VqaPage() {
  const params = useParams();
  const router = useRouter();

  const eventId = params.id as string;
  const { resetQueue } = useQueue(eventId);

  return (
    <div className="flex items-center justify-center ">
      <div className="max-w-2xl w-full p-8 bg-white border-2 border-gray-200 rounded-none">
        <div className="p-4 bg-gray-100 rounded-sm border border-gray-100 text-center">
          <div className="py-2">
            <p className="text-gray-500 font-bold">매크로 검증(VQA)</p>
          </div>
          <div className="py-2 border-2 border-dashed border-gray-500">
            <p
              className="text-gray-500 font-bold cursor-pointer"
              onClick={() => {
                resetQueue();
                router.push(`/ticketing/${eventId}/waitQueue`);
              }}
            >
              [reCAPTCHA 또는 봇 검증 영역]
            </p>
          </div>
          <div className="py-2">
            <p className="text-gray-400 font-bold">
              정상적인 사용자 확인을 위한 검증을 필요합니다.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
