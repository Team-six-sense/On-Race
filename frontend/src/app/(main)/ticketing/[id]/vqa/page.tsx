'use client';
import { Button } from '@/components/ui/button';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radioGroup';
import { VqaTooltip } from '@/features/ticketing/components/VqaTooltip';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useRef, useState } from 'react';
import { FaVolumeDown, FaVolumeMute, FaVolumeUp } from 'react-icons/fa';
import { LuPlay } from 'react-icons/lu';
import { MdReplay } from 'react-icons/md';

export default function SecurityAuthPage() {
  const params = useParams();
  const router = useRouter();
  const [selectedValue, setSelectedValue] = useState('');
  const [isPlaying, setIsPlaying] = useState(false);
  const [volume, setVolume] = useState(1);
  const [isMuted, setIsMuted] = useState(false);

  const [videoFinished, setVideoFinished] = useState(false);
  const [audioFinished, setAudioFinished] = useState(false);

  const videoRef = useRef<HTMLVideoElement>(null);
  const audioRef = useRef<HTMLAudioElement>(null);

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newVolume = parseFloat(e.target.value);
    setVolume(newVolume);
    if (audioRef.current) {
      audioRef.current.volume = newVolume;
      audioRef.current.muted = newVolume === 0;
      setIsMuted(newVolume === 0);
    }
  };

  const toggleMute = () => {
    if (audioRef.current) {
      if (isMuted) {
        audioRef.current.muted = false;
        audioRef.current.volume = volume || 0.5;
        setVolume(volume || 0.5);
        setIsMuted(false);
      } else {
        audioRef.current.muted = true;
        setIsMuted(true);
      }
    }
  };

  const handlePlay = () => {
    if (videoRef.current && audioRef.current) {
      setVideoFinished(false);
      setAudioFinished(false);
      videoRef.current.currentTime = 0;
      audioRef.current.currentTime = 0;
      audioRef.current.volume = isMuted ? 0 : volume;

      Promise.all([videoRef.current.play(), audioRef.current.play()])
        .then(() => setIsPlaying(true))
        .catch(() => {});
    }
  };

  useEffect(() => {
    if (videoFinished && audioFinished) setIsPlaying(false);
  }, [videoFinished, audioFinished]);

  return (
    <div className="flex items-center justify-center p-4">
      <div className="w-full max-w-2xl overflow-hidden rounded-sm bg-white">
        <div className="flex items-center border-b border-gray-100 px-6 py-5">
          <p className="text-xl font-bold mr-2">보안 인증</p>
          <VqaTooltip />
        </div>

        <div className="flex flex-col items-start px-6 py-6 w-full">
          <div className="relative w-full mb-6 aspect-video bg-black rounded-sm overflow-hidden group">
            <video
              ref={videoRef}
              onEnded={() => setVideoFinished(true)}
              className="w-full h-full object-cover pointer-events-none"
              playsInline
            >
              <source src="/animations/vqa_video.mp4" type="video/mp4" />
            </video>

            <audio
              ref={audioRef}
              src="/animations/vqa_audio.mp3"
              onEnded={() => setAudioFinished(true)}
            />

            {/* 볼륨 컨트롤 레이아웃 */}
            <div className="absolute top-3 right-3 flex items-center gap-1 bg-black/40 hover:bg-black/70 p-2 rounded-full transition-all duration-300 backdrop-blur-md z-20 group/volume overflow-hidden">
              <input
                type="range"
                min="0"
                max="1"
                step="0.01"
                value={isMuted ? 0 : volume}
                onChange={handleVolumeChange}
                className={`
                  /* 기본 너비는 0, 호버 시 확장 */
                  w-0 group-hover/volume:w-24 group-hover/volume:mx-2 transition-all duration-300
                  appearance-none bg-white/30 h-1 rounded-lg cursor-pointer outline-none
                  /* [핵심] 호버 전에는 thumb(원)을 투명/크기 0으로, 호버 시에만 보이게 설정 */
                  [&::-webkit-slider-thumb]:appearance-none
                  [&::-webkit-slider-thumb]:w-0
                  [&::-webkit-slider-thumb]:h-0
                  group-hover/volume:[&::-webkit-slider-thumb]:w-3
                  group-hover/volume:[&::-webkit-slider-thumb]:h-3
                  group-hover/volume:[&::-webkit-slider-thumb]:bg-white
                  group-hover/volume:[&::-webkit-slider-thumb]:rounded-full
                  group-hover/volume:[&::-webkit-slider-thumb]:transition-all
                `}
              />
              <button
                onClick={toggleMute}
                className="text-white p-1 hover:scale-110 transition-transform"
              >
                {isMuted || volume === 0 ? (
                  <FaVolumeMute size={18} />
                ) : volume < 0.5 ? (
                  <FaVolumeDown size={18} />
                ) : (
                  <FaVolumeUp size={18} />
                )}
              </button>
            </div>

            {!isPlaying && (
              <div className="absolute inset-0 flex items-center justify-center bg-black/30 z-10">
                <Button
                  onClick={handlePlay}
                  variant="ghost"
                  rounded="full"
                  size="icon"
                  className="text-white hover:bg-black/60"
                >
                  {videoRef.current && videoRef.current.currentTime > 0 ? (
                    <MdReplay />
                  ) : (
                    <LuPlay />
                  )}
                </Button>
              </div>
            )}
          </div>

          <div className="text-lg font-bold mb-5 leading-tight text-gray-800">
            3등으로 결승선을 통과한 러너의 기록은 무엇입니까?
          </div>

          <div className="w-full">
            <RadioGroup
              value={selectedValue}
              onValueChange={setSelectedValue}
              className="flex flex-col gap-3"
            >
              {[
                { id: '1', label: '02:15:20.05' },
                { id: '2', label: '02:16:15.10' },
                { id: '3', label: '02:17:05.45' },
                { id: '4', label: '02:18:11.00' },
              ].map((item) => (
                <label
                  key={item.id}
                  className={`flex items-center space-x-3 cursor-pointer px-4 transition-all`}
                >
                  <RadioGroupItem value={item.id} id={item.id} />
                  <span className={`text-base font-semibold`}>
                    {item.label}
                  </span>
                </label>
              ))}
            </RadioGroup>
          </div>
        </div>

        <div className="px-6 py-6 border-t">
          <Button
            variant="primary1"
            rounded="full"
            disabled={!selectedValue}
            onClick={() => router.push(`/ticketing/${params.id}/waitQueue`)}
          >
            제출하기
          </Button>
        </div>
      </div>
    </div>
  );
}
