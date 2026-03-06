interface GifVideoProps {
  webmSrc?: string;
  className?: string;
}

const GifVideo = ({ webmSrc, className }: GifVideoProps) => {
  return (
    <div className={`relative overflow-hidden ${className}`}>
      <video
        autoPlay
        muted
        loop
        playsInline
        preload="auto"
        /* 
           아래 스타일은 비디오가 컨테이너를 가득 채우도록 설정합니다. 
           GIF처럼 보이게 하기 위해 우클릭 메뉴 등을 방지하려면 제어가 필요할 수 있습니다.
        */
        className="w-full h-full object-cover"
      >
        {/* WebM이 MP4보다 용량이 훨씬 작으므로 먼저 배치합니다. */}
        {webmSrc && <source src={webmSrc} type="video/webm" />}
        {/* 브라우저가 비디오를 지원하지 않을 때 출력 */}
        Your browser does not support the video tag.
      </video>
    </div>
  );
};

export default GifVideo;
