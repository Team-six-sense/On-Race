interface Props {
  progress: number;
}

export const QueueProgressBar = ({ progress }: Props) => {
  return (
    <div className="w-full">
      <div className="h-4 w-full bg-gray-100 rounded-full overflow-hidden border border-gray-200">
        <div
          className="h-full bg-gradient-to-r from-gray-600 to-black" // 이 부분을 수정했습니다.
          style={{
            width: `${progress}%`,
            transition: 'width 0.1s linear',
          }}
        />
      </div>
    </div>
  );
};
