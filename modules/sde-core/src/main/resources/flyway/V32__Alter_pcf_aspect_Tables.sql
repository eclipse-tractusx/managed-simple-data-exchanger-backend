/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
 
DO $$ 
BEGIN 
	IF EXISTS (select 1 as foundtable from pg_tables WHERE tablename = 'pcf_aspect') 
	THEN 
	DELETE FROM pcf_aspect WHERE id IN (SELECT id FROM (SELECT id, ROW_NUMBER() OVER( PARTITION BY productid ORDER BY  id ) AS row_num FROM pcf_aspect ) t WHERE t.row_num > 1);
END IF;
END $$;

ALTER TABLE IF EXISTS pcf_aspect DROP CONSTRAINT IF EXISTS "pcf_aspect_un";

ALTER TABLE IF EXISTS pcf_aspect ADD CONSTRAINT pcf_aspect_un UNIQUE (productid);
