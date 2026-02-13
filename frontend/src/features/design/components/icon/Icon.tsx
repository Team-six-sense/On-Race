import { BsTwitterX } from 'react-icons/bs';
import {
  FaFacebookF,
  FaLinkedinIn,
  FaStrava,
  FaTiktok,
  FaYoutube,
} from 'react-icons/fa';
import {
  LuAirplay,
  LuAlarmClock,
  LuAlarmClockCheck,
  LuAlarmClockMinus,
  LuAlarmClockOff,
  LuAlarmClockPlus,
  LuAlignCenter,
  LuArrowRight,
  LuArrowUpDown,
  LuBookmark,
  LuCalendar,
  LuCheck,
  LuChevronDown,
  LuChevronLeft,
  LuChevronRight,
  LuChevronUp,
  LuCircleAlert,
  LuCircleCheck,
  LuColumns2,
  LuEllipsis,
  LuEllipsisVertical,
  LuExternalLink,
  LuHeart,
  LuInstagram,
  LuListFilter,
  LuLoader,
  LuLock,
  LuLogOut,
  LuMail,
  LuMinus,
  LuPanelLeft,
  LuPanelLeftOpen,
  LuPencil,
  LuPlus,
  LuSearch,
  LuSettings,
  LuShare,
  LuShare2,
  LuShieldAlert,
  LuSquareDashed,
  LuTrash,
  LuUser,
  LuX,
} from 'react-icons/lu';

export const IconTable = () => {
  return (
    <div className="p-10 flex flex-col gap-10 bg-blue-100/40">
      <div className="flex flex-wrap gap-6 items-center">
        <LuSquareDashed />
        <LuChevronDown />
        <LuChevronUp />
        <LuChevronRight />
        <LuChevronLeft />
        <LuSearch />
        <LuUser />
        <LuCheck />
        <LuX />
        <LuAirplay />
        <LuColumns2 />
        <LuPanelLeftOpen />
        <LuPanelLeft />
        <LuEllipsis />
        <LuEllipsisVertical />
        <LuPencil />
        <LuBookmark />
        <LuTrash />
        <LuMinus />
        <LuPlus />
        <LuAlarmClockCheck />
        <LuAlarmClockMinus />
        <LuAlarmClockOff />
        <LuAlarmClockPlus />
        <LuAlarmClock />
      </div>

      <div className="flex flex-wrap gap-6 items-center ">
        <LuHeart />
        <LuArrowRight />
        <LuLogOut />
        <LuArrowUpDown />
        <LuLoader />
        <LuLock />
        <LuListFilter />
        <LuAlignCenter />
        <LuCircleAlert />
        <LuCircleCheck />
        <LuShieldAlert />
        <LuMail />
        <LuCalendar />
        <LuExternalLink />
        <LuSettings />
        <LuShare2 />
        <LuShare />
        <FaFacebookF />
        <LuInstagram />
        <FaLinkedinIn />
        <FaStrava />
        <FaTiktok />
        <BsTwitterX />
        <FaYoutube />
      </div>
    </div>
  );
};
