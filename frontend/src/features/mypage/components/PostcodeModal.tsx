'use client';
import DaumPostcodeEmbed from 'react-daum-postcode';

interface PostcodeModalProps {
  onComplete: (data: any) => void;
  onClose: () => void;
}

export default function PostcodeModal({
  onComplete,
  onClose,
}: PostcodeModalProps) {
  const handleComplete = (data: any) => {
    let fullAddress = data.address;
    let extraAddress = '';

    if (data.addressType === 'R') {
      if (data.bname !== '') extraAddress += data.bname;
      if (data.buildingName !== '')
        extraAddress +=
          extraAddress !== '' ? `, ${data.buildingName}` : data.buildingName;
      fullAddress += extraAddress !== '' ? ` (${extraAddress})` : '';
    }

    onComplete({ zonecode: data.zonecode, fullAddress });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/50">
      <div className="w-full max-w-lg bg-white p-4 rounded-lg">
        <div className="flex justify-between mb-4">
          <h2 className="font-bold">주소 검색</h2>
          <button onClick={onClose}>닫기</button>
        </div>
        <DaumPostcodeEmbed onComplete={handleComplete} />
      </div>
    </div>
  );
}
