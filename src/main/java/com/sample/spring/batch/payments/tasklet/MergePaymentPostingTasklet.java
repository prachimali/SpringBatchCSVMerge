package com.sample.spring.batch.payments.tasklet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
/**
 * 
 * Tasklet for MergePaymentPosting job.
 *
 */
public class MergePaymentPostingTasklet implements Tasklet {

	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
		File dir = new File(System.getProperty("user.dir"));
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		});
		for (File file : files) {
			String currentFileName = file.getName();
			System.out.println("Archiving file "+currentFileName);
			String gzipFileName = currentFileName + ".gz";
			FileInputStream fin = new FileInputStream(file);
			GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzipFileName));
			byte[] buffer = new byte[4096];
			int bytes_read;
			while ((bytes_read = fin.read(buffer)) != -1)
				out.write(buffer, 0, bytes_read);
			fin.close();
			out.close();
		}
		return RepeatStatus.FINISHED;
	}

}
