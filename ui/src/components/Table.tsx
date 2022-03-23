import * as React from 'react';
import { HistoricData } from '../models/HistoricData';
import Paper from '@mui/material/Paper';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import { formateDate } from '../utils/utils';

interface Column {
  id:
    | 'processId'
    | 'csvType'
    | 'numberOfItems'
    | 'numberOfFailedItems'
    | 'numberOfSucceededItems'
    | 'status'
    | 'startDate'
    | 'endDate';
  label: string;
  minWidth?: number;
  align?: 'right';
  format?: (value: string) => string;
}

const columns: readonly Column[] = [
  { id: 'processId', label: 'ProcessId', minWidth: 170 },
  { id: 'csvType', label: 'CSV Type', minWidth: 100 },
  {
    id: 'numberOfItems',
    label: 'Number of Items',
    minWidth: 170,
    align: 'right',
  },
  {
    id: 'numberOfFailedItems',
    label: 'Number of Failed Items',
    minWidth: 170,
    align: 'right',
  },
  {
    id: 'numberOfSucceededItems',
    label: 'Number of Succeded Items',
    minWidth: 170,
    align: 'right',
  },
  {
    id: 'status',
    label: 'Status',
    minWidth: 170,
    align: 'right',
  },
  {
    id: 'startDate',
    label: 'Start Date',
    minWidth: 170,
    align: 'right',
    format: (value: string) => formateDate(value),
  },
  {
    id: 'endDate',
    label: 'End Date',
    minWidth: 170,
    align: 'right',
    format: (value: string) => formateDate(value),
  },
];

const rowsData: HistoricData[] = [];

export default function StickyHeadTable({
  rows = rowsData,
  page = 0,
  rowsPerPage = 15,
  totalElements = 0,
  setPage = (p: number) => {},
  setRowsPerPage = (r: number) => {},
}) {
  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(+event.target.value);
    setPage(0);
  };

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      <TableContainer sx={{ maxHeight: 640 }}>
        <Table stickyHeader aria-label="sticky table">
          <TableHead>
            <TableRow>
              {columns.map(column => (
                <TableCell key={column.id} align={column.align} style={{ minWidth: column.minWidth }}>
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map(row => {
              return (
                <TableRow hover role="checkbox" tabIndex={-1} key={row.processId}>
                  {columns.map(column => {
                    const value = row[column.id];
                    return (
                      <TableCell key={column.id} align={column.align}>
                        {column.format && typeof value === 'string' ? column.format(value) : value}
                      </TableCell>
                    );
                  })}
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[15, 25, 30]}
        component="div"
        count={totalElements}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Paper>
  );
}
