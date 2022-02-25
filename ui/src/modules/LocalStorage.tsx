import { useEffect, useState } from 'react';

export const getStorageValue = (key: string, defaultValue: boolean) => {
  const saved = localStorage.getItem(key) as string;
  const initial = JSON.parse(saved);
  return initial || defaultValue;
};

export const useLocalStorage = (key: string, defaultValue: boolean) => {
  const [value, setValue] = useState(() => {
    return getStorageValue(key, defaultValue);
  });

  useEffect(() => {
    localStorage.setItem(key, value);
  }, [key, value]);

  return [value, setValue];
};
