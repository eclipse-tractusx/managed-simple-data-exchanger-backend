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

import axios from 'axios';

axios.defaults.headers.common.API_KEY = 'aec093c2c98144f99a4a365ad1d2f05e';

export default axios.create({
  baseURL: 'https://dft.tracefoss.com:8080/api',
});
