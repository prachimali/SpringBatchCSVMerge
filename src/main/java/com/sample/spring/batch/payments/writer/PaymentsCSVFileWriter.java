package com.sample.spring.batch.payments.writer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.sample.spring.batch.payments.dto.PaymentDTO;
/**
 * 
 * Writer for MergePaymentPosting job.
 *
 */
public class PaymentsCSVFileWriter implements ItemStream, ItemWriter<PaymentDTO> {

	private Map<String, FlatFileItemWriter<PaymentDTO>> writers = new HashMap<String, FlatFileItemWriter<PaymentDTO>>();

	public static int fileCount = 1;

	private ExecutionContext executionContext;

	public void write(List<? extends PaymentDTO> payments) throws Exception {
		FlatFileItemWriter<PaymentDTO> ffiw = getFlatFileItemWriter();
		for (PaymentDTO payment : payments) {
			ffiw.open(executionContext);
			ffiw.write(Arrays.asList(payment));
		}

	}

	public File getFile() throws IOException {
		File paymentFileName = new File("payment_" + fileCount + ".csv");
		if (paymentFileName.exists()) {
			long fileSize = (paymentFileName.length()) / 1024;
			if (fileSize >= 200) {
				fileCount++;
				File newFile = new File("payment_" + fileCount + ".csv");
				newFile.createNewFile();
				System.out.println("Writing to " + newFile + ".");
				return newFile;
			} else {
				return paymentFileName;
			}
		} else {
			try {
				paymentFileName.createNewFile();
				System.out.println("Writing to " + paymentFileName + ".");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return paymentFileName;
		}
	}

	public FlatFileItemWriter<PaymentDTO> getFlatFileItemWriter() {
		FlatFileItemWriter<PaymentDTO> paymentFile = new FlatFileItemWriter<PaymentDTO>();
		paymentFile.setAppendAllowed(true);
		paymentFile.setShouldDeleteIfExists(false);
		try {
			paymentFile.setResource(new FileSystemResource(getFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		paymentFile.setHeaderCallback(paymentFileHeaderCallback());
		paymentFile.setLineAggregator(new DelimitedLineAggregator<PaymentDTO>() {
			{
				setDelimiter(";");
				setFieldExtractor(new BeanWrapperFieldExtractor<PaymentDTO>() {
					{
						setNames(new String[] { "paymentId", "agencyCode", "paymentAmount", "requestDateTime",
								"paymentDate", "paymentMethod", "accountId" });
					}
				});
			}
		});
		return paymentFile;
	}

	public FlatFileHeaderCallback paymentFileHeaderCallback() {
		FlatFileHeaderCallback fileHeaderCallback = new FlatFileHeaderCallback() {
			public void writeHeader(Writer writer) throws IOException {
				writer.write("paymentId;agencyCode;paymentAmount;requestDateTime;paymentDate;paymentMethod;accountId");
			}
		};
		return fileHeaderCallback;
	}

	public void open(ExecutionContext executionContext) throws ItemStreamException {
		this.executionContext = executionContext;
	}

	public void update(ExecutionContext executionContext) throws ItemStreamException {
	}

	public void close() throws ItemStreamException {
		for (FlatFileItemWriter f : writers.values()) {
			System.out.println(f);
			f.close();
		}
	}
}
