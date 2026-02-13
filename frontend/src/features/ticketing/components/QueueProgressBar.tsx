interface Props {
  progress: number;
}

export const QueueProgressBar = ({ progress }: Props) => {
  return (
    <div className="w-full">
      <div className="h-4 w-full bg-gray-100 rounded-full overflow-hidden border border-gray-200">
        <div
          className="h-full bg-black"
          style={{
            width: `${progress}%`,
            transition: 'width 0.1s linear',
          }}
        />
      </div>
      {/* <div className="flex justify-between mt-2">
        <span className="text-xs text-gray-400 font-medium">진행률</span>
        <span className="text-xs text-black font-bold">
          {progress.toFixed(1)}%
        </span>
      </div> */}
    </div>
  );
};
