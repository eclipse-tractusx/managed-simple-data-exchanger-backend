/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.EnableSFTP;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.notification.config.EmailConfiguration;
import org.eclipse.tractusx.sde.notification.manager.EmailManager;
import org.eclipse.tractusx.sde.retrieverl.service.SftpRetrieverFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@SpringBootTest
@EnableSFTP
@Execution(ExecutionMode.SAME_THREAD)
@ActiveProfiles("sftptest")
@WithMockUser(username = "Admin", authorities = { "Admin" })
class SftpRetrieverTest {

	@Autowired
	CsvHandlerService csvHandlerService;

	@MockBean
	EmailManager emailManager;

	@MockBean
	EmailConfiguration emailConfiguration;

	@Autowired
	ConfigService configService;

	@Autowired
	ApplicationContext context;

	private SftpRetrieverFactoryImpl sftpRetrieverFactory;

	private record MyFile(
			String name,
			String content
	){};

	private final MyFile file1 = new MyFile("file1.csv", "test 1 content\n");
	private final MyFile file2 = new MyFile("file2.csv", "test 2 content\n");

	@PostConstruct
	public void init() {
		sftpRetrieverFactory = (SftpRetrieverFactoryImpl) context.getBean("sftp");
		configService.deleteAllConfig();
	}

	@FunctionalInterface
	interface ThrowableConsumer<T> {
		public void accept(T t) throws Exception;
	}

	private void doSftp(ThrowableConsumer<ChannelSftp> sftpConsumer) throws Exception {
		var config = sftpRetrieverFactory.getConfiguration();
		JSch jsch = new JSch();
		if (config.getAccessKey() != null) {
			jsch.addIdentity(config.getHost() + "-agent", config.getAccessKey().getBytes(), null, null);
		}
		var session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
		if (config.getPassword() != null) {
			session.setPassword(config.getPassword());
		}
		session.setConfig("StrictHostKeyChecking", "no");
		session.setConfig("PreferredAuthentications", "publickey,password");
		session.connect();
		var channelSftp = (ChannelSftp) session.openChannel("sftp");
		channelSftp.connect();
		sftpConsumer.accept(channelSftp);
		channelSftp.disconnect();
		session.disconnect();
	}

	@BeforeEach
	public void createFilesInToBeProcessedLocation() throws Exception {
		final var config = sftpRetrieverFactory.getConfiguration();
		doSftp(channelSftp -> {
			var dirs = getRecDirs(channelSftp, "/", any -> {});
			for (var path : Stream.of(
					split(config.getToBeProcessedLocation()),
					split(config.getInProgressLocation()),
					split(config.getFailedLocation()),
					split(config.getPartialSuccessLocation()),
					split(config.getSuccessLocation())
			).flatMap(Function.identity()).filter(dirs::add).toList()) {
				log.info("create {} directory", path);
				channelSftp.mkdir(path);
			}
			try(var outputStream = channelSftp.put(config.getToBeProcessedLocation() + "/" + file1.name())){
				outputStream.write(file1.content.getBytes());
			}
			try(var outputStream = channelSftp.put(config.getToBeProcessedLocation() + "/" + file2.name())){
				outputStream.write(file2.content.getBytes());
			}
		});
	}
	@AfterEach
	public void cleanupFolders() throws Exception {
		doSftp(channelSftp -> {
			getRecDirs(channelSftp, "/", channelSftp::rm);
		});
	}

	private static Stream<String> split(String path) {
		var prefix = "";
		var res = new ArrayList<String>();
		for(String dir: (path.charAt(0) == '/' ? path.substring(1) : path).split("/")) {
			prefix += "/" + dir;
			res.add(prefix);
		}
		return res.stream();
	}

	public static Set<String> getRecDirs(ChannelSftp sftpChannel, String remotePath, ThrowableConsumer<String> doForFiles) throws Exception {
		var res = new HashSet<String>();
		res.add(remotePath);
		for (ChannelSftp.LsEntry entry : sftpChannel.ls(remotePath)) {
			String filename = entry.getFilename();
			if (!filename.equals(".") && !filename.equals("..")) {
				String filePath = remotePath + (remotePath.equals("/") ? "" :  "/") + filename;
				if (entry.getAttrs().isDir()) {
					res.addAll(getRecDirs(sftpChannel, filePath, doForFiles));
				} else {
					doForFiles.accept(filePath);
				}
			}
		}
		return res;
	}

	private String getFileContent(String fileName) throws Exception {
		var holder = new Object() {
			String data;
			void set(String data) {
				this.data = data;
			}
		};
		doSftp(channelSftp -> {
			try(var is = channelSftp.get(fileName)) {
				holder.set(new String(is.readAllBytes()));
			}
		});
		return holder.data;
	}

	@Test
	void testMoveFileAround() throws Exception {
		var config = sftpRetrieverFactory.getConfiguration();
		var contentSet = new HashSet<String>();
		try (var sftp = sftpRetrieverFactory.create()) {
			for (String fileId : sftp) {
				final var filePath = Path.of(csvHandlerService.getFilePath(fileId));
				final var retrievedContent = Files.readString(filePath);
				sftp.setProgress(fileId);
				Assertions.assertEquals(
						retrievedContent,
						getFileContent(config.getInProgressLocation() + "/" + sftp.getFileName(fileId))
				);
				sftp.setSuccess(fileId);
				Assertions.assertEquals(
						retrievedContent,
						getFileContent(config.getSuccessLocation() + "/" + sftp.getFileName(fileId))
				);
				sftp.setFailed(fileId);
				Assertions.assertEquals(
						retrievedContent,
						getFileContent(config.getFailedLocation() + "/" + sftp.getFileName(fileId))
				);
				sftp.setPartial(fileId);
				Assertions.assertEquals(
						retrievedContent,
						getFileContent(config.getPartialSuccessLocation() + "/" + sftp.getFileName(fileId))
				);
				contentSet.add(Files.readString(filePath));
				Files.delete(filePath);
			}
			Assertions.assertEquals(Set.of(file1.content(), file2.content()), contentSet);
		}
	}
}