export const ShadowTable = () => {
  return (
    <div className="p-10 flex flex-col gap-10 bg-blue-50">
      {/* shadow-md */}
      <div className="w-40 h-20 bg-white shadow-xs flex items-center justify-center rounded-sm">
        shadow-xs
      </div>

      {/* shadow-md */}
      <div className="w-60 h-30 bg-white shadow-sm flex items-center justify-center rounded-sm">
        shadow-sm
      </div>
      {/* shadow-md */}
      <div className="w-80 h-40 bg-white shadow-md flex items-center justify-center rounded-sm">
        shadow-md
      </div>

      {/* shadow-lg */}
      <div className="w-100 h-40 bg-white shadow-lg flex items-center justify-center rounded-sm">
        shadow-lg
      </div>

      {/* shadow-xl */}
      <div className="w-120 h-50 bg-white shadow-xl flex items-center justify-center rounded-sm">
        shadow-xl
      </div>

      {/* forcus ring */}
      <div className="w-30 h-20 bg-white shadow-xs flex items-center justify-center rounded-sm border-3 border-[#0081FF]">
        forcus ring
      </div>

      {/* forcus ring (shadow) */}
      <div className="w-30 h-20 bg-white shadow-xs flex items-center justify-center rounded-sm border-3 border-[#0081FF]">
        forcus ring
        <br />
        (shadow)
      </div>
    </div>
  );
};
