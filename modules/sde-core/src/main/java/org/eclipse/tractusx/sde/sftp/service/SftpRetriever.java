/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.sftp.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.utils.TryUtils;
import org.eclipse.tractusx.sde.sftp.RetrieverI;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SftpRetriever implements RetrieverI {
	private ChannelSftp channelSftp;
	private Session session;
	private final CsvHandlerService csvHandlerService;
	private final Map<String, String> idToPath;
	private final Map<String, String> idToPolicy;
	private final String inProgressLocation;
	private final String successLocation;
	private final String partialSuccessLocation;
	private final String failedLocation;
	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String pKey;

	public SftpRetriever(CsvHandlerService csvHandlerService, String host, int port, String username, String password,
			String pKey, String toBeProcessedLocation, String inProgressLocation, String successLocation,
			String partialSuccessLocation, String failedLocation) throws JSchException, SftpException {
		this.csvHandlerService = csvHandlerService;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.pKey = pKey;
		this.inProgressLocation = inProgressLocation;
		this.successLocation = successLocation;
		this.partialSuccessLocation = partialSuccessLocation;
		this.failedLocation = failedLocation;

		idToPath = ensureConnected().ls(toBeProcessedLocation).stream().filter(lsEntry -> !lsEntry.getAttrs().isDir())
				.filter(lsEntry -> lsEntry.getFilename().toLowerCase().endsWith(".csv"))
				.map(lsEntry -> toBeProcessedLocation + "/" + lsEntry.getFilename())
				.collect(Collectors.toMap(path -> UUID.randomUUID().toString(), Function.identity()));
		idToPolicy = new ConcurrentHashMap<>();
	}

	private ChannelSftp ensureConnected() throws JSchException {
		if (session == null || !session.isConnected()) {
			JSch jsch = new JSch();
			if (pKey != null) {
				jsch.addIdentity(host + "-agent", pKey.getBytes(), null, null);
			}
			session = jsch.getSession(username, host, port);
			if (password != null) {
				session.setPassword(password);
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.setConfig("PreferredAuthentications", "publickey,password");
			session.connect();

			channelSftp = (ChannelSftp) session.openChannel("sftp");
		}
		if (!channelSftp.isConnected()) {
			channelSftp.connect();
		}
		return channelSftp;
	}

	private void disconnect() {
		if (channelSftp.isConnected()) {
			channelSftp.disconnect();
		}
		if (session.isConnected()) {
			session.disconnect();
		}
	}

	private void moveTo(String id, String newLocation) throws IOException {
		try {
			var newPath = newLocation + "/" + getFileName(id);
			ensureConnected().rename(idToPath.get(id), newPath);
			idToPath.put(id, newPath);
		} catch (JSchException | SftpException e) {
			throw new IOException(e);
		}
	}

	public String getFileName(String id) {
		return Optional.ofNullable(idToPath.get(id)).map(path -> path.substring(path.lastIndexOf('/') + 1))
				.orElseThrow();
	}

	@Override
	public void setPolicyName(String id, String policyName) {
		idToPolicy.put(id, policyName);
	}

	@Override
	public String getPolicyName(String id) {
		return Optional.ofNullable(idToPolicy.get(id)).map(path -> path.substring(path.lastIndexOf('/') + 1))
				.orElseThrow();
	}

	@Override
	public void setProgress(String id) throws IOException {
		moveTo(id, inProgressLocation);
	}

	@Override
	public void setSuccess(String id) throws IOException {
		moveTo(id, successLocation);
	}

	@Override
	public void setPartial(String id) throws IOException {
		moveTo(id, partialSuccessLocation);
	}

	@Override
	public void setFailed(String id) throws IOException {
		moveTo(id, failedLocation);
	}

	@Override
	public void close() {
		disconnect();
		log.debug("Ftps client {} disconnected", host);
	}

	@Override
	public Iterator<String> iterator() {
		var it = idToPath.entrySet().iterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
				.map(next -> new Object() {
					final String id = next.getKey();
					final String filePath = next.getValue();
					final File localFile = new File(csvHandlerService.getFilePath(id));
				}).flatMap(o -> TryUtils.tryExec(() -> {
					Files.copy(ensureConnected().get(o.filePath), o.localFile.toPath());
					return o.id;
				}, err -> o.localFile.delete()).stream()).iterator();
	}
	
	@Override
	public int size() {
		return idToPath.size();
	}
}
