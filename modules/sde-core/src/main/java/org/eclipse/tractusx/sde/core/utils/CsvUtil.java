package org.eclipse.tractusx.sde.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CsvUtil {

	@SneakyThrows
	public ResponseEntity<Resource> generateCSV(String fileName, List<List<String>> data) {

		InputStreamResource file = new InputStreamResource(writeCsv(data));

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.contentType(MediaType.parseMediaType("application/csv")).body(file);
	}

	@SneakyThrows
	public static ByteArrayInputStream writeCsv(List<List<String>> data) {
		final CSVFormat format = CSVFormat.EXCEL.withEscape(' ').withQuoteMode(QuoteMode.NONE).withDelimiter(';');

		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {
			data.forEach(list -> {
				try {
					csvPrinter.printRecord(list);
				} catch (IOException e1) {
					log.error(e1.getMessage());
				}
			});

			csvPrinter.flush();
			return new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			throw new ServiceException("fail to import data to CSV file: " + e.getMessage());
		}
	}


}
