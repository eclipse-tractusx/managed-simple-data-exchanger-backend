import BackupOutlinedIcon from '@mui/icons-material/BackupOutlined';
import HistoryOutlinedIcon from '@mui/icons-material/HistoryOutlined';

const Sidebar = (props: any) => {
  const { isExpanded } = props;
  return (
    <aside className="will-change-width transition-width delay-300 ease-[cubic-bezier(0.2, 0, 0, 1, 0)] flex flex-col overflow-hidden z-auto order-none shadow-md">
      <div className={isExpanded ? 'w-64' : 'w-14 '}>
        <div className="h-[calc(100%-4.75rem)] flex flex-col fixed">
          <div className="will-change-width py-6 px-0 overflow-hidden relative">
            <ul className="flex flex-col p-0 list-none overflow-hidden">
              <li className="flex gap-x-2 p-4 cursor-pointer items-center relative">
                <BackupOutlinedIcon fontSize="small" />
                <p className={!isExpanded ? 'hidden' : 'flex'}>Upload file</p>
              </li>
              <li className="flex gap-x-2 p-4 cursor-pointer items-center relative">
                <HistoryOutlinedIcon fontSize="small" />
                <p className={!isExpanded ? 'hidden' : 'flex'}>Upload history</p>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </aside>
  );
};
export default Sidebar;
