package org.eclipse.tractusx.sde.core.utils;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.eclipse.tractusx.sde.common.exception.CsvException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CsvUtil {

	@SneakyThrows
	public void generateCSV(HttpServletResponse response, String fileName, List<JsonObject> data) {

		response.setContentType("text/csv");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		writeCsv(response, data);
	}

	private void writeCsv(HttpServletResponse response, List<JsonObject> data) throws CsvException {
		try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(),
				CSVFormat.DEFAULT.withEscape(' ').withQuoteMode(QuoteMode.NONE).withDelimiter(';'))) {
			data.forEach(json -> {
				List<String> list = json.entrySet().stream().map(e -> {

					if (e != null)
						return e.getValue().toString().replace("\"", "");
					else
						return "";
				}).toList();
				try {
					csvPrinter.printRecord(list);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
		} catch (IOException ioException) {
			log.error(ioException.getMessage());
			throw new CsvException(ioException.getMessage());
		}
	}
}
