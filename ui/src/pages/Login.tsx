// Copyright 2022 Catena-X
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import getHash from '../modules/Hash';
import FormControl from '@mui/material/FormControl';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import TextField from '@mui/material/TextField';
import InputLabel from '@mui/material/InputLabel';
import OutlinedInput from '@mui/material/OutlinedInput';

interface Props {
  setIsAuth: any;
  setIsAuthError: React.Dispatch<React.SetStateAction<boolean>>;
  isAuthError: boolean;
}

type FormValues = {
  username: string;
  password: string;
};

interface State {
  amount: string;
  password: string;
  weight: string;
  weightRange: string;
  showPassword: boolean;
}

export const Login: React.FC<Props> = ({ setIsAuth, setIsAuthError, isAuthError }) => {
  const { register, handleSubmit } = useForm<FormValues>();
  const navigate = useNavigate();
  const onSubmit = handleSubmit(({ username, password }) => {
    if (getHash(password) === window._env_.PASS && username === window._env_.USERNAME) {
      setIsAuth(true);
      navigate('/dashboard');
    } else {
      setIsAuthError(true);
    }
  });

  const [passwordValue, setPasswordValue] = useState<State>({
    amount: '',
    password: '',
    weight: '',
    weightRange: '',
    showPassword: false,
  });

  const handleChange = (prop: keyof State) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setPasswordValue({ ...passwordValue, [prop]: event.target.value });
  };

  const handleClickShowPassword = () => {
    setPasswordValue({
      ...passwordValue,
      showPassword: !passwordValue.showPassword,
    });
  };

  const handleMouseDownPassword = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
  };

  return (
    <div className="grid grid-cols-2 gap-0">
      <div className="min-h-screen flex flex-col justify-center bg-white bg-fixed bg-[url('../public/images/login.jpg')] bg-cover"></div>
      <div className="min-h-screen flex flex-col justify-center items-center ">
        <img src="images/logo-dft-blue.svg" alt="DFT logo" className=" w-32 h-32 fill-white" />

        {isAuthError ? (
          <div>
            <label htmlFor="" className="text-sm font-bold text-red-600 block text-center content-center ">
              Username or Password is Incorrect
            </label>
          </div>
        ) : (
          <div />
        )}
        <div className="max-w-md w-full mx-auto mt-4 bg-white p-8  ">
          <form action="" className="space-y-6" onSubmit={onSubmit}>
            <TextField
              className="w-full p-2 border border-gray-300 rounded mt-1"
              {...register('username', { required: true, minLength: 5, maxLength: 80 })}
              required
              id="username"
              label="Username"
            />
            <FormControl className="w-full p-2 border border-gray-300 rounded mt-1">
              <InputLabel htmlFor="outlined-adornment-password">Password</InputLabel>
              <OutlinedInput
                id="outlined-adornment-password"
                type={passwordValue.showPassword ? 'text' : 'password'}
                value={passwordValue.password}
                {...register('password', { required: true, minLength: 4, maxLength: 80 })}
                onChange={handleChange('password')}
                endAdornment={
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={handleClickShowPassword}
                      onMouseDown={handleMouseDownPassword}
                      edge="end"
                    >
                      {passwordValue.showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                }
                label="Password"
              />
            </FormControl>

            <div>
              <button
                type="submit"
                className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 rounded-md text-white text-sm"
              >
                Login
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
